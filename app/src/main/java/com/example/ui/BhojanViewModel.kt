package com.example.ui

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FoodItem
import com.example.data.FoodMenu
import com.example.db.BhojanDatabase
import com.example.db.BhojanRepository
import com.example.db.CartItemEntity
import com.example.db.OrderEntity
import com.example.db.ReviewEntity
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    Login,
    Home,
    FoodDetail,
    Cart,
    Checkout,
    Payment,
    OrderTracking,
    OrderHistory
}

data class PromoCode(
    val code: String,
    val description: String,
    val discountPercent: Int = 0,
    val flatDiscount: Double = 0.0,
    val minOrderAmount: Double = 300.0
)

data class DeliveryAddress(
    val name: String,
    val phone: String,
    val addressLine: String,
    val area: String,
    val latitude: Double,
    val longitude: Double
)

class BhojanViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BhojanDatabase.getDatabase(application)
    private val repository = BhojanRepository(db)
    private val notificationHelper = NotificationHelper(application)

    // --- NAVIGATION STATE ---
    var currentScreen by mutableStateOf(AppScreen.Home) // Start at Home (or Login if not authenticated)
    var selectedFoodItem: FoodItem? by mutableStateOf(null)

    // --- AUTHENTICATION STATE ---
    var isLoggedIn by mutableStateOf(false)
    var phoneNumber by mutableStateOf("")
    var otpSent by mutableStateOf(false)
    var generatedOtp by mutableStateOf("")
    var otpInput by mutableStateOf("")
    var userName by mutableStateOf("Bhojan Nepal Customer")

    // --- LOCATION STATE ---
    var selectedLocation by mutableStateOf(
        DeliveryAddress(
            name = "My Home",
            phone = "",
            addressLine = "Balaju-16, Kathmandu",
            area = "Kathmandu Valley",
            latitude = 27.7317,
            longitude = 85.3059
        )
    )

    // --- LOCATION SENSING STATE ---
    var isDetectingLocation by mutableStateOf(false)
    var locationErrorMsg by mutableStateOf<String?>(null)

    fun detectDeviceLocation(onLocationDetected: (String) -> Unit = {}) {
        isDetectingLocation = true
        locationErrorMsg = null
        val context = getApplication<Application>().applicationContext
        
        val hasFineLocation = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (!hasFineLocation && !hasCoarseLocation) {
            locationErrorMsg = "Permissions not granted. Please allow location access."
            isDetectingLocation = false
            return
        }
        
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            locationErrorMsg = "Location services not available on this device."
            isDetectingLocation = false
            return
        }
        
        try {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            
            if (!isGpsEnabled && !isNetworkEnabled) {
                locationErrorMsg = "GPS/Network location is disabled. Please enable device location."
                isDetectingLocation = false
                return
            }
            
            var bestLocation: Location? = null
            if (isNetworkEnabled) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (bestLocation == null && isGpsEnabled) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            
            if (bestLocation != null) {
                updateLocationState(bestLocation, onLocationDetected)
            } else {
                val provider = if (isNetworkEnabled) LocationManager.NETWORK_PROVIDER else LocationManager.GPS_PROVIDER
                locationManager.requestSingleUpdate(provider, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        updateLocationState(location, onLocationDetected)
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }, context.mainLooper)
            }
        } catch (e: SecurityException) {
            locationErrorMsg = "Permissions not satisfied."
            isDetectingLocation = false
        } catch (e: Exception) {
            locationErrorMsg = "Sensing error: ${e.localizedMessage}"
            isDetectingLocation = false
        }
    }

    private fun updateLocationState(location: Location, onLocationDetected: (String) -> Unit) {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            var addressName = ""
            var areaName = "Kathmandu Valley"
            
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val subLocality = address.subLocality ?: address.locality ?: address.subAdminArea ?: ""
                    val thoroughfare = address.thoroughfare ?: address.featureName ?: ""
                    addressName = listOfNotNull(thoroughfare.takeIf { it.isNotBlank() }, subLocality.takeIf { it.isNotBlank() }, address.locality ?: "").joinToString(", ")
                    if (addressName.isBlank()) {
                        addressName = address.getAddressLine(0) ?: "Pinpoint"
                    }
                    areaName = address.subAdminArea ?: address.adminArea ?: "Kathmandu"
                }
            } catch (e: Exception) {
                // Ignore and fallback
            }
            
            if (addressName.isBlank()) {
                addressName = getKathmanduApproxAreaName(location.latitude, location.longitude)
            }
            
            val finalAddress = addressName
            val finalArea = areaName
            withContext(Dispatchers.Main) {
                selectedLocation = selectedLocation.copy(
                    addressLine = finalAddress,
                    area = finalArea,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                isDetectingLocation = false
                onLocationDetected(finalAddress)
            }
        }
    }

    private fun getKathmanduApproxAreaName(latitude: Double, longitude: Double): String {
        val areas = listOf(
            Triple("Balaju Balaju-16, Kathmandu", 27.7317, 85.3059),
            Triple("Thamel Tourism Hub, Kathmandu", 27.7150, 85.3123),
            Triple("Patandurbar Square, Lalitpur", 27.6744, 85.3250),
            Triple("New Road Business Hub, Kathmandu", 27.7042, 85.3115),
            Triple("Kapan Monastery Area, Kathmandu", 27.7365, 85.3621)
        )
        
        var minDistance = Double.MAX_VALUE
        var closestArea = "Detected GPS Pinpoint"
        for (area in areas) {
            val dist = java.lang.Math.hypot(latitude - area.second, longitude - area.third)
            if (dist < minDistance) {
                minDistance = dist
                closestArea = area.first
            }
        }
        
        if (minDistance > 0.5) {
            return "GPS (Lat: ${String.format(Locale.US, "%.4f", latitude)}, Lon: ${String.format(Locale.US, "%.4f", longitude)})"
        }
        return closestArea
    }

    // --- SEARCH & FILTER ---
    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf("All")

    // --- COUPON SYSTEM ---
    val availableCoupons = listOf(
        PromoCode("BHOJAN20", "Special Bento Promo 20% off (Min spend Rs. 200)", discountPercent = 20, minOrderAmount = 200.0),
        PromoCode("NEPAL20", "Get 20% off on your order (Min spend Rs. 400)", discountPercent = 20, minOrderAmount = 400.0),
        PromoCode("BHOJAN50", "Flat Rs. 50 off on standard orders (Min spend Rs. 300)", flatDiscount = 50.0, minOrderAmount = 300.0)
    )
    var appliedCoupon: PromoCode? by mutableStateOf(null)
    var couponInput by mutableStateOf("")

    // --- NOTIFICATION SETTING ---
    var pushNotificationsEnabled by mutableStateOf(true)

    // --- FLOWS FROM DATABASE ---
    val cartItems = repository.cartItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favorites = repository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val ordersList = repository.orders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active order being tracked
    var activeTrackingOrder: OrderEntity? by mutableStateOf(null)

    // Temporary reviews map for live loaded reviews
    private val _currentFoodReviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val currentFoodReviews = _currentFoodReviews.asStateFlow()

    private var activeTrackingJob: Job? = null

    init {
        // Automatically check if user details exist or log in as guest
        // For demonstration, let's pre-populate some reviews on first launch
        viewModelScope.launch {
            repository.addReview(ReviewEntity(foodId = "combo_family", userName = "Sagar Adhikari", rating = 5f, comment = "Excellent combo value! The Mo:Mo and chowmein pair perfectly. Highly recommend to everyone in Kathmandu!"))
            repository.addReview(ReviewEntity(foodId = "combo_family", userName = "Pooja Shrestha", rating = 4.5f, comment = "Delivered hot and on time. Solid taste."))
            repository.addReview(ReviewEntity(foodId = "momo_buff", userName = "Rohan Devkota", rating = 5f, comment = "Best Buff Mo:Mo in valley, authentic achar is superb!"))
            repository.addReview(ReviewEntity(foodId = "burger_cheese", userName = "Ankit Verma", rating = 4f, comment = "Juicy burger and lots of cheese. Good presentation."))
        }
    }

    // --- ACTIONS ---

    // Send OTP Simulated
    fun sendOtp(phone: String) {
        if (phone.length < 10) {
            showToast("Please enter a valid 10-digit mobile number")
            return
        }
        phoneNumber = phone
        // Generate random 4 digit code
        val randomNum = (1000..9999).random().toString()
        generatedOtp = randomNum
        otpSent = true
        showToast("OTP sent to $phone for Bhojan Nepal. Code: $randomNum")
    }

    // Verify OTP Simulated
    fun verifyOtp(code: String) {
        if (code == generatedOtp || code == "1234") { // Allow 1234 as universal bypass for easier testing
            isLoggedIn = true
            otpSent = false
            currentScreen = AppScreen.Home
            showToast("LoggedIn Successfully as +977 $phoneNumber")
        } else {
            showToast("Invalid OTP. Please try again.")
        }
    }

    fun logout() {
        isLoggedIn = false
        phoneNumber = ""
        otpSent = false
        generatedOtp = ""
        otpInput = ""
        showToast("Logged out of Bhojan Nepal")
        currentScreen = AppScreen.Home
    }

    // Search and filter menu
    fun getFilteredFoodItems(): List<FoodItem> {
        return FoodMenu.menuList.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) || 
                                item.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    // Cart operations
    fun addItemToCart(item: FoodItem) {
        viewModelScope.launch {
            repository.addToCart(
                foodId = item.id,
                name = item.name,
                price = item.price,
                discountPercent = item.discountPercent,
                category = item.category
            )
            showToast("${item.name} added to cart!")
        }
    }

    fun removeCartItem(entity: CartItemEntity) {
        viewModelScope.launch {
            repository.deleteCartItem(entity.id)
            showToast("${entity.name} removed from cart")
        }
    }

    fun incrementCartQuantity(entity: CartItemEntity) {
        viewModelScope.launch {
            repository.updateCartItemDirect(entity.copy(quantity = entity.quantity + 1))
        }
    }

    fun decrementCartQuantity(entity: CartItemEntity) {
        viewModelScope.launch {
            if (entity.quantity <= 1) {
                repository.deleteCartItem(entity.id)
                showToast("${entity.name} removed from cart")
            } else {
                repository.updateCartItemDirect(entity.copy(quantity = entity.quantity - 1))
            }
        }
    }

    // Coupon logic
    fun applyCouponCode(code: String, cartSubtotal: Double) {
        val coupon = availableCoupons.find { it.code.equals(code, ignoreCase = true) }
        if (coupon != null) {
            if (cartSubtotal >= coupon.minOrderAmount) {
                appliedCoupon = coupon
                showToast("Coupon '${coupon.code}' applied successfully!")
            } else {
                showToast("Minimum order of Rs. ${coupon.minOrderAmount.toInt()} required for this coupon")
            }
        } else {
            showToast("Invalid coupon code")
        }
    }

    fun removeCoupon() {
        appliedCoupon = null
        showToast("Coupon removed")
    }

    // Favorites
    fun toggleFavoriteItem(foodId: String) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.foodId == foodId }
            repository.toggleFavorite(foodId, isFav)
        }
    }

    fun isFoodFavorite(foodId: String): Flow<Boolean> {
        return repository.isFavorite(foodId)
    }

    // Load reviews
    fun loadReviewsForFood(foodId: String) {
        viewModelScope.launch {
            repository.getReviews(foodId).collect {
                _currentFoodReviews.value = it
            }
        }
    }

    // Add review
    fun addFoodReview(foodId: String, user: String, rating: Float, comment: String) {
        if (comment.isBlank()) {
            showToast("Review comment cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.addReview(
                ReviewEntity(
                    foodId = foodId,
                    userName = user,
                    rating = rating,
                    comment = comment
                )
            )
            showToast("Review submitted successfully!")
            loadReviewsForFood(foodId)
        }
    }

    // Checkout & Place Order
    fun placeBhojanOrder(paymentMethod: String, addressInput: String) {
        val items = cartItems.value
        if (items.isEmpty()) {
            showToast("Cart is empty!")
            return
        }

        val subtotal = items.sumOf { it.discountedPrice * it.quantity }
        val discount = calcDiscountAmount(subtotal)
        val deliveryFee = 60.0 // Standard Rs. 60 delivery for Kathmandu Valley
        val grandTotal = subtotal - discount + deliveryFee

        val summary = items.joinToString { "${it.name} x${it.quantity}" }
        // Serialize details: "id|name|price|quantity|discount" separated by ";"
        val dataString = items.joinToString(";") { "${it.foodId}|${it.name}|${it.price}|${it.quantity}|${it.discountPercent}" }

        val oId = "BN-" + (100000..999999).random().toString()
        val formattedAddress = addressInput.ifBlank { selectedLocation.addressLine }

        val newOrder = OrderEntity(
            orderId = oId,
            totalAmount = grandTotal,
            itemSummary = summary,
            itemsData = dataString,
            address = formattedAddress,
            paymentMethod = paymentMethod,
            status = "Order Placed",
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.placeOrder(newOrder)
            repository.clearCart()
            appliedCoupon = null
            activeTrackingOrder = newOrder
            currentScreen = AppScreen.OrderTracking
            showToast("Order Placed Successfully via $paymentMethod!")

            if (pushNotificationsEnabled) {
                notificationHelper.showOrderNotification(
                    "Order Placed! 🍲",
                    "Your Bhojan Nepal order $oId total of Rs. ${grandTotal.toInt()} has been successfully processed."
                )
            }

            simulateOrderTracking(oId)
        }
    }

    // Simulate Order Progress
    private fun simulateOrderTracking(orderId: String) {
        activeTrackingJob?.cancel()
        activeTrackingJob = viewModelScope.launch {
            // Step 1: Confirmed after 10s
            delay(10000)
            updateTrackingState(orderId, "Confirmed", "Chef Approved 👨‍🍳", "Chef has approved your fresh meal prep!")

            // Step 2: Preparing after 12s
            delay(12000)
            updateTrackingState(orderId, "Preparing", "Cooking in Kitchen 🔥", "Your order is freshly sizzling in our central kitchen.")

            // Step 3: Out for Delivery after 14s
            delay(14000)
            updateTrackingState(orderId, "Out for Delivery", "Rider Picked Up 🛵", "Our Bhojan Nepal rider is speeding to your Kathmandu location.")

            // Step 4: Delivered after 15s
            delay(15000)
            updateTrackingState(orderId, "Delivered", "Delivered & Enjoy! 🎉", "Your food is here. Please slide to rate and enjoy your hot Bhojan!")
        }
    }

    private suspend fun updateTrackingState(orderId: String, status: String, notificationTitle: String, msg: String) {
        repository.updateOrderStatus(orderId, status)
        // Refresh active tracking order if same
        activeTrackingOrder?.let {
            if (it.orderId == orderId) {
                activeTrackingOrder = it.copy(status = status)
            }
        }
        if (pushNotificationsEnabled) {
            notificationHelper.showOrderNotification(notificationTitle, msg)
        }
    }

    // Force progress tracking manually for demo/testing
    fun manuallyProgressTracking() {
        val currOrder = activeTrackingOrder ?: return
        val nextStatus = when (currOrder.status) {
            "Order Placed" -> "Confirmed"
            "Confirmed" -> "Preparing"
            "Preparing" -> "Out for Delivery"
            "Out for Delivery" -> "Delivered"
            else -> "Delivered"
        }
        
        viewModelScope.launch {
            updateTrackingState(
                currOrder.orderId,
                nextStatus,
                "Demo Status: $nextStatus 🛵",
                "Your Bhojan Nepal tracking updated to $nextStatus"
            )
            showToast("Demo Stage updated to $nextStatus")
        }
    }

    // Reorder Past Order
    fun reorderPastItems(order: OrderEntity) {
        // ItemsData is "foodId|name|price|quantity|discount" separated by ";"
        val itemsList = order.itemsData.split(";")
        viewModelScope.launch {
            for (line in itemsList) {
                val parts = line.split("|")
                if (parts.size >= 5) {
                    val foodId = parts[0]
                    val name = parts[1]
                    val price = parts[2].toDoubleOrNull() ?: 0.0
                    val count = parts[3].toIntOrNull() ?: 1
                    val discount = parts[4].toIntOrNull() ?: 0
                    // Add item quantity times
                    for (i in 1..count) {
                        repository.addToCart(foodId, name, price, discount, "Bhojan Item")
                    }
                }
            }
            showToast("Reordered past items, added to cart!")
            currentScreen = AppScreen.Cart
        }
    }

    // Calculation assistors
    fun calcDiscountAmount(subtotal: Double): Double {
        val coupon = appliedCoupon ?: return 0.0
        return if (coupon.discountPercent > 0) {
            subtotal * (coupon.discountPercent / 100.0)
        } else {
            coupon.flatDiscount
        }
    }

    fun formatNepaliPrice(amount: Double): String {
        return "Rs. ${amount.toInt()}"
    }

    fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    private fun showToast(msg: String) {
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }
}

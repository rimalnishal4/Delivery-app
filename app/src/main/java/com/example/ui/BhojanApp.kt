package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import android.widget.Toast
import kotlinx.coroutines.delay
import com.example.data.FoodItem
import com.example.data.FoodMenu
import com.example.db.CartItemEntity
import com.example.db.OrderEntity
import com.example.db.ReviewEntity
import com.example.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BhojanApp(viewModel: BhojanViewModel) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val cartCount = cartItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Procedural custom logomark
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BhojanRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Bhojan Nepal Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "भोजन Nepal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BhojanRed
                            )
                            Text(
                                text = "Bhojan Nepal Online",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.currentScreen = AppScreen.OrderHistory
                        },
                        modifier = Modifier.testTag("action_history")
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "Order History")
                    }

                    Box(modifier = Modifier.wrapContentSize()) {
                        IconButton(
                            onClick = { viewModel.currentScreen = AppScreen.Cart },
                            modifier = Modifier.testTag("action_cart")
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Go to Cart")
                        }
                        if (cartCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(18.dp)
                                    .background(BhojanGold, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (viewModel.isLoggedIn) {
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("action_logout")
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = BhojanRed)
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.currentScreen = AppScreen.Login },
                            modifier = Modifier.testTag("action_login")
                        ) {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Login")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = BhojanRed
                ),
                modifier = Modifier.shadow(1.dp)
            )
        },
        bottomBar = {
            if (viewModel.currentScreen != AppScreen.Login && viewModel.currentScreen != AppScreen.Payment) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = viewModel.currentScreen == AppScreen.Home,
                        onClick = { viewModel.currentScreen = AppScreen.Home },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Menu") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = BhojanRed,
                            indicatorColor = BhojanRed,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.Gray
                        )
                    )

                    NavigationBarItem(
                        selected = viewModel.currentScreen == AppScreen.Cart,
                        onClick = { viewModel.currentScreen = AppScreen.Cart },
                        icon = {
                            Box {
                                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Cart")
                                if (cartCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 8.dp, y = (-4).dp)
                                            .size(14.dp)
                                            .background(BhojanRed, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(cartCount.toString(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        },
                        label = { Text("Cart") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = BhojanRed,
                            indicatorColor = BhojanRed,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.Gray
                        )
                    )

                    NavigationBarItem(
                        selected = viewModel.currentScreen == AppScreen.OrderTracking,
                        onClick = {
                            if (viewModel.activeTrackingOrder != null) {
                                viewModel.currentScreen = AppScreen.OrderTracking
                            } else {
                                viewModel.currentScreen = AppScreen.OrderHistory
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = "Track") },
                        label = { Text(if (viewModel.activeTrackingOrder != null) "Track" else "Orders") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = BhojanRed,
                            indicatorColor = BhojanRed,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (viewModel.currentScreen) {
                AppScreen.Login -> LoginScreen(viewModel)
                AppScreen.Home -> HomeScreen(viewModel, favorites)
                AppScreen.FoodDetail -> FoodDetailScreen(viewModel)
                AppScreen.Cart -> CartScreen(viewModel, cartItems)
                AppScreen.Checkout -> CheckoutScreen(viewModel, cartItems)
                AppScreen.Payment -> PaymentScreen(viewModel)
                AppScreen.OrderTracking -> OrderTrackingScreen(viewModel)
                AppScreen.OrderHistory -> OrderHistoryScreen(viewModel)
            }
        }
    }
}

// --- SCREEN 1: LOGIN (OTP Mobile Number) ---
@Composable
fun LoginScreen(viewModel: BhojanViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BhojanCream)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocalPhone,
            contentDescription = "OTP Login",
            tint = BhojanRed,
            modifier = Modifier
                .size(72.dp)
                .background(Color.White, shape = CircleShape)
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bhojan Nepal Login",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = BhojanRed
        )

        Text(
            text = "मोबाइल नम्बरबाट सुरक्षित OTP Login",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!viewModel.otpSent) {
            // Step 1: Input Mobile Number
            Text(
                text = "Enter your Nepali mobile number to receive verification code:",
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                color = DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.phoneNumber,
                onValueChange = { viewModel.phoneNumber = it },
                label = { Text("Mobile Number (E.g. 98xxxxxxxx)") },
                prefix = { Text("+977 ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_phone_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.sendOtp(viewModel.phoneNumber) },
                colors = ButtonDefaults.buttonColors(containerColor = BhojanRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("send_otp_button")
            ) {
                Text("Send OTP (कोर्ड पठाउनुहोस्)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            // Step 2: Input OTP Verified
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                border = BorderStroke(1.dp, BhojanGold)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Sms, contentDescription = "SMS", tint = BhojanGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("SMS Simulated Gateway", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                        Text("Your BhojanNepal OTP is: ${viewModel.generatedOtp}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BhojanRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "We have sent a 4-digit code to +977 ${viewModel.phoneNumber}. Enter it below:",
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                color = DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.otpInput,
                onValueChange = { viewModel.otpInput = it },
                label = { Text("4-Digit OTP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_otp_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.verifyOtp(viewModel.otpInput) },
                colors = ButtonDefaults.buttonColors(containerColor = BhojanRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("verify_otp_button")
            ) {
                Text("Verify OTP & Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { viewModel.otpSent = false }) {
                Text("Change Mobile Number", color = BhojanRed)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "By logging in, you agree to our Terms and BhojanNepal Recipes Guidelines.",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// --- SCREEN 2: HOME (Food Browser, Category, Search, Heart Favorite) ---
@Composable
fun HomeScreen(viewModel: BhojanViewModel, favoriteList: List<com.example.db.FavoriteEntity>) {
    val context = LocalContext.current
    var showCouponsDialog by remember { mutableStateOf(false) }

    if (showCouponsDialog) {
        AlertDialog(
            onDismissRequest = { showCouponsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Loyalty, contentDescription = null, tint = BentoPrimary)
                    Text("Available Bento Coupons", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply these special discount codes inside user checkout screen:", fontSize = 12.sp, color = Color.Gray)
                    viewModel.availableCoupons.forEach { coupon ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoSurface),
                            border = BorderStroke(1.dp, BentoBorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(coupon.code, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = BentoPrimary)
                                    Text(coupon.description, fontSize = 11.sp, color = BentoMutedText)
                                }
                                Button(
                                    onClick = {
                                        viewModel.couponInput = coupon.code
                                        Toast.makeText(context, "${coupon.code} Coupon Selected! Go to Cart / Checkout to see saving.", Toast.LENGTH_SHORT).show()
                                        showCouponsDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Apply", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCouponsDialog = false }) {
                    Text("Close", color = BentoPrimary, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
    ) {
        // 1. Bento Dashboard: Location Info & Notification Bell
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Arrow",
                        tint = BentoPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "DELIVER TO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = viewModel.selectedLocation.addressLine,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoDarkText
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand Location",
                        tint = BentoDarkText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BentoSurface)
                    .clickable {
                        Toast.makeText(context, "Notifications system active and configured!", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = BentoDarkText,
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .size(6.dp)
                        .background(Color(0xFFB3261E), shape = CircleShape)
                )
            }
        }

        // 2. Bento Styled Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                placeholder = { Text("Search dishes, momos or combo deals...", color = BentoMutedText, fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon", tint = BentoMutedText) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = BentoMutedText)
                        }
                    } else {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = "Filter", tint = BentoMutedText)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BentoSurface,
                    unfocusedContainerColor = BentoSurface,
                    focusedBorderColor = BentoPrimary.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = BentoDarkText,
                    unfocusedTextColor = BentoDarkText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("search_field")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BentoBg)
        ) {
            // Hero Promo Card inside Bento Dashboard
            item {
                FeaturedBanner(viewModel)
            }

            // Quick Actions / Categories Grid Blocks
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Menu (bg-purple)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(BentoPillMenu)
                            .clickable {
                                viewModel.selectedCategory = "All"
                                viewModel.searchQuery = ""
                                Toast.makeText(context, "Full list of dishes loaded!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = "Menu icon bento",
                                tint = BentoPillMenuText,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "MENU",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPillMenuText
                            )
                        }
                    }

                    // Favorites (bg-lilac)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(BentoPillFavorites)
                            .clickable {
                                if (favoriteList.isEmpty()) {
                                    Toast.makeText(context, "No favorites added yet! Tap hearts in menu.", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.selectedCategory = "Traditional Khaja"
                                    Toast.makeText(context, "Displaying Nepalese Specialties!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorites icon bento",
                                tint = BentoPillFavoritesText,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "FAVORITES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPillFavoritesText
                            )
                        }
                    }

                    // Coupons (bg-pink)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(BentoPillCoupons)
                            .clickable {
                                showCouponsDialog = true
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Loyalty,
                                contentDescription = "Coupons icon bento",
                                tint = BentoPillCouponsText,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "COUPONS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPillCouponsText
                            )
                        }
                    }
                }
            }

            // Recent Order / Tracking Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val orders by viewModel.ordersList.collectAsStateWithLifecycle(emptyList())
                    val lastOrder = orders.firstOrNull()

                    // Last Order / Reorder (cols-4 equivalent)
                    Card(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(115.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BentoBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "LAST ORDER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = lastOrder?.itemSummary ?: "No past orders yet",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoDarkText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = if (lastOrder != null) viewModel.formatDate(lastOrder.timestamp).take(10) else "",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lastOrder != null) "Rs. ${lastOrder.totalAmount.toInt()}" else "Bhojan Nepal",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoDarkText
                                )
                                if (lastOrder != null) {
                                    Button(
                                        onClick = {
                                            viewModel.reorderPastItems(lastOrder)
                                            Toast.makeText(context, "Added last order items to your cart!", Toast.LENGTH_SHORT).show()
                                            viewModel.currentScreen = AppScreen.Cart
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("REORDER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                } else {
                                    Text("Ready to order!", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Order Tracking (cols-2 equivalent)
                    val activeOrder = viewModel.activeTrackingOrder
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clickable {
                                if (activeOrder != null) {
                                    viewModel.currentScreen = AppScreen.OrderTracking
                                } else {
                                    Toast.makeText(context, "Start your Nepalese feast by placing an order!", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurface),
                        border = BorderStroke(1.dp, BentoStatusBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Tracking Icon bento",
                                tint = BentoPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "TRACKING",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoDarkText,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = activeOrder?.status ?: "No active delivery",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BentoPrimary,
                                    maxLines = 2,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Payment partners mini row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAY WITH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoMutedText,
                        letterSpacing = 1.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("eSewa", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF3E5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Khalti", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Card", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFECEFF1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("COD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                        }
                    }
                }
            }

            // Horizontal Categories list
            item {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = "Browse Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BentoDarkText,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(FoodMenu.categories) { category ->
                            val isSelected = viewModel.selectedCategory == category
                            val backgroundColor = if (isSelected) BentoPrimary else Color.White
                            val textColor = if (isSelected) Color.White else BentoDarkText
                            val borderAccent = if (isSelected) Color.Transparent else BentoBorderColor

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { viewModel.selectedCategory = category }
                                    .testTag("category_pill_$category"),
                                color = backgroundColor,
                                shape = RoundedCornerShape(20.dp),
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                border = BorderStroke(1.dp, borderAccent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (category) {
                                            "Combos" -> Icons.Default.Loyalty
                                            "Mo:Mo Special" -> Icons.Default.OutdoorGrill
                                            "Burgers & Pizza" -> Icons.Default.LunchDining
                                            "Traditional Khaja" -> Icons.Default.SetMeal
                                            "Main Course" -> Icons.Default.MenuBook
                                            "Drinks & Desserts" -> Icons.Default.LocalCafe
                                            else -> Icons.Default.RestaurantMenu
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else BentoPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = category,
                                        color = textColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Food Items Grid
            val filteredMenu = viewModel.getFilteredFoodItems()
            if (filteredMenu.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "No food found", modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No dishes match your criteria.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                item {
                    Text(
                        text = "${viewModel.selectedCategory} Menu Items",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = BentoDarkText,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
                    )
                }

                items(filteredMenu.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (item in pair) {
                            Box(modifier = Modifier.weight(1f)) {
                                FoodCard(item = item, viewModel = viewModel, favoriteList = favoriteList)
                            }
                        }
                        if (pair.size < 2) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun FeaturedBanner(viewModel: BhojanViewModel) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(165.dp),
        shape = RoundedCornerShape(24.dp), // Bento deep rounding
        colors = CardDefaults.cardColors(containerColor = BentoPromoBlue),
        elevation = CardDefaults.cardElevation(0.dp) // Flat modern bento feel
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Visual background glow decoration from HTML
                    drawCircle(
                        color = BentoPromoBlueGlow.copy(alpha = 0.5f),
                        radius = size.height * 0.7f,
                        center = Offset(size.width * 1.0f, size.height * 1.0f)
                    )
                }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.7f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .background(BentoPromoBlueText, shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "20% OFF ON KHAJA", 
                            fontSize = 8.sp, 
                            fontWeight = FontWeight.ExtraBold, 
                            color = Color.White
                        )
                    }
    
                    Spacer(modifier = Modifier.height(6.dp))
    
                    Text(
                        text = "NEPALESE FAMILY COMBO",
                        color = BentoPromoBlueText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "Use code: BHOJAN20 in checkout",
                        color = BentoPromoBlueText.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Text(
                    text = "Mo:Mo + Chowmein + Burger + Coke. A complete feast!",
                    color = BentoPromoBlueText.copy(alpha = 0.75f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Button(
                    onClick = {
                        val familyCombo = FoodMenu.menuList.find { it.id == "combo_family" }
                        familyCombo?.let {
                            viewModel.addItemToCart(it)
                            Toast.makeText(context, "Nepalese Combo Added! Coupon code applied.", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.couponInput = "BHOJAN20"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPromoBlueText),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("ADD TO CART", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            // Stylized food mock representation
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = "Combo Platter",
                    tint = BentoPromoBlueText,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun FoodCard(item: FoodItem, viewModel: BhojanViewModel, favoriteList: List<com.example.db.FavoriteEntity>) {
    val isFav = favoriteList.any { it.foodId == item.id }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                viewModel.selectedFoodItem = item
                viewModel.loadReviewsForFood(item.id)
                viewModel.currentScreen = AppScreen.FoodDetail
            }
            .testTag("food_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoBorderColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFEF7FF), Color(0xFFF3EDF7))
                        )
                    )
            ) {
                // Discount tag
                if (item.discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(BentoPrimary, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${item.discountPercent}% OFF",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Favorite Toggle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                        .clickable { viewModel.toggleFavoriteItem(item.id) }
                        .testTag("favorite_btn_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite toggle",
                        tint = if (isFav) BentoPrimary else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Standard clean icon representor
                Icon(
                    imageVector = when (item.category) {
                        "Combos" -> Icons.Default.CardGiftcard
                        "Mo:Mo Special" -> Icons.Default.RiceBowl
                        "Burgers & Pizza" -> Icons.Default.LocalPizza
                        "Drinks & Desserts" -> Icons.Default.Icecream
                        else -> Icons.Default.DinnerDining
                    },
                    contentDescription = null,
                    tint = BentoPrimary.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = BentoDarkText
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = BhojanGold, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "${item.rating} (${item.reviewCount})", fontSize = 10.sp, color = BentoMutedText)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (item.discountPercent > 0) {
                            Text(
                                text = "Rs. ${item.price.toInt()}",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                style = TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                        Text(
                            text = "Rs. ${item.discountedPrice.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = BentoPrimary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.addItemToCart(item) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = BentoPrimary),
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("add_to_cart_${item.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add to cart", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// --- SCREEN 3: FOOD DETAIL & REVIEW ENGINE ---
@Composable
fun FoodDetailScreen(viewModel: BhojanViewModel) {
    val item = viewModel.selectedFoodItem ?: return
    val reviews by viewModel.currentFoodReviews.collectAsStateWithLifecycle()
    var userReviewRating by remember { mutableStateOf(5) }
    var userReviewComment by remember { mutableStateOf("") }
    var reviewAuthorName by remember { mutableStateOf("Nepali Foodie") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BhojanCream)
    ) {
        // Picture area
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(BhojanRed.copy(alpha = 0.1f), Color.White)
                        )
                    )
            ) {
                IconButton(
                    onClick = { viewModel.currentScreen = AppScreen.Home },
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = BhojanRed)
                }

                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = BhojanRed.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                )

                // Category badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(BhojanGold, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(item.category, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Title and descriptions
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkGray,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { viewModel.toggleFavoriteItem(item.id) }) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite", tint = BhojanRed)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = BhojanGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${item.rating} Standard Rating", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkGray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "${item.reviewCount} customer ratings", fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Description",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Price checkout strip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Full Portion Price", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "Rs. ${item.discountedPrice.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BhojanRed
                            )
                        }

                        Button(
                            onClick = { viewModel.addItemToCart(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = BhojanRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("detail_add_cart")
                        ) {
                            Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to Cart")
                        }
                    }
                }
            }
        }

        // Ratings and review history list
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = "Customer Reviews & Ratings (ग्राहक समीक्षा र प्रतिक्रिया)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )
            }
        }

        if (reviews.isEmpty()) {
            item {
                Text(
                    "No user reviews yet. Be the first to share your dining experience!",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(reviews) { r ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(r.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkGray)
                            Row {
                                repeat(5) { i ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (i < r.rating) BhojanGold else Color.LightGray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(r.comment, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }

        // Write a review Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BhojanGold.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Express Your Experience (आफ्नो समीक्षा लेख्नुहोस्)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BhojanRed)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reviewAuthorName,
                        onValueChange = { reviewAuthorName = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Touch and Select Rating:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        repeat(5) { i ->
                            val starNum = i + 1
                            val isSelected = starNum <= userReviewRating
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = null,
                                tint = if (isSelected) BhojanGold else Color.Gray,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { userReviewRating = starNum }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userReviewComment,
                        onValueChange = { userReviewComment = it },
                        label = { Text("How was the spice, packaging and delivery?") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_comment_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.addFoodReview(item.id, reviewAuthorName, userReviewRating.toFloat(), userReviewComment)
                            userReviewComment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BhojanRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_submit")
                    ) {
                        Text("Submit Review")
                    }
                }
            }
        }
    }
}

// --- SCREEN 4: CART ---
@Composable
fun CartScreen(viewModel: BhojanViewModel, items: List<CartItemEntity>) {
    val context = LocalContext.current
    val subtotal = items.sumOf { it.discountedPrice * it.quantity }
    val appliedDiscount = viewModel.calcDiscountAmount(subtotal)
    val deliveryFee = if (subtotal > 0) 60.0 else 0.0
    val total = subtotal - appliedDiscount + deliveryFee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BhojanCream)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BhojanRed)
                .padding(16.dp)
        ) {
            Text(
                "My Shopping Cart (मेरो टोकरी)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = "Empty Basket", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Your cart is empty.", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkGray)
                Text("Browse Bhojan Nepal menus and add delicious dishes!", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.currentScreen = AppScreen.Home },
                    colors = ButtonDefaults.buttonColors(containerColor = BhojanRed)
                ) {
                    Text("Browse Food Items")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Cart Product Listings
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(BhojanRed.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = BhojanRed, modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Rs. ${item.discountedPrice.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = BhojanRed
                                    )
                                    if (item.discountPercent > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Rs. ${item.price.toInt()}",
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            style = TextStyle(
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Quantity manager
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .background(Color(0xFFEEEEEE), CircleShape)
                                        .clickable { viewModel.decrementCartQuantity(item) }
                                        .testTag("dec_qty_${item.foodId}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Reduce", tint = DarkGray, modifier = Modifier.size(14.dp))
                                }

                                Text(
                                    item.quantity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    color = DarkGray
                                )

                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .background(BhojanRed, CircleShape)
                                        .clickable { viewModel.incrementCartQuantity(item) }
                                        .testTag("inc_qty_${item.foodId}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // Coupon panel
                item {
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Coupons & Promo Codes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = viewModel.couponInput,
                                    onValueChange = { viewModel.couponInput = it },
                                    placeholder = { Text("E.g. NEPAL20") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("coupon_field")
                                )

                                Button(
                                    onClick = { viewModel.applyCouponCode(viewModel.couponInput, subtotal) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BhojanRed),
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Text("Apply")
                                }
                            }

                            if (viewModel.appliedCoupon != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Applied: ${viewModel.appliedCoupon?.code}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    IconButton(
                                        onClick = { viewModel.removeCoupon() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Tapeable quick coupons
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Tap to quickly apply coupon:", fontSize = 11.sp, color = Color.Gray)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                items(viewModel.availableCoupons) { coupon ->
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, BhojanGold, RoundedCornerShape(4.dp))
                                            .clickable { viewModel.applyCouponCode(coupon.code, subtotal) }
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            Text(coupon.code, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = BhojanRed)
                                            Text(coupon.description, fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Billing breakdown
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Food Basket Subtotal", color = Color.Gray)
                                Text("Rs. ${subtotal.toInt()}", fontWeight = FontWeight.SemiBold, color = DarkGray)
                            }
                            if (appliedDiscount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Campaign Coupon Discount", color = Color(0xFF2E7D32))
                                    Text("-Rs. ${appliedDiscount.toInt()}", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Kathmandu Valley Delivery Fee", color = Color.Gray)
                                Text("Rs. ${deliveryFee.toInt()}", fontWeight = FontWeight.SemiBold, color = DarkGray)
                            }
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total (जम्मा रकम)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BhojanRed)
                                Text("Rs. ${total.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BhojanRed)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Pay checkout bottom panel
            Surface(
                color = Color.White,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total to Deliver", fontSize = 11.sp, color = Color.Gray)
                        Text("Rs. ${total.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BhojanRed)
                    }

                    Button(
                        onClick = {
                            if (viewModel.isLoggedIn) {
                                viewModel.currentScreen = AppScreen.Checkout
                            } else {
                                viewModel.currentScreen = AppScreen.Login
                                Toast.makeText(context, "Registration/Login is required to checkout", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BhojanRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("checkout_proceed_btn")
                    ) {
                        Text("Place Food Order 🛵", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SCREEN 5: CHECKOUT (Location sensing Google Map, Nepal Payments) ---
@Composable
fun CheckoutScreen(viewModel: BhojanViewModel, items: List<CartItemEntity>) {
    val context = LocalContext.current
    var deliverNotes by remember { mutableStateOf("") }
    var addressInputStr by remember { mutableStateOf(viewModel.selectedLocation.addressLine) }
    var selectedPayBy by remember { mutableStateOf("COD") } // COD, eSewa, Khalti, Fonepay, Card

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.detectDeviceLocation { detectedStr ->
                addressInputStr = detectedStr
            }
        } else {
            Toast.makeText(context, "Location permission is required for auto-detection.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(viewModel.selectedLocation.addressLine) {
        addressInputStr = viewModel.selectedLocation.addressLine
    }

    val subtotal = items.sumOf { it.discountedPrice * it.quantity }
    val appliedDiscount = viewModel.calcDiscountAmount(subtotal)
    val deliveryFee = 60.0
    val totalAmount = subtotal - appliedDiscount + deliveryFee

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BhojanCream)
            .padding(12.dp)
    ) {
        item {
            Text("Shipping & Delivery Pinpoint", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkGray)
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Location sensing custom simulated Maps drawing
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                border = BorderStroke(1.dp, Color(0xFFDDDDDD))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // procedural vector canvas representing Kathmandu grid
                    CoordinateGoogleMapCanvas(
                        lat = viewModel.selectedLocation.latitude,
                        lon = viewModel.selectedLocation.longitude
                    )

                    // Overlay Map components
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                            .clickable(enabled = !viewModel.isDetectingLocation) {
                                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasFine || hasCoarse) {
                                    viewModel.detectDeviceLocation { detectedStr ->
                                        addressInputStr = detectedStr
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (viewModel.isDetectingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = BentoPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sensing GPS...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "GPS Sense",
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Detect Location (GPS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkGray)
                            }
                        }
                    }

                    // Floating marker pin in center
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Rider Destination PIN",
                        tint = BhojanRed,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }

        viewModel.locationErrorMsg?.let { error ->
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8)),
                    border = BorderStroke(1.dp, Color(0xFFF8B4B4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFF9B1C1C),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = error,
                            color = Color(0xFF9B1C1C),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = addressInputStr,
                onValueChange = {
                    addressInputStr = it
                    viewModel.selectedLocation = viewModel.selectedLocation.copy(addressLine = it)
                },
                label = { Text("Delivery Street Address (Kathmandu Valley)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("address_field")
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Payment Gateways (भुक्तानी विधि)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Nepali payment gateways selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // eSewa Card
                PaymentMethodCard(
                    id = "eSewa",
                    title = "eSewa Mobile Wallet",
                    subtitle = "Instant Nepali secure authentication response",
                    color = eSewaGreen,
                    isSelected = selectedPayBy == "eSewa",
                    onSelect = { selectedPayBy = "eSewa" }
                )

                // Khalti Card
                PaymentMethodCard(
                    id = "Khalti",
                    title = "Khalti Digital Wallet",
                    subtitle = "Verify with 4 digit secure PIN code",
                    color = KhaltiPurple,
                    isSelected = selectedPayBy == "Khalti",
                    onSelect = { selectedPayBy = "Khalti" }
                )

                // Fonepay QR
                PaymentMethodCard(
                    id = "Fonepay",
                    title = "Fonepay Direct Banking",
                    subtitle = "Mobile scan & transfer validation",
                    color = FonepayRed,
                    isSelected = selectedPayBy == "Fonepay",
                    onSelect = { selectedPayBy = "Fonepay" }
                )

                // Cash on Delivery
                PaymentMethodCard(
                    id = "COD",
                    title = "Cash on Delivery (COD)",
                    subtitle = "Pay cash or scan QR when rider arrives",
                    color = BhojanRed,
                    isSelected = selectedPayBy == "COD",
                    onSelect = { selectedPayBy = "COD" }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = deliverNotes,
                onValueChange = { deliverNotes = it },
                label = { Text("Special Rider Instructions (E.g. Call at gate)") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Display final tallies
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selected Method: $selectedPayBy", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkGray)
                    Text("Total billing: Rs. ${totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BhojanRed)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pay action
        item {
            Button(
                onClick = {
                    if (selectedPayBy == "COD") {
                        viewModel.placeBhojanOrder("Cash on Delivery", addressInputStr)
                    } else {
                        // Go to authentic gateway screen first
                        viewModel.activeTrackingOrder = OrderEntity(
                            orderId = "BN-" + (100000..999999).random().toString(),
                            totalAmount = totalAmount,
                            itemSummary = items.joinToString { "${it.name} x${it.quantity}" },
                            itemsData = items.joinToString(";") { "${it.foodId}|${it.name}|${it.price}|${it.quantity}|${it.discountPercent}" },
                            address = addressInputStr,
                            paymentMethod = selectedPayBy,
                            status = "Pending Payment",
                            timestamp = System.currentTimeMillis()
                        )
                        viewModel.currentScreen = AppScreen.Payment
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BhojanRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("place_order_button")
            ) {
                Text(
                    text = if (selectedPayBy == "COD") "Process Cash Order ✓" else "Verify via $selectedPayBy Wallet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PaymentMethodCard(
    id: String,
    title: String,
    subtitle: String,
    color: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("pay_card_$id"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) color else Color(0xFFDDDDDD)),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(id.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = color)
            )
        }
    }
}

// Procedural vector illustration drawing of Map lines, roads, locations of Kathmandu
@Composable
fun CoordinateGoogleMapCanvas(lat: Double, lon: Double) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw background grid lines
        val stroke = Stroke(width = 1.5f)
        val majorStroke = Stroke(width = 3f)

        // Draw light grey grids
        for (i in 0..size.width.toInt() step 60) {
            drawLine(Color(0xFFE2E2E2), Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), stroke.width)
        }
        for (j in 0..size.height.toInt() step 60) {
            drawLine(Color(0xFFE2E2E2), Offset(0f, j.toFloat()), Offset(size.width, j.toFloat()), stroke.width)
        }

        // Draw main Ringroad curves in Kathmandu style
        val path = Path().apply {
            moveTo(0f, size.height * 0.4f)
            quadraticTo(size.width * 0.5f, size.height * 0.1f, size.width, size.height * 0.5f)
        }
        drawPath(path, Color(0xFFB0BEC5), style = majorStroke)

        // Draw Bagmati River curves
        val riverPath = Path().apply {
            moveTo(size.width * 0.3f, 0f)
            quadraticTo(size.width * 0.4f, size.height * 0.6f, size.width * 0.1f, size.height)
        }
        drawPath(riverPath, Color(0xFF90CAF9), style = Stroke(width = 6f))

        // Draw some building blocks
        drawRect(Color(0xFFFFF176).copy(alpha = 0.5f), Offset(size.width * 0.1f, size.height * 0.15f), size = androidx.compose.ui.geometry.Size(90f, 60f))
        drawRect(Color(0xFFA5D6A7).copy(alpha = 0.5f), Offset(size.width * 0.6f, size.height * 0.6f), size = androidx.compose.ui.geometry.Size(120f, 80f))
    }
}

// --- SCREEN 6: PAYMENT SIMULATION GATEWAYS ---
@Composable
fun PaymentScreen(viewModel: BhojanViewModel) {
    val context = LocalContext.current
    val pendingOrder = viewModel.activeTrackingOrder
    val paymentName = pendingOrder?.paymentMethod ?: "Digital Portal"
    val orderTotal = pendingOrder?.totalAmount ?: 0.0

    var payerPhone by remember { mutableStateOf("") }
    var gatewayPin by remember { mutableStateOf("") }
    var verifyingState by remember { mutableStateOf(false) }

    val portalColor = when (paymentName) {
        "eSewa" -> eSewaGreen
        "Khalti" -> KhaltiPurple
        "Fonepay" -> FonepayRed
        else -> BhojanRed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo representation of portal
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(portalColor),
            contentAlignment = Alignment.Center
        ) {
            Text(paymentName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to $paymentName Nepal Gateway",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGray
        )

        Text(
            text = "Secure digital gateway authorization portal",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Amount frame
        Surface(
            color = portalColor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total to Pay (Bhojan Nepal):", fontSize = 13.sp, color = DarkGray, fontWeight = FontWeight.Bold)
                Text("Rs. ${orderTotal.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = portalColor)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!verifyingState) {
            OutlinedTextField(
                value = payerPhone,
                onValueChange = { payerPhone = it },
                label = { Text("$paymentName ID / Mobile Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pay_gateway_phone")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = gatewayPin,
                onValueChange = { gatewayPin = it },
                label = { Text("4-Digit Secure Gateway Password / PIN") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pay_gateway_pin")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (payerPhone.length < 8 || gatewayPin.length < 4) {
                        Toast.makeText(context, "Enter valid credentials", Toast.LENGTH_SHORT).show()
                    } else {
                        verifyingState = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = portalColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("pay_gateway_comfirm")
            ) {
                Text("Proceed Secure Checkout (OTP / PIN Verified)", fontWeight = FontWeight.Bold)
            }
        } else {
            // Processing delay
            var percent by remember { mutableStateOf(0.1f) }
            LaunchedEffect(Unit) {
                val duration = 4000
                val steps = 20
                for (i in 1..steps) {
                    delay(200)
                    percent = i.toFloat() / steps
                }
                // complete
                viewModel.placeBhojanOrder(paymentName, pendingOrder?.address ?: "")
            }

            CircularProgressIndicator(color = portalColor)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Connecting to $paymentName Secure Severs...", fontWeight = FontWeight.Bold, color = DarkGray)
            Text("Simulated transaction processing. Please do not close application", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

// --- SCREEN 7: ORDER TRACKING (Steps & Canvas Route) ---
@Composable
fun OrderTrackingScreen(viewModel: BhojanViewModel) {
    val order = viewModel.activeTrackingOrder ?: return
    val progressPercent = when (order.status) {
        "Order Placed" -> 0.2f
        "Confirmed" -> 0.4f
        "Preparing" -> 0.6f
        "Out for Delivery" -> 0.8f
        "Delivered" -> 1.0f
        else -> 0.2f
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BhojanCream)
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order Tracking (डेलिभरी ट्र्याकिङ)", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BhojanRed)
                    Text("Order ID: ${order.orderId}", fontSize = 13.sp, color = Color.Gray)
                }

                Box(
                    modifier = Modifier
                        .background(BhojanGold, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(order.status, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live Route representation on Kathmandu style maps
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                border = BorderStroke(1.dp, Color(0xFFDDDDDD))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing static Kathmandu roads and rivers
                        drawCircle(Color(0xFFE2E2E2).copy(alpha = 0.5f), radius = 200f, center = Offset(size.width * 0.2f, size.height * 0.3f))
                        drawLine(Color(0xFFB0BEC5), Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), strokeWidth = 4f)
                        drawLine(Color(0xFFB0BEC5), Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height), strokeWidth = 4f)

                        // Glowing Delivery route path
                        val routeP = Path().apply {
                            moveTo(size.width * 0.15f, size.height * 0.35f) // Bhojan Kitchen (Balaju)
                            lineTo(size.width * 0.5f, size.height * 0.35f)
                            lineTo(size.width * 0.5f, size.height * 0.75f) // User home destination
                        }
                        drawPath(routeP, Color.Gray, style = Stroke(width = 6f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)))
                        
                        // Active delivery motorcycle progress drawing
                        val riderOffset = Offset(
                            x = size.width * (0.15f + (0.5f - 0.15f) * progressPercent),
                            y = size.height * (0.35f + (0.75f - 0.35f) * progressPercent)
                        )
                        drawCircle(BhojanRed, radius = 9f, center = riderOffset)
                    }

                    // Labels Overlay
                    Text(
                        "Bhojan Kitchen",
                        color = BhojanRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 12.dp, top = 36.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                            .padding(2.dp)
                    )

                    Text(
                        "Your Home Address",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 24.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                            .padding(2.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery Status Tracker", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    TrackingStepColumn(currentStatus = order.status)
                }
            }
        }

        // Rider Profile Details
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BhojanRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.DirectionsBike, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bhojan Nepal Rider Assigned", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkGray)
                        Text("Pasang Tamang (9801xxxxxx)", fontSize = 11.sp, color = Color.Gray)
                    }
                    IconButton(
                        onClick = {
                            // Dial simulated phone call
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Call Rider", tint = BhojanRed)
                    }
                }
            }
        }

        // Manual simulator bypass for testing
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.manuallyProgressTracking() },
                colors = ButtonDefaults.buttonColors(containerColor = BhojanGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("manually_progress_btn")
            ) {
                Text("Speed Prep / Progress Delivery Stage (For Testing) ⚡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun TrackingStepColumn(currentStatus: String) {
    val steps = listOf("Order Placed", "Confirmed", "Preparing", "Out for Delivery", "Delivered")
    val currentIndex = steps.indexOf(currentStatus).coerceAtLeast(0)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        steps.forEachIndexed { idx, label ->
            val isActive = idx <= currentIndex
            val isCurrent = idx == currentIndex

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circle bullet
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (isActive) BhojanRed else Color.LightGray,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) DarkGray else Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = when (label) {
                            "Order Placed" -> "Received by Bhojan central dispatchers"
                            "Confirmed" -> "Chef approved fresh ticket setup"
                            "Preparing" -> "Cooking in central Kathmandu kitchens"
                            "Out for Delivery" -> "Delivery driver is rushing locally"
                            "Delivered" -> "Arrived safe! Ready for dining"
                            else -> ""
                        },
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// --- SCREEN 8: ORDER HISTORY ---
@Composable
fun OrderHistoryScreen(viewModel: BhojanViewModel) {
    val orders by viewModel.ordersList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BhojanCream)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BhojanRed)
                .padding(16.dp)
        ) {
            Text(
                "My Order History (मेरो अर्डर इतिहास)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (orders.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = "No History", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No previous transactions found.", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkGray)
                Text("Your placed orders will reside secure here.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                viewModel.activeTrackingOrder = order
                                viewModel.currentScreen = AppScreen.OrderTracking
                            }
                            .testTag("order_history_card_${order.orderId}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Order ID: ${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(viewModel.formatDate(order.timestamp), fontSize = 11.sp, color = Color.Gray)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(BhojanGold.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(order.status, color = BhojanRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp))

                            Text("Dishes Placed:", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = order.itemSummary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Address: ${order.address}", fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)

                            Divider(modifier = Modifier.padding(vertical = 10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Paid: Rs. ${order.totalAmount.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = BhojanRed
                                )

                                // Reorder Button re-constitutes items back into active database cart
                                Button(
                                    onClick = { viewModel.reorderPastItems(order) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BhojanRed),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("reorder_btn_${order.orderId}")
                                ) {
                                    Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reorder Food Items", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

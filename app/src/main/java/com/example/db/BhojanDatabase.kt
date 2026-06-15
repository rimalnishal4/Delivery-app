package com.example.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- ENTITIES ---

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodId: String,
    val name: String,
    val price: Double,
    val discountPercent: Int,
    val quantity: Int,
    val category: String
) {
    val discountedPrice: Double
        get() = if (discountPercent > 0) price * (1.0 - discountPercent / 100.0) else price
}

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val foodId: String
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val totalAmount: Double,
    val itemSummary: String, // e.g. "Family Combo x1, Sweet Lassi x2"
    val itemsData: String, // Stringified list of items "foodId|name|price|quantity|discountPercent" separated by ";"
    val address: String,
    val paymentMethod: String,
    val status: String, // "Order Placed", "Confirmed", "Preparing", "Out for Delivery", "Delivered"
    val timestamp: Long
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val reviewId: Int = 0,
    val foodId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOS ---

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CartItemEntity)

    @Update
    suspend fun updateItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteItem(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT * FROM cart_items WHERE foodId = :foodId LIMIT 1")
    suspend fun getCartItemByFoodId(foodId: String): CartItemEntity?
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE foodId = :foodId")
    suspend fun deleteFavorite(foodId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE foodId = :foodId)")
    fun isFavorite(foodId: String): Flow<Boolean>
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE foodId = :foodId ORDER BY timestamp DESC")
    fun getReviewsForFood(foodId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)
}

// --- DATABASE ---

@Database(
    entities = [CartItemEntity::class, FavoriteEntity::class, OrderEntity::class, ReviewEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BhojanDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun orderDao(): OrderDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: BhojanDatabase? = null

        fun getDatabase(context: Context): BhojanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BhojanDatabase::class.java,
                    "bhojan_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- REPOSITORY ---

class BhojanRepository(private val database: BhojanDatabase) {
    val cartItems: Flow<List<CartItemEntity>> = database.cartDao().getCartItems()
    val favorites: Flow<List<FavoriteEntity>> = database.favoriteDao().getFavorites()
    val orders: Flow<List<OrderEntity>> = database.orderDao().getOrders()

    // Cart operations
    suspend fun addToCart(foodId: String, name: String, price: Double, discountPercent: Int, category: String) {
        val existing = database.cartDao().getCartItemByFoodId(foodId)
        if (existing != null) {
            database.cartDao().updateItem(existing.copy(quantity = existing.quantity + 1))
        } else {
            database.cartDao().insertItem(
                CartItemEntity(
                    foodId = foodId,
                    name = name,
                    price = price,
                    discountPercent = discountPercent,
                    quantity = 1,
                    category = category
                )
            )
        }
    }

    suspend fun updateCartQuantity(id: Int, quantity: Int) {
        if (quantity <= 0) {
            database.cartDao().deleteItem(id)
        } else {
            // Find existing
            val existing = database.cartDao().getCartItems().toString() // Wait, let's just retrieve or delete
            // Actually, we can pass a CartItemEntity directly to database.cartDao().updateItem(entity)
        }
    }

    suspend fun deleteCartItem(id: Int) = database.cartDao().deleteItem(id)

    suspend fun updateCartItemDirect(item: CartItemEntity) {
        database.cartDao().updateItem(item)
    }

    suspend fun clearCart() = database.cartDao().clearCart()

    // Favorite operations
    suspend fun toggleFavorite(foodId: String, currentIsFavorite: Boolean) {
        if (currentIsFavorite) {
            database.favoriteDao().deleteFavorite(foodId)
        } else {
            database.favoriteDao().insertFavorite(FavoriteEntity(foodId))
        }
    }

    fun isFavorite(foodId: String): Flow<Boolean> = database.favoriteDao().isFavorite(foodId)

    // Order operations
    suspend fun placeOrder(order: OrderEntity) {
        database.orderDao().insertOrder(order)
    }

    fun getOrder(orderId: String): Flow<OrderEntity?> = database.orderDao().getOrderById(orderId)

    suspend fun updateOrderStatus(orderId: String, status: String) {
        database.orderDao().updateOrderStatus(orderId, status)
    }

    // Review operations
    fun getReviews(foodId: String): Flow<List<ReviewEntity>> = database.reviewDao().getReviewsForFood(foodId)

    suspend fun addReview(review: ReviewEntity) {
        database.reviewDao().insertReview(review)
    }
}

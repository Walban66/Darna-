package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "families")
data class Family(
    @PrimaryKey val id: String, // Invite Code is the ID
    val name: String
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val name: String,
    val phone: String,
    val role: String, // Admin or Member
    val familyId: String,
    val photoUri: String = "",
    val relation: String = "غير محدد"
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // cat_food, cat_cleaning, etc.
    val quantity: Double,
    val unit: String,
    val isPurchased: Boolean = false,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val expiryDate: String, // YYYY-MM-DD
    val barcode: String = "",
    val limitThreshold: Double = 2.0 // Low stock indicator
)

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val mealType: String, // Breakfast, Lunch, Dinner
    val name: String,
    val ingredients: String, // Comma-separated or instructions
    val instructions: String = ""
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val assignedMemberName: String,
    val dueDate: String, // YYYY-MM-DD
    val isCompleted: Boolean = false,
    val recurrence: String = "None" // None, Daily, Weekly
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String,
    val description: String,
    val date: String // YYYY-MM-DD
)

@Entity(tableName = "messages")
data class FamilyMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val senderRole: String,
    val text: String,
    val imageUri: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val updateType: String = "none" // none, shopping, task
)

@Entity(tableName = "shared_notes")
data class SharedNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val type: String, // inventory_low, expiry_warning, task_assigned, shopping, activity
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

// --- Combined SQLite Data Access Object (DAO) ---

@Dao
interface AppDao {

    // Families
    @Query("SELECT * FROM families")
    fun getAllFamilies(): Flow<List<Family>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: Family)

    @Query("SELECT * FROM families WHERE id = :id LIMIT 1")
    suspend fun getFamilyById(id: String): Family?

    // Users
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Shopping List
    @Query("SELECT * FROM shopping_items ORDER BY createdAt DESC")
    fun getShoppingItems(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)

    // Inventory
    @Query("SELECT * FROM inventory_items ORDER BY expiryDate ASC")
    fun getInventoryItems(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)

    // Meal Plans
    @Query("SELECT * FROM meal_plans ORDER BY date ASC")
    fun getMealPlans(): Flow<List<MealPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(meal: MealPlan)

    @Delete
    suspend fun deleteMealPlan(meal: MealPlan)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Family Chat Messages
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getMessages(): Flow<List<FamilyMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: FamilyMessage)

    // Shared Notes
    @Query("SELECT * FROM shared_notes ORDER BY createdAt DESC")
    fun getSharedNotes(): Flow<List<SharedNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedNote(note: SharedNote)

    @Delete
    suspend fun deleteSharedNote(note: SharedNote)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Delete
    suspend fun deleteNotification(notification: AppNotification)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    @Query("DELETE FROM shopping_items")
    suspend fun clearShoppingItems()

    @Query("DELETE FROM inventory_items")
    suspend fun clearInventoryItems()

    @Query("DELETE FROM meal_plans")
    suspend fun clearMealPlans()

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM messages")
    suspend fun clearMessages()

    @Query("DELETE FROM shared_notes")
    suspend fun clearSharedNotes()

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

// --- App Database Configuration ---

@Database(
    entities = [
        Family::class,
        User::class,
        ShoppingItem::class,
        InventoryItem::class,
        MealPlan::class,
        Task::class,
        Expense::class,
        FamilyMessage::class,
        SharedNote::class,
        AppNotification::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}

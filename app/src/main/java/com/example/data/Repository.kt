package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- Singleton Database Provider ---

object DatabaseProvider {
    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "darna_database"
            )
            .fallbackToDestructiveMigration()
            .build()
            database = instance
            instance
        }
    }
}

// --- App Repository ---

class FamilyRepository(private val appDao: AppDao) {

    // Exposure flows
    val families: Flow<List<Family>> = appDao.getAllFamilies()
    val users: Flow<List<User>> = appDao.getAllUsers()
    val shoppingItems: Flow<List<ShoppingItem>> = appDao.getShoppingItems()
    val inventoryItems: Flow<List<InventoryItem>> = appDao.getInventoryItems()
    val mealPlans: Flow<List<MealPlan>> = appDao.getMealPlans()
    val tasks: Flow<List<Task>> = appDao.getTasks()
    val expenses: Flow<List<Expense>> = appDao.getExpenses()
    val messages: Flow<List<FamilyMessage>> = appDao.getMessages()
    val sharedNotes: Flow<List<SharedNote>> = appDao.getSharedNotes()
    val notifications: Flow<List<AppNotification>> = appDao.getNotifications()

    // Seeds checker
    suspend fun checkAndSeedData() = withContext(Dispatchers.IO) {
        val familiesSample = appDao.getAllFamilies().firstOrNull() ?: emptyList()
        if (familiesSample.isEmpty()) {
            seedSampleDatabase()
        }
    }

    private suspend fun seedSampleDatabase() {
        val defaultCode = "FAMILY123"
        // Seed Family
        appDao.insertFamily(Family(defaultCode, "عائلة أحمد / Ahmad Household"))

        // Seed Users
        appDao.insertUser(User(name = "أحمد (الأب)", email = "ahmad@darna.com", phone = "0551112223", role = "Admin", familyId = defaultCode))
        appDao.insertUser(User(name = "نور (الأم)", email = "nour@darna.com", phone = "0554445556", role = "Member", familyId = defaultCode))

        // Seed Shopping
        appDao.insertShoppingItem(ShoppingItem(name = "حليب طويل الأجل", category = "cat_food", quantity = 4.0, unit = "عبوة", createdBy = "نور (الأم)"))
        appDao.insertShoppingItem(ShoppingItem(name = "أرز بسمتي طازج", category = "cat_food", quantity = 5.0, unit = "كجم", createdBy = "أحمد (الأب)"))
        appDao.insertShoppingItem(ShoppingItem(name = "سائل غسيل الأطباق", category = "cat_cleaning", quantity = 1.0, unit = "حبة", createdBy = "نور (الأم)"))
        appDao.insertShoppingItem(ShoppingItem(name = "خبز توست أسمر", category = "cat_food", quantity = 2.0, unit = "كيس", isPurchased = true, createdBy = "أحمد (الأب)"))

        // Seed Inventory
        appDao.insertInventoryItem(InventoryItem(name = "صدور دجاج مجمدة", category = "cat_food", quantity = 2.5, unit = "كجم", expiryDate = "2026-08-15", limitThreshold = 1.0))
        appDao.insertInventoryItem(InventoryItem(name = "زيت طبخ نباتي", category = "cat_food", quantity = 0.5, unit = "لتر", expiryDate = "2026-11-20", limitThreshold = 1.5))
        appDao.insertInventoryItem(InventoryItem(name = "معجون أسنان بالنعناع", category = "cat_personal", quantity = 1.0, unit = "حبة", expiryDate = "2026-06-03", limitThreshold = 2.0)) // Near expiry!
        appDao.insertInventoryItem(InventoryItem(name = "ليمون طازج", category = "cat_food", quantity = 1.5, unit = "كجم", expiryDate = "2026-06-07", limitThreshold = 2.0)) // Low stock alert!

        // Seed Meal Plans
        appDao.insertMealPlan(MealPlan(date = "2026-06-02", mealType = "Breakfast", name = "توست أسمر بيض مسلوق", ingredients = "بيض، توست أسمر، زيت زيتون", instructions = "سلق البيض، وتحميص الخبز وتزيينها بزيت الزيتون"))
        appDao.insertMealPlan(MealPlan(date = "2026-06-02", mealType = "Lunch", name = "مكبوس دجاج بالسبانخ", ingredients = "دجاج مجمد، أرز، بهارات عربية", instructions = "طهي الدجاج والبهارات ثم خلط الأرز في مرق الدجاج"))
        appDao.insertMealPlan(MealPlan(date = "2026-06-02", mealType = "Dinner", name = "شوربة الخضار الخفيفة", ingredients = "بطاطس، جزر، ليمون طازج", instructions = "تقطيع الخضروات وسلقها في ماء ساخن وإضافة الليمون"))

        // Seed Tasks
        appDao.insertTask(Task(title = "تنظيف الأواني وترتيب المطبخ", description = "تنظيف وغسل كافة رفوف التخزين وترتيب الليمون الجديد", assignedMemberName = "أحمد (الأب)", dueDate = "2026-06-03", isCompleted = true, recurrence = "None"))
        appDao.insertTask(Task(title = "شراء معجون الأسنان والزيت", description = "يرجى شراء المستلزمات المنتهية الصلاحية من المتجر", assignedMemberName = "نور (الأم)", dueDate = "2026-06-04", isCompleted = false, recurrence = "Weekly"))

        // Seed Expenses
        appDao.insertExpense(Expense(amount = 230.50, category = "cat_food", description = "باقة الخضروات واللحوم الأسبوعية", date = "2026-06-01"))
        appDao.insertExpense(Expense(amount = 45.00, category = "cat_cleaning", description = "شراء صابون وورق تنشيف", date = "2026-05-28"))
        appDao.insertExpense(Expense(amount = 90.00, category = "cat_other", description = "مستلزمات صيانة لمقبض باب المطبخ", date = "2026-05-25"))

        // Seed Messages
        appDao.insertMessage(FamilyMessage(senderName = "أحمد (الأب)", senderRole = "Admin", text = "أهلاً ومرحباً بكم مع تطبيق دارنا الذكي! هنا سننسق الأنشطة المشتركة للأسرة."))
        appDao.insertMessage(FamilyMessage(senderName = "نور (الأم)", senderRole = "Member", text = "رائع جداً! سأقوم بتسجيل المنتجات المفقودة الآن وتحديث مخازن الطعام."))

        // Seed Shared Note
        appDao.insertSharedNote(SharedNote(title = "مواعيد استلام فواتير الخدمات العامة", content = "تسليم فواتير المياه والكهرباء في الخامس والعشرين من كل شهر هجري، الرجاء مراجعة الموزع مباشرة.", createdBy = "أحمد (الأب)"))

        // Seed Notifications
        appDao.insertNotification(AppNotification(title = "تحذير انتهاء الصلاحية", message = "معجون أسنان بالنعناع تنتهي صلاحيته في 2026-06-03!", type = "expiry_warning"))
        appDao.insertNotification(AppNotification(title = "تنبيه نقص السلع", message = "ليمون طازج أقل من عتبة الحد الأدنى المطلوب لشؤون المطبخ!", type = "inventory_low"))
    }

    // --- Action implementations ---

    suspend fun createFamily(familyName: String): String = withContext(Dispatchers.IO) {
        val uniqueCode = (100000..999999).random().toString()
        appDao.insertFamily(Family(uniqueCode, familyName))
        uniqueCode
    }

    suspend fun insertFamily(family: Family) = withContext(Dispatchers.IO) {
        appDao.insertFamily(family)
    }

    suspend fun joinFamily(inviteCode: String): Boolean = withContext(Dispatchers.IO) {
        val family = appDao.getFamilyById(inviteCode)
        return@withContext family != null
    }

    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        appDao.insertUser(user)
    }

    suspend fun insertShoppingItem(item: ShoppingItem) = withContext(Dispatchers.IO) {
        appDao.insertShoppingItem(item)
    }

    suspend fun updateShoppingItem(item: ShoppingItem) = withContext(Dispatchers.IO) {
        appDao.insertShoppingItem(item)
    }

    suspend fun deleteShoppingItem(item: ShoppingItem) = withContext(Dispatchers.IO) {
        appDao.deleteShoppingItem(item)
    }

    suspend fun insertInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) {
        appDao.insertInventoryItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) {
        appDao.insertInventoryItem(item)
    }

    suspend fun deleteInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) {
        appDao.deleteInventoryItem(item)
    }

    suspend fun insertMealPlan(plan: MealPlan) = withContext(Dispatchers.IO) {
        appDao.insertMealPlan(plan)
    }

    suspend fun deleteMealPlan(plan: MealPlan) = withContext(Dispatchers.IO) {
        appDao.deleteMealPlan(plan)
    }

    suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        appDao.insertTask(task)
    }

    suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        appDao.deleteTask(task)
    }

    suspend fun insertExpense(expense: Expense) = withContext(Dispatchers.IO) {
        appDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        appDao.deleteExpense(expense)
    }

    suspend fun insertMessage(msg: FamilyMessage) = withContext(Dispatchers.IO) {
        appDao.insertMessage(msg)
    }

    suspend fun insertSharedNote(note: SharedNote) = withContext(Dispatchers.IO) {
        appDao.insertSharedNote(note)
    }

    suspend fun deleteSharedNote(note: SharedNote) = withContext(Dispatchers.IO) {
        appDao.deleteSharedNote(note)
    }

    suspend fun insertNotification(notification: AppNotification) = withContext(Dispatchers.IO) {
        appDao.insertNotification(notification)
    }

    suspend fun deleteNotification(notification: AppNotification) = withContext(Dispatchers.IO) {
        appDao.deleteNotification(notification)
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        appDao.clearAllNotifications()
    }

    suspend fun clearAndReplaceShopping(items: List<ShoppingItem>) = withContext(Dispatchers.IO) {
        appDao.clearShoppingItems()
        items.forEach { appDao.insertShoppingItem(it) }
    }

    suspend fun clearAndReplaceInventory(items: List<InventoryItem>) = withContext(Dispatchers.IO) {
        appDao.clearInventoryItems()
        items.forEach { appDao.insertInventoryItem(it) }
    }

    suspend fun clearAndReplaceMealPlans(items: List<MealPlan>) = withContext(Dispatchers.IO) {
        appDao.clearMealPlans()
        items.forEach { appDao.insertMealPlan(it) }
    }

    suspend fun clearAndReplaceTasks(items: List<Task>) = withContext(Dispatchers.IO) {
        appDao.clearTasks()
        items.forEach { appDao.insertTask(it) }
    }

    suspend fun clearAndReplaceExpenses(items: List<Expense>) = withContext(Dispatchers.IO) {
        appDao.clearExpenses()
        items.forEach { appDao.insertExpense(it) }
    }

    suspend fun clearAndReplaceMessages(items: List<FamilyMessage>) = withContext(Dispatchers.IO) {
        appDao.clearMessages()
        items.forEach { appDao.insertMessage(it) }
    }

    suspend fun clearAndReplaceSharedNotes(items: List<SharedNote>) = withContext(Dispatchers.IO) {
        appDao.clearSharedNotes()
        items.forEach { appDao.insertSharedNote(it) }
    }

    suspend fun clearAndReplaceUsers(items: List<User>) = withContext(Dispatchers.IO) {
        appDao.clearUsers()
        items.forEach { appDao.insertUser(it) }
    }
}

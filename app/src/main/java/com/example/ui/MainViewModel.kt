package com.example.ui

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Screen states
sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Login : Screen()
    object Register : Screen()
    object ForgotPassword : Screen()
    object FamilySetup : Screen()
    object MainApp : Screen()
}

// Bottom tab states
enum class Tab {
    DASHBOARD,
    SHOPPING,
    INVENTORY,
    MEALS,
    TASKS,
    CHAT,
    EXPENSES,
    NOTIFICATIONS,
    PROFILE,
    SETTINGS,
    ABOUT
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FamilyRepository
    private val TAG = "MainViewModel"

    // App state flows
    var currentScreen by mutableStateOf<Screen>(Screen.Splash)
    var currentTab by mutableStateOf<Tab>(Tab.DASHBOARD)
    
    // Core parameters (synchronized from Database repository)
    val shoppingList = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val inventoryList = MutableStateFlow<List<InventoryItem>>(emptyList())
    val mealPlanner = MutableStateFlow<List<MealPlan>>(emptyList())
    val taskList = MutableStateFlow<List<Task>>(emptyList())
    val expenseList = MutableStateFlow<List<Expense>>(emptyList())
    val chatMessages = MutableStateFlow<List<FamilyMessage>>(emptyList())
    val sharedNotes = MutableStateFlow<List<SharedNote>>(emptyList())
    val notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val familyList = MutableStateFlow<List<Family>>(emptyList())
    val userList = MutableStateFlow<List<User>>(emptyList())

    // Active Authenticated user session (persisted via SharedPreferences)
    var currentUserEmail by mutableStateOf("")
    var currentUserName by mutableStateOf("")
    var currentUserPhone by mutableStateOf("")
    var currentUserRole by mutableStateOf("") // Admin or Member
    var currentFamilyId by mutableStateOf("")
    var currentFamilyName by mutableStateOf("")

    // Settings States
    var isDarkMode by mutableStateOf(false)
    var budgetLimit by mutableStateOf(1000.0) // State initialized limit
    
    // Country Feature States (Algeria by default)
    var currentCountry by mutableStateOf("الجزائر")
    var currentCity by mutableStateOf("الجزائر العاصمة")
    
    // Cloud Sync States For Firebase Realtime Database
    var syncStatus by mutableStateOf("idle") // "idle", "syncing", "done", "failed"
    var lastSyncTimestamp by mutableStateOf(0L)

    // AI Operation States
    var aiMealResult by mutableStateOf("")
    var isGeneratingMeals by mutableStateOf(false)
    var aiBudgetResult by mutableStateOf("")
    var isGeneratingBudget by mutableStateOf(false)

    // Scanner States
    var scannedBarcode by mutableStateOf("")
    var isScannerActive by mutableStateOf(false)

    // Internet connectivity state
    var isOnline by mutableStateOf(true)

    private val prefs = application.getSharedPreferences("darna_prefs", android.content.Context.MODE_PRIVATE)

    fun saveUserSession() {
        prefs.edit().apply {
            putString("user_email", currentUserEmail)
            putString("user_name", currentUserName)
            putString("user_phone", currentUserPhone)
            putString("user_role", currentUserRole)
            putString("family_id", currentFamilyId)
            putString("family_name", currentFamilyName)
            apply()
        }
    }

    private fun loadUserSession() {
        currentUserEmail = prefs.getString("user_email", "") ?: ""
        currentUserName = prefs.getString("user_name", "") ?: ""
        currentUserPhone = prefs.getString("user_phone", "") ?: ""
        currentUserRole = prefs.getString("user_role", "") ?: ""
        currentFamilyId = prefs.getString("family_id", "") ?: ""
        currentFamilyName = prefs.getString("family_name", "") ?: ""
    }

    private fun clearUserSession() {
        prefs.edit().clear().apply()
    }

    private fun monitorNetworkStatus() {
        try {
            val connectivityManager = getApplication<Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            // Initial check
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        isOnline = true
                    }

                    override fun onLost(network: Network) {
                        isOnline = false
                    }
                })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error monitoring network: ${e.message}", e)
            isOnline = true
        }
    }

    init {
        loadUserSession()
        monitorNetworkStatus()
        val database = DatabaseProvider.getDatabase(application)
        repository = FamilyRepository(database.appDao())

        // Launch in-database verification and seeding
        viewModelScope.launch {
            try {
                repository.checkAndSeedData()
                observeDatabase()
                startCloudPolling()
            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed: ${e.message}", e)
            }
        }
    }

    private var pollingJob: kotlinx.coroutines.Job? = null

    fun startCloudPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(6000)
                if (currentFamilyId.isNotBlank()) {
                    syncWithCloud(silent = true)
                }
            }
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.shoppingItems.collect { 
                shoppingList.value = it 
                if (!isSyncingFromCloud) {
                    triggerBackgroundPush()
                }
            }
        }
        viewModelScope.launch {
            repository.inventoryItems.collect { 
                inventoryList.value = it 
                // Expiration logic trigger check
                checkInventoryAlerts(it)
                if (!isSyncingFromCloud) {
                    triggerBackgroundPush()
                }
            }
        }
        viewModelScope.launch {
            repository.mealPlans.collect { 
                mealPlanner.value = it 
                if (!isSyncingFromCloud) {
                    triggerBackgroundPush()
                }
            }
        }
        viewModelScope.launch {
            repository.tasks.collect { 
                taskList.value = it 
                if (!isSyncingFromCloud) {
                    triggerBackgroundPush()
                }
            }
        }
        viewModelScope.launch {
            repository.expenses.collect { 
                expenseList.value = it 
                if (!isSyncingFromCloud) {
                    triggerBackgroundPush()
                }
            }
        }
        viewModelScope.launch {
            repository.messages.collect { 
                chatMessages.value = it 
                if (!isSyncingFromCloud) {
                    triggerBackgroundPush()
                }
            }
        }
        viewModelScope.launch {
            repository.sharedNotes.collect { 
                sharedNotes.value = it 
                if (!isSyncingFromCloud) {
                    triggerBackgroundPush()
                }
            }
        }
        viewModelScope.launch {
            repository.notifications.collect { notifications.value = it }
        }
        viewModelScope.launch {
            repository.families.collect { families ->
                familyList.value = families
                families.find { it.id == currentFamilyId }?.let {
                    currentFamilyName = it.name
                }
            }
        }
        viewModelScope.launch {
            repository.users.collect { users ->
                userList.value = users
                if (!isSyncingFromCloud && currentFamilyId.isNotBlank()) {
                    val myFamilyMembers = users.filter { it.familyId == currentFamilyId }
                    if (myFamilyMembers.isNotEmpty()) {
                        FirebaseSyncService.pushFamilyMembers(currentFamilyId, myFamilyMembers)
                    }
                }
            }
        }
    }

    private fun checkInventoryAlerts(items: List<InventoryItem>) {
        viewModelScope.launch {
            items.forEach { item ->
                // Low stock condition
                if (item.quantity <= item.limitThreshold) {
                    val alertMsg = "السلعة '${item.name}' شارفت على النفاد المتبقي (${item.quantity} ${item.unit})!"
                    if (notifications.value.none { it.message == alertMsg }) {
                        repository.insertNotification(AppNotification(
                            title = "تنبيه نقص السلعة",
                            message = alertMsg,
                            type = "inventory_low"
                        ))
                    }
                }
            }
        }
    }

    fun getCurrencySymbol(): String {
        return when (currentCountry) {
            "الجزائر" -> if (Localization.isArabic) "د.ج" else "DZD"
            "السعودية" -> if (Localization.isArabic) "ر.س" else "SAR"
            "مصر" -> if (Localization.isArabic) "ج.م" else "EGP"
            "الإمارات" -> if (Localization.isArabic) "د.إ" else "AED"
            else -> if (Localization.isArabic) "د.ج" else "DZD"
        }
    }

    var isSyncingFromCloud = false

    fun syncWithCloud(silent: Boolean = false) {
        if (currentFamilyId.isBlank()) return
        if (!silent) {
            syncStatus = "syncing"
        }
        viewModelScope.launch {
            try {
                isSyncingFromCloud = true

                // 1. Shopping List
                val cloudShopping = FirebaseSyncService.pullShoppingItems(currentFamilyId)
                if (cloudShopping != null) {
                    if (cloudShopping != shoppingList.value) {
                        try {
                            if (shoppingList.value.isNotEmpty() && cloudShopping.size > shoppingList.value.size) {
                                val added = cloudShopping.takeLast(cloudShopping.size - shoppingList.value.size)
                                added.forEach { item ->
                                    if (item.createdBy != currentUserName) {
                                        NotificationHelper.showNotification(
                                            getApplication(),
                                            "إضافة إلى المشتريات 🛒",
                                            "تم إضافة '${item.name}' بواسطة أحد أفراد العائلة."
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) { Log.e(TAG, "Notification difference logic error", e) }
                        repository.clearAndReplaceShopping(cloudShopping)
                    }
                } else {
                    FirebaseSyncService.pushShoppingItems(currentFamilyId, shoppingList.value)
                }

                // 2. Inventory Items
                val cloudInventory = FirebaseSyncService.pullInventoryItems(currentFamilyId)
                if (cloudInventory != null) {
                    if (cloudInventory != inventoryList.value) {
                        repository.clearAndReplaceInventory(cloudInventory)
                    }
                } else {
                    FirebaseSyncService.pushInventoryItems(currentFamilyId, inventoryList.value)
                }

                // 3. Meal plans
                val cloudMeals = FirebaseSyncService.pullMealPlans(currentFamilyId)
                if (cloudMeals != null) {
                    if (cloudMeals != mealPlanner.value) {
                        try {
                            if (mealPlanner.value.isNotEmpty() && cloudMeals.size > mealPlanner.value.size) {
                                val added = cloudMeals.takeLast(cloudMeals.size - mealPlanner.value.size)
                                added.forEach { meal ->
                                    NotificationHelper.showNotification(
                                        getApplication(),
                                        "جدولة وجبة عائلية جديدة 🍽️",
                                        "العائلة أضافت وجبة '${meal.name}' لجدول الوجبات!"
                                    )
                                }
                            }
                        } catch (e: Exception) { Log.e(TAG, "Meal Notification error", e) }
                        repository.clearAndReplaceMealPlans(cloudMeals)
                    }
                } else {
                    FirebaseSyncService.pushMealPlans(currentFamilyId, mealPlanner.value)
                }

                // 4. Tasks
                val cloudTasks = FirebaseSyncService.pullTasks(currentFamilyId)
                if (cloudTasks != null) {
                    if (cloudTasks != taskList.value) {
                        try {
                            if (taskList.value.isNotEmpty() && cloudTasks.size > taskList.value.size) {
                                val added = cloudTasks.takeLast(cloudTasks.size - taskList.value.size)
                                added.forEach { task ->
                                    if (task.assignedMemberName == currentUserName || task.assignedMemberName == "الكل / All") {
                                        NotificationHelper.showNotification(
                                            getApplication(),
                                            "مهمة عائلية مسندة إليك 📋",
                                            "اسم المهمة: ${task.title}"
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) { Log.e(TAG, "Task Notification error", e) }
                        repository.clearAndReplaceTasks(cloudTasks)
                    }
                } else {
                    FirebaseSyncService.pushTasks(currentFamilyId, taskList.value)
                }

                // 5. Expenses
                val cloudExpenses = FirebaseSyncService.pullExpenses(currentFamilyId)
                if (cloudExpenses != null) {
                    if (cloudExpenses != expenseList.value) {
                        repository.clearAndReplaceExpenses(cloudExpenses)
                    }
                } else {
                    FirebaseSyncService.pushExpenses(currentFamilyId, expenseList.value)
                }

                // 6. Messages
                val cloudMessages = FirebaseSyncService.pullMessages(currentFamilyId)
                if (cloudMessages != null) {
                    if (cloudMessages != chatMessages.value) {
                        try {
                            if (chatMessages.value.isNotEmpty() && cloudMessages.size > chatMessages.value.size) {
                                val added = cloudMessages.takeLast(cloudMessages.size - chatMessages.value.size)
                                added.forEach { msg ->
                                    if (msg.senderName != currentUserName && msg.senderRole != "System") {
                                        NotificationHelper.showNotification(
                                            getApplication(),
                                            "رسالة عائلية جديدة من ${msg.senderName} 💬",
                                            msg.text
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) { Log.e(TAG, "Message Notification error", e) }
                        repository.clearAndReplaceMessages(cloudMessages)
                    }
                } else {
                    FirebaseSyncService.pushMessages(currentFamilyId, chatMessages.value)
                }

                // 7. Shared Notes
                val cloudNotes = FirebaseSyncService.pullNotes(currentFamilyId)
                if (cloudNotes != null) {
                    if (cloudNotes != sharedNotes.value) {
                        repository.clearAndReplaceSharedNotes(cloudNotes)
                    }
                } else {
                    FirebaseSyncService.pushNotes(currentFamilyId, sharedNotes.value)
                }

                // 8. Family Members List
                var cloudUsers = FirebaseSyncService.pullFamilyMembers(currentFamilyId)?.toMutableList()
                if (cloudUsers != null) {
                    if (currentUserEmail.isNotBlank()) {
                        val hasMe = cloudUsers.any { it.email.trim().equals(currentUserEmail.trim(), ignoreCase = true) }
                        if (!hasMe) {
                            val me = User(
                                name = currentUserName,
                                email = currentUserEmail,
                                phone = currentUserPhone,
                                role = currentUserRole,
                                familyId = currentFamilyId
                            )
                            cloudUsers.add(me)
                            FirebaseSyncService.pushFamilyMembers(currentFamilyId, cloudUsers)
                        }
                    }

                    if (cloudUsers.isNotEmpty()) {
                        val me = cloudUsers.find { it.email.trim().equals(currentUserEmail.trim(), ignoreCase = true) }
                        if (me != null && me.role != currentUserRole) {
                            currentUserRole = me.role
                            prefs.edit().putString("user_role", me.role).apply()
                        }
                        
                        val otherUsers = userList.value.filter { !it.email.trim().equals(currentUserEmail.trim(), ignoreCase = true) && it.familyId != currentFamilyId }
                        val merged = otherUsers + cloudUsers
                        val uniqueMerged = merged.distinctBy { it.email.trim().lowercase() }
                        repository.clearAndReplaceUsers(uniqueMerged)
                    }
                } else {
                    val myFamilyMembers = userList.value.filter { it.familyId == currentFamilyId }.toMutableList()
                    val hasMe = myFamilyMembers.any { it.email.trim().equals(currentUserEmail.trim(), ignoreCase = true) }
                    if (!hasMe && currentUserEmail.isNotBlank()) {
                        myFamilyMembers.add(User(
                            name = currentUserName,
                            email = currentUserEmail,
                            phone = currentUserPhone,
                            role = currentUserRole,
                            familyId = currentFamilyId
                        ))
                    }
                    if (myFamilyMembers.isNotEmpty()) {
                        FirebaseSyncService.pushFamilyMembers(currentFamilyId, myFamilyMembers)
                        val otherUsers = userList.value.filter { it.familyId != currentFamilyId }
                        repository.clearAndReplaceUsers(otherUsers + myFamilyMembers)
                    }
                }

                if (!silent) {
                    syncStatus = "done"
                    lastSyncTimestamp = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Sync error: ${e.message}", e)
                if (!silent) {
                    syncStatus = "failed"
                }
            } finally {
                isSyncingFromCloud = false
            }
        }
    }

    fun triggerBackgroundPush() {
        if (currentFamilyId.isBlank()) return
        viewModelScope.launch {
            try {
                FirebaseSyncService.pushShoppingItems(currentFamilyId, shoppingList.value)
                FirebaseSyncService.pushInventoryItems(currentFamilyId, inventoryList.value)
                FirebaseSyncService.pushMealPlans(currentFamilyId, mealPlanner.value)
                FirebaseSyncService.pushTasks(currentFamilyId, taskList.value)
                FirebaseSyncService.pushExpenses(currentFamilyId, expenseList.value)
                FirebaseSyncService.pushMessages(currentFamilyId, chatMessages.value)
                FirebaseSyncService.pushNotes(currentFamilyId, sharedNotes.value)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Background push error: ${e.message}")
            }
        }
    }

    // --- Authentication Actions ---

    fun login(email: String, phone: String, onCompleted: (Boolean) -> Unit) {
        if (email.isNotBlank()) {
            currentUserEmail = email
            val defaultPhone = phone.ifBlank { "0554445556" }
            currentUserPhone = defaultPhone

            viewModelScope.launch {
                try {
                    // Check if user exists on Firebase Database first!
                    val onlineUser = FirebaseSyncService.pullUser(email)
                    if (onlineUser != null) {
                        currentUserName = onlineUser.name
                        currentUserEmail = onlineUser.email
                        currentUserPhone = onlineUser.phone.ifBlank { defaultPhone }
                        currentUserRole = onlineUser.role
                        currentFamilyId = onlineUser.familyId

                        // Insert into local Room database
                        repository.insertUser(onlineUser)

                        // If they belong to a family, pull that family's metadata too!
                        if (currentFamilyId.isNotBlank()) {
                            val onlineFamily = FirebaseSyncService.pullFamily(currentFamilyId)
                            if (onlineFamily != null) {
                                currentFamilyName = onlineFamily.name
                                repository.insertFamily(onlineFamily)
                            } else {
                                currentFamilyName = "العائلة النشطة"
                            }
                            // Perform cloud synchronization to download all family data immediately!
                            syncWithCloud()
                        }
                    } else {
                        // Check if they exist in the local Room databases as fallback (e.g. seeded accounts)
                        val existing = userList.value.find { it.email == email }
                        if (existing != null) {
                            currentUserName = existing.name
                            currentUserRole = existing.role
                            currentFamilyId = existing.familyId

                            // Sync profile to Firebase so it is saved online too!
                            FirebaseSyncService.syncUser(existing)

                            if (currentFamilyId.isNotBlank()) {
                                val onlineFamily = FirebaseSyncService.pullFamily(currentFamilyId)
                                if (onlineFamily == null) {
                                    // Save their seeded family metadata online too!
                                    familyList.value.find { it.id == currentFamilyId }?.let {
                                        FirebaseSyncService.syncFamily(it)
                                    }
                                }
                                syncWithCloud()
                            }
                        } else {
                            // Brand new user login (acts as registration fallback)
                            currentUserName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                            currentUserRole = "Member"
                            currentFamilyId = "" // Direct them to family setup

                            val newUser = User(
                                name = currentUserName,
                                email = email,
                                phone = currentUserPhone,
                                role = currentUserRole,
                                familyId = ""
                            )
                            repository.insertUser(newUser)
                            FirebaseSyncService.syncUser(newUser)
                        }
                    }

                    saveUserSession()
                    if (currentFamilyId.isNotBlank()) {
                        currentScreen = Screen.MainApp
                    } else {
                        currentScreen = Screen.FamilySetup
                    }
                    onCompleted(true)
                } catch (e: Exception) {
                    Log.e(TAG, "Login sync error: ${e.message}", e)
                    onCompleted(false)
                }
            }
        } else {
            onCompleted(false)
        }
    }

    fun register(name: String, email: String, phone: String, onCompleted: (Boolean) -> Unit) {
        if (name.isNotBlank() && email.isNotBlank()) {
            currentUserName = name
            currentUserEmail = email
            currentUserPhone = phone
            currentUserRole = "Admin" // Registrant defaults to creating/having admin rights
            currentFamilyId = "" // Direct to family creation screen first

            viewModelScope.launch {
                try {
                    val newUser = User(
                        name = name,
                        email = email,
                        phone = phone,
                        role = currentUserRole,
                        familyId = ""
                    )
                    repository.insertUser(newUser)

                    // Write user online!
                    FirebaseSyncService.syncUser(newUser)

                    saveUserSession()
                    currentScreen = Screen.FamilySetup
                    onCompleted(true)
                } catch (e: Exception) {
                    Log.e(TAG, "Registration sync error: ${e.message}", e)
                    onCompleted(false)
                }
            }
        } else {
            onCompleted(false)
        }
    }

    fun logout() {
        currentUserEmail = ""
        currentUserName = ""
        currentUserRole = "Member"
        currentFamilyId = ""
        currentFamilyName = ""
        clearUserSession()
        currentScreen = Screen.Login
    }

    fun updateUserRole(user: User, newRole: String) {
        viewModelScope.launch {
            try {
                val updated = user.copy(role = newRole)
                repository.insertUser(updated)
                
                // Write user online
                FirebaseSyncService.syncUser(updated)
                
                if (user.email == currentUserEmail) {
                    currentUserRole = newRole
                    prefs.edit().putString("user_role", newRole).apply()
                }
                
                // Fetch/push updated family user roster to keep everything in sync
                val updatedMembers = userList.value.filter { it.familyId == currentFamilyId }.map {
                    if (it.email == user.email) it.copy(role = newRole) else it
                }
                FirebaseSyncService.pushFamilyMembers(currentFamilyId, updatedMembers)
                
                // Trigger quick background sync to broadcast changes
                syncWithCloud(silent = true)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user role: ${e.message}", e)
            }
        }
    }

    fun updateUserRelation(user: User, newRelation: String) {
        viewModelScope.launch {
            try {
                val updated = user.copy(relation = newRelation)
                repository.insertUser(updated)
                
                // Write user online
                FirebaseSyncService.syncUser(updated)
                
                // Fetch/push updated family user roster to keep everything in sync
                val updatedMembers = userList.value.filter { it.familyId == currentFamilyId }.map {
                    if (it.email == user.email) it.copy(relation = newRelation) else it
                }
                FirebaseSyncService.pushFamilyMembers(currentFamilyId, updatedMembers)
                
                // Trigger quick background sync to broadcast changes
                syncWithCloud(silent = true)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user relation: ${e.message}", e)
            }
        }
    }

    // --- Family Management Actions ---

    fun createFamily(familyName: String) {
        viewModelScope.launch {
            try {
                val code = repository.createFamily(familyName)
                currentFamilyId = code
                currentFamilyName = familyName
                currentUserRole = "Admin"

                // Push family metadata online to Firebase!
                val newFamily = Family(code, familyName)
                FirebaseSyncService.syncFamily(newFamily)

                // Update local user Profile
                val updatedUser = User(
                    name = currentUserName,
                    email = currentUserEmail,
                    phone = currentUserPhone,
                    role = "Admin",
                    familyId = code
                )
                repository.insertUser(updatedUser)

                // Update user profile online on Firebase!
                FirebaseSyncService.syncUser(updatedUser)

                // Push chat welcome log
                repository.insertMessage(FamilyMessage(
                    senderName = "دارنا الذكي",
                    senderRole = "Admin",
                    text = "مبارك! تم تأسيس عائلة '$familyName' بنجاح. رمز المشاركة الدعوي هو: $code"
                ))

                saveUserSession()
                currentScreen = Screen.MainApp
            } catch (e: Exception) {
                Log.e(TAG, "Error creating family: ${e.message}", e)
            }
        }
    }

    fun joinFamily(inviteCode: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // First, check Firebase Realtime Database for this family
                val onlineFamily = FirebaseSyncService.pullFamily(inviteCode)
                if (onlineFamily != null) {
                    currentFamilyId = inviteCode
                    currentFamilyName = onlineFamily.name
                    currentUserRole = "Member"

                    // Cache Family object locally in Room
                    repository.insertFamily(onlineFamily)

                    // Create/update user object
                    val joinedUser = User(
                        name = currentUserName,
                        email = currentUserEmail,
                        phone = currentUserPhone,
                        role = "Member",
                        familyId = inviteCode
                    )
                    repository.insertUser(joinedUser)

                    // Sync user profile with Firebase
                    FirebaseSyncService.syncUser(joinedUser)

                    // Send member updates to chat
                    repository.insertMessage(FamilyMessage(
                        senderName = "دارنا الذكي",
                        senderRole = "Member",
                        text = "انضم العضو الجديد '$currentUserName' لمجلس العائلة التنسيقي الموحد!"
                    ))

                    // Sync all family lists, meals, etc. from Firebase Realtime Database to local Room DB
                    syncWithCloud()

                    saveUserSession()
                    currentScreen = Screen.MainApp
                    onResult(true)
                } else {
                    // Local fallback
                    val existsLocally = repository.joinFamily(inviteCode)
                    if (existsLocally) {
                        currentFamilyId = inviteCode
                        currentUserRole = "Member"
                        val familyObj = familyList.value.find { it.id == inviteCode }
                        currentFamilyName = familyObj?.name ?: "عائلة جديدة"

                        val joinedUser = User(
                            name = currentUserName,
                            email = currentUserEmail,
                            phone = currentUserPhone,
                            role = "Member",
                            familyId = inviteCode
                        )
                        repository.insertUser(joinedUser)
                        FirebaseSyncService.syncUser(joinedUser)

                        familyObj?.let { FirebaseSyncService.syncFamily(it) }

                        repository.insertMessage(FamilyMessage(
                            senderName = "دارنا الذكي",
                            senderRole = "Member",
                            text = "انضم العضو الجديد '$currentUserName' لمجلس العائلة التنسيقي الموحد!"
                        ))

                        saveUserSession()
                        currentScreen = Screen.MainApp
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Join family sync error: ${e.message}", e)
                onResult(false)
            }
        }
    }

    // --- Shopping Management Actions ---

    fun addShoppingItem(name: String, category: String, quantity: Double, unit: String) {
        viewModelScope.launch {
            repository.insertShoppingItem(ShoppingItem(
                name = name,
                category = category,
                quantity = quantity,
                unit = unit,
                createdBy = currentUserName
            ))
            
            // Notification updates
            pushAppActivityNotification(
                title = "إضافة مسودة مشتريات",
                msg = "أضاف $currentUserName منتج '$name' لقائمتنا الموحدة.",
                type = "shopping"
            )
        }
    }

    fun toggleShoppingItemPurchased(item: ShoppingItem) {
        viewModelScope.launch {
            val updated = item.copy(isPurchased = !item.isPurchased)
            repository.updateShoppingItem(updated)
            
            if (updated.isPurchased) {
                // Synchronize system updates in general chat
                repository.insertMessage(FamilyMessage(
                    senderName = "تم الشراء ✔️",
                    senderRole = "System",
                    text = "قام $currentUserName بشراء وإنجاز طلب السلعة: ${item.name} (${item.quantity} ${item.unit})",
                    updateType = "shopping"
                ))

                // Suggest inserting it inside available Kitchen inventory
                repository.insertInventoryItem(InventoryItem(
                    name = item.name,
                    category = item.category,
                    quantity = item.quantity,
                    unit = item.unit,
                    expiryDate = "2026-06-30", // Placeholder default for ease
                    limitThreshold = 1.0
                ))
            }
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    // --- Kitchen Inventory Management Actions ---

    fun addInventoryItem(name: String, category: String, quantity: Double, unit: String, expiry: String, barcode: String = "") {
        viewModelScope.launch {
            repository.insertInventoryItem(InventoryItem(
                name = name,
                category = category,
                quantity = quantity,
                unit = unit,
                expiryDate = expiry.ifBlank { "YYYY-MM-DD" },
                barcode = barcode,
                limitThreshold = 1.0
            ))

            pushAppActivityNotification(
                title = "إضافة بند مخزون",
                msg = "أضاف $currentUserName منتج '$name' بمستودع التخزين المكتبي للمنزل.",
                type = "activity"
            )
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.updateInventoryItem(item)
        }
    }

    // --- Barcode Scanner Simulation Handler ---

    fun triggerSimulateBarcodeScan(barcode: String) {
        scannedBarcode = barcode
        isScannerActive = false

        // Automatically resolve item properties matching barcode
        viewModelScope.launch {
            val (resolvedName, resolvedCategory) = when (barcode) {
                "6281013002" -> "عصير التفاح الطبيعي المراعي" to "cat_food"
                "4005900135" -> "كريم مرطب البشرة نيفيا" to "cat_personal"
                "3251241123" -> "مناديل مبللة مخصصة لأسطح الفولاذ" to "cat_cleaning"
                else -> "منتج باركود مخصص $barcode" to "cat_other"
            }

            addInventoryItem(
                name = resolvedName,
                category = resolvedCategory,
                quantity = 1.0,
                unit = "علبة",
                expiry = "2027-02-14",
                barcode = barcode
            )
        }
    }

    // --- Meal Planner Actions ---

    fun planMeal(date: String, mealType: String, name: String, ingredients: String, instructions: String) {
        viewModelScope.launch {
            repository.insertMealPlan(MealPlan(
                date = date,
                mealType = mealType,
                name = name,
                ingredients = ingredients,
                instructions = instructions
            ))
        }
    }

    fun deletePlannedMeal(meal: MealPlan) {
        viewModelScope.launch {
            repository.deleteMealPlan(meal)
        }
    }

    // AI Suggestions calling logic

    fun requestAIMealSuggestions() {
        if (isGeneratingMeals) return
        isGeneratingMeals = true
        aiMealResult = ""

        viewModelScope.launch {
            try {
                val outcome = GeminiService.generateMealSuggestions(
                    language = if (Localization.isArabic) "ar" else "en",
                    inventoryItems = inventoryList.value
                )
                aiMealResult = outcome
            } catch (e: Exception) {
                aiMealResult = "فشل توليد الاقتراحات: ${e.message}"
            } finally {
                isGeneratingMeals = false
            }
        }
    }

    // Apply specific generated meals onto meal planner
    fun applyAISuggestionToPlanner(name: String, ingredients: String, instructions: String) {
        viewModelScope.launch {
            planMeal(
                date = "2026-06-02", // Today's dynamic placeholder index
                mealType = "Lunch",
                name = name,
                ingredients = ingredients,
                instructions = instructions
            )
        }
    }

    fun shareMealToChat(mealName: String, ingredients: String, instructions: String? = null) {
        viewModelScope.launch {
            val detailsText = buildString {
                append("🍲 أقترح وجبة عائلية جديدة للمطالعة: [$mealName]")
                if (ingredients.isNotBlank()) {
                    append("\n• المقادير: $ingredients")
                }
                if (!instructions.isNullOrBlank()) {
                    append("\n• إعداد: $instructions")
                }
                append("\nما رأيكم يا عائلتي؟ شاركوني بالتعليقات والآراء! 👍👩‍🍳")
            }
            val msg = FamilyMessage(
                senderName = currentUserName,
                senderRole = currentUserRole,
                text = detailsText
            )
            repository.insertMessage(msg)
            if (currentFamilyId.isNotBlank()) {
                FirebaseSyncService.pushMessages(currentFamilyId, chatMessages.value)
            }
        }
    }

    // --- Tasks Management Actions ---

    fun addTask(title: String, description: String, assignedName: String, dueDate: String, recurrence: String) {
        viewModelScope.launch {
            val taskObj = Task(
                title = title,
                description = description,
                assignedMemberName = assignedName,
                dueDate = dueDate,
                recurrence = recurrence,
                isCompleted = false
            )
            repository.insertTask(taskObj)

            // Notify assignee and fire phone system notification ONLY if current user is the actual recipient/assignee
            pushAppActivityNotification(
                title = "مهمّة جديدة مسندة 📋",
                msg = "قام $currentUserName بإسناد مهمة '$title' لعضو العائلة $assignedName.",
                type = "task_assigned",
                showSystemAlert = (assignedName.trim() == currentUserName.trim() || assignedName.trim() == "الكل / All" || assignedName.contains("الكل"))
            )

            // Chat notifier
            repository.insertMessage(FamilyMessage(
                senderName = "مهام العائلة 📋",
                senderRole = "System",
                text = "أسند $currentUserName مهمة '$title' إلى $assignedName لتنفيذها قبل تاريخ $dueDate",
                updateType = "task"
            ))
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.insertTask(updated)

            if (updated.isCompleted) {
                repository.insertMessage(FamilyMessage(
                    senderName = "إنجاز المهام 🎉",
                    senderRole = "System",
                    text = "أتمَّ $currentUserName بنجاح مهام البيت الخاصة بـ: ${task.title}!",
                    updateType = "task"
                ))
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Expense Management Actions ---

    fun addExpense(amount: Double, category: String, description: String, date: String) {
        viewModelScope.launch {
            repository.insertExpense(Expense(
                amount = amount,
                category = category,
                description = description,
                date = date
            ))

            pushAppActivityNotification(
                title = "المصروفات العائلية",
                msg = "تم قيد صرفية جديدة قيمتها $amount ريال للفئة وصفي بـ '$description'.",
                type = "activity"
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun requestAIBudgetInsights() {
        if (isGeneratingBudget) return
        isGeneratingBudget = true
        aiBudgetResult = ""

        viewModelScope.launch {
            try {
                val outcome = GeminiService.generateBudgetSuggestions(
                    language = if (Localization.isArabic) "ar" else "en",
                    expenses = expenseList.value,
                    monthlyLimit = budgetLimit
                )
                aiBudgetResult = outcome
            } catch (e: Exception) {
                aiBudgetResult = "فشل توليد التحليلات: ${e.message}"
            } finally {
                isGeneratingBudget = false
            }
        }
    }

    // --- Chat & Notebook Actions ---

    fun sendChatMessage(text: String, photoUri: String = "") {
        if (text.isNotBlank() || photoUri.isNotBlank()) {
            viewModelScope.launch {
                repository.insertMessage(FamilyMessage(
                    senderName = currentUserName,
                    senderRole = currentUserRole,
                    text = text,
                    imageUri = photoUri
                ))
            }
        }
    }

    fun addSharedNote(title: String, content: String) {
        viewModelScope.launch {
            repository.insertSharedNote(SharedNote(
                title = title,
                content = content,
                createdBy = currentUserName
            ))
        }
    }

    fun deleteSharedNote(note: SharedNote) {
        viewModelScope.launch {
            repository.deleteSharedNote(note)
        }
    }

    // --- Notifications Management Flow ---

    fun removeNotification(noti: AppNotification) {
        viewModelScope.launch {
            repository.deleteNotification(noti)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    // --- Auxiliary Helpers ---

    private suspend fun pushAppActivityNotification(title: String, msg: String, type: String, showSystemAlert: Boolean = false) {
        repository.insertNotification(AppNotification(
            title = title,
            message = msg,
            type = type
        ))
        // Trigger OS notification only if explicit showSystemAlert is true (meaning the current user is a target recipient)
        if (showSystemAlert) {
            try {
                NotificationHelper.showNotification(getApplication(), title, msg)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show OS notification: ${e.message}", e)
            }
        }
    }
}

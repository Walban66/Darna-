package com.example.data

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object FirebaseSyncService {
    private const val TAG = "FirebaseSyncService"
    private const val BASE_URL = "https://darna-a3c1e-default-rtdb.europe-west1.firebasedatabase.app"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun sanitizeEmail(email: String): String {
        return email.replace(".", "_").replace("@", "_")
    }

    suspend fun syncUser(user: User): Boolean {
        return putData("users/${sanitizeEmail(user.email)}/profile", user, User::class.java)
    }

    suspend fun pullUser(email: String): User? {
        return getData("users/${sanitizeEmail(email)}/profile", User::class.java)
    }

    suspend fun pullFamily(familyId: String): Family? {
        return getData("families/$familyId/meta", Family::class.java)
    }

    // Generic helper to get a single object from Firebase
    private suspend fun <T> getData(path: String, clazz: Class<T>): T? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/$path.json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed getting data from $path: Code ${response.code}")
                    return@withContext null
                }
                val bodyStr = response.body?.string() ?: return@withContext null
                if (bodyStr == "null" || bodyStr.isBlank()) {
                    return@withContext null
                }
                val adapter = moshi.adapter(clazz)
                return@withContext adapter.fromJson(bodyStr)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting data from $path", e)
            return@withContext null
        }
    }

    // Generic helper to put data to Firebase
    private suspend fun <T> putData(path: String, data: T, clazz: Class<T>): Boolean = withContext(Dispatchers.IO) {
        try {
            val adapter = moshi.adapter(clazz)
            val json = adapter.toJson(data)
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$BASE_URL/$path.json")
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed putting data to $path: Code ${response.code}, message ${response.message}")
                }
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error putting data to $path", e)
            return@withContext false
        }
    }

    // Generic helper to put list of data
    private suspend fun <T> putList(path: String, dataList: List<T>, itemClazz: Class<T>): Boolean = withContext(Dispatchers.IO) {
        try {
            val type = Types.newParameterizedType(List::class.java, itemClazz)
            val adapter = moshi.adapter<List<T>>(type)
            val json = adapter.toJson(dataList)
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$BASE_URL/$path.json")
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed putting list to $path: Code ${response.code}")
                }
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error putting list to $path", e)
            return@withContext false
        }
    }

    // Generic helper to get list of data
    private suspend fun <T> getList(path: String, itemClazz: Class<T>): List<T>? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/$path.json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed getting list from $path: Code ${response.code}")
                    return@withContext null
                }
                val bodyStr = response.body?.string() ?: return@withContext null
                if (bodyStr == "null" || bodyStr.isBlank()) {
                    return@withContext emptyList()
                }

                val type = Types.newParameterizedType(List::class.java, itemClazz)
                val adapter = moshi.adapter<List<T>>(type)
                return@withContext adapter.fromJson(bodyStr)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting list from $path", e)
            return@withContext null
        }
    }

    // Synchronize family general definitions
    suspend fun syncFamily(family: Family): Boolean {
        return putData("families/${family.id}/meta", family, Family::class.java)
    }

    // List Push Sync
    suspend fun pushShoppingItems(familyId: String, items: List<ShoppingItem>): Boolean {
        if (familyId.isBlank()) return false
        return putList("families/$familyId/shopping_items", items, ShoppingItem::class.java)
    }

    suspend fun pushInventoryItems(familyId: String, items: List<InventoryItem>): Boolean {
        if (familyId.isBlank()) return false
        return putList("families/$familyId/inventory_items", items, InventoryItem::class.java)
    }

    suspend fun pushMealPlans(familyId: String, plans: List<MealPlan>): Boolean {
        if (familyId.isBlank()) return false
        return putList("families/$familyId/meal_plans", plans, MealPlan::class.java)
    }

    suspend fun pushTasks(familyId: String, tasks: List<Task>): Boolean {
        if (familyId.isBlank()) return false
        return putList("families/$familyId/tasks", tasks, Task::class.java)
    }

    suspend fun pushExpenses(familyId: String, expenses: List<Expense>): Boolean {
        if (familyId.isBlank()) return false
        return putList("families/$familyId/expenses", expenses, Expense::class.java)
    }

    suspend fun pushMessages(familyId: String, msg: List<FamilyMessage>): Boolean {
        if (familyId.isBlank()) return false
        return putList("families/$familyId/messages", msg, FamilyMessage::class.java)
    }

    suspend fun pushNotes(familyId: String, notes: List<SharedNote>): Boolean {
        if (familyId.isBlank()) return false
        return putList("families/$familyId/shared_notes", notes, SharedNote::class.java)
    }

    // List Pull Sync
    suspend fun pullShoppingItems(familyId: String): List<ShoppingItem>? {
        if (familyId.isBlank()) return null
        return getList("families/$familyId/shopping_items", ShoppingItem::class.java)
    }

    suspend fun pullInventoryItems(familyId: String): List<InventoryItem>? {
        if (familyId.isBlank()) return null
        return getList("families/$familyId/inventory_items", InventoryItem::class.java)
    }

    suspend fun pullMealPlans(familyId: String): List<MealPlan>? {
        if (familyId.isBlank()) return null
        return getList("families/$familyId/meal_plans", MealPlan::class.java)
    }

    suspend fun pullTasks(familyId: String): List<Task>? {
        if (familyId.isBlank()) return null
        return getList("families/$familyId/tasks", Task::class.java)
    }

    suspend fun pullExpenses(familyId: String): List<Expense>? {
        if (familyId.isBlank()) return null
        return getList("families/$familyId/expenses", Expense::class.java)
    }

    suspend fun pullMessages(familyId: String): List<FamilyMessage>? {
        if (familyId.isBlank()) return null
        return getList("families/$familyId/messages", FamilyMessage::class.java)
    }

    suspend fun pullNotes(familyId: String): List<SharedNote>? {
        if (familyId.isBlank()) return null
        return getList("families/$familyId/shared_notes", SharedNote::class.java)
    }

    suspend fun pushFamilyMembers(familyId: String, members: List<User>): Boolean {
        if (familyId.isBlank()) return false
        return putList("families/$familyId/members", members, User::class.java)
    }

    suspend fun pullFamilyMembers(familyId: String): List<User>? {
        if (familyId.isBlank()) return null
        return getList("families/$familyId/members", User::class.java)
    }
}

package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun getGeminiResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is empty or placeholder!")
            return@withContext "Error: API Key is not set in Secrets. Please add GEMINI_API_KEY to execute this feature."
        }

        // Clean any double quotes inside the prompt to ensure JSON validity
        val escapedPrompt = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")

        val jsonBody = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "$escapedPrompt"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Unsuccessful response from Gemini API: ${response.code} - $bodyString")
                    return@withContext "Error details from Gemini API (${response.code}): $bodyString"
                }

                // Highly robust direct parsing of text candidate
                // This shields us from complex data model serialization mismatches
                try {
                    val candidateStart = bodyString.indexOf("\"text\":")
                    if (candidateStart != -1) {
                        val textStartValue = bodyString.indexOf("\"", candidateStart + 7)
                        if (textStartValue != -1) {
                            // Find matching ending unescaped quote
                            var textEndValue = -1
                            var isEscaped = false
                            for (i in (textStartValue + 1) until bodyString.length) {
                                val char = bodyString[i]
                                if (isEscaped) {
                                    isEscaped = false
                                    continue
                                }
                                if (char == '\\') {
                                    isEscaped = true
                                    continue
                                }
                                if (char == '"') {
                                    textEndValue = i
                                    break
                                }
                            }
                            if (textEndValue != -1) {
                                val extractedContent = bodyString.substring(textStartValue + 1, textEndValue)
                                return@withContext unescapeJsonString(extractedContent)
                            }
                        }
                    }
                    return@withContext "Could not extract text candidate response from Gemini API outcome. Output: $bodyString"
                } catch (pe: Exception) {
                    Log.e(TAG, "Failed parsing text candidate: ${pe.message}")
                    return@withContext "Result output parser failed. Raw: $bodyString"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during call: ${e.message}", e)
            return@withContext "Network interaction failed: ${e.message ?: "Unknown Error"}"
        }
    }

    private fun unescapeJsonString(input: String): String {
        return input
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    // --- High-level prompt generators ---

    suspend fun generateMealSuggestions(
        language: String,
        inventoryItems: List<InventoryItem>
    ): String {
        val inventoryStr = if (inventoryItems.isEmpty()) {
            "No ingredients available in kitchen storage now."
        } else {
            inventoryItems.joinToString("\n") { 
                "- ${it.name} (${it.quantity} ${it.unit}), Expires: ${it.expiryDate}, Category: ${it.category}" 
            }
        }

        val prompt = """
            You are Darna AI (دارنا الذكي), an expert household culinary chef specialized in authentic Algerian cuisine and food waste prevention AI assistant.
            The user’s kitchen inventory currently includes these ingredients:
            $inventoryStr

            Tasks:
            1. Suggest 3 authentic Algerian family recipes (Breakfast, Lunch, Dinner) such as Couscous (كسكسي), Chorba Frik (شربة فريك), Tajine (طاجين زيتون/برقوق), Rechta (رشتة), Chakshouka (شكشوكة جزائرية حارة), Harira (حريرة), or Baghrir (بغرير) that directly maximize the use of the above ingredients or adapt them beautifully to traditional Algerian household cooking.
            2. Warn about any ingredients that are nearing expiration or already expired, and suggest using them first in these Algerian dishes.
            3. Highlight any auxiliary ingredients that are NOT in the inventory but are required to make these Algerian meals, formulating them as a list of shopping suggestions.
            4. Keep the output extremely encouraging, visually structured, and helpful for a proud Algerian home.

            Format the instructions in ${if (language == "ar") "Arabic only" else "English only"}.
            Use Material Design markdown format with clean lists, clear headers, and icons.
        """.trimIndent()

        return getGeminiResponse(prompt)
    }

    suspend fun generateBudgetSuggestions(
        language: String,
        expenses: List<Expense>,
        monthlyLimit: Double
    ): String {
        val expensesStr = if (expenses.isEmpty()) {
            "No recorded expenses for the month yet."
        } else {
            expenses.joinToString("\n") { 
                "- Category: ${it.category}, Amount: ${it.amount}, Desc: ${it.description}, Date: ${it.date}" 
            }
        }

        val prompt = """
            You are Darna AI Finance Advisor (دارنا المالي الذكي), an expert family budgeting and cost reduction AI consultant.
            Current Monthly Limit: $monthlyLimit
            Expenses recorded so far:
            $expensesStr

            Tasks:
            1. Perform a compact analysis of the expenses. Sum up category costs.
            2. Detect any anomalies, excessive spending categories, or opportunities for family thrift.
            3. Evaluate remaining budget capacity if a limit exists.
            4. Provide 3 specific, highly actionable cost-cutting recommendations for this family to reduce unnecessary spending in their home.

            Format the instructions in ${if (language == "ar") "Arabic only" else "English only"}.
            Keep it clear, professional, and structurally organized using clean headers and Material emojis.
        """.trimIndent()

        return getGeminiResponse(prompt)
    }
}

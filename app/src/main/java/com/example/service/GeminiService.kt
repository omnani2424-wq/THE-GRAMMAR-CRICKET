package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.model.GrammarQuestion
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// Moshi mappings for Gemini Request and Response
@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// The target parsed output structure
@JsonClass(generateAdapter = true)
data class GeminiGrammarQuestion(
    @Json(name = "questionText") val questionText: String,
    @Json(name = "options") val options: List<String>,
    @Json(name = "correctIndex") val correctIndex: Int,
    @Json(name = "category") val category: String,
    @Json(name = "explanation") val explanation: String
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Flag indicating whether API is configured with a valid key
    val isApiKeyAvailable: Boolean
        get() = try {
            val key = BuildConfig.GEMINI_API_KEY
            key != "MY_GEMINI_API_KEY" && key.isNotBlank()
        } catch (e: Exception) {
            false
        }

    suspend fun fetchDynamicGrammarQuestion(): GrammarQuestion? = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable) {
            Log.w(TAG, "Gemini API key is not configured. Falling back to offline mode.")
            return@withContext null
        }

        val categories = listOf(
            "Subject-Verb Agreement",
            "Subjunctive Mood",
            "Conditional Sentences",
            "Inversion",
            "Dangling Modifiers",
            "Parallelism",
            "Relative Clauses"
        )
        val selectedCategory = categories.random()
        val useCricketContext = Random.nextBoolean()

        val contextInstruction = if (useCricketContext) {
            "Inject fun cricket or IPL-themed vocabulary (batsman, stadium, boundary, wickets, bowler, clean-bowled)."
        } else {
            "Set the context randomly (e.g., cricket, business, literature, sport, science)."
        }

        val prompt = """
            Generate exactly one advanced, challenging English grammar quiz question at a high level (e.g. GMAT, SAT, GRE, CAT level).
            Topic: $selectedCategory.
            $contextInstruction
            Provide exactly 4 multiple choice options.
            Ensure one and only one is completely correct.
            Provide a detailed detailed explanation of why the correct option is grammatically sound, and why others are incorrect.
            
            Return your response EXCLUSIVELY as a JSON object, matching the following schema exactly (no enclosing markdown or code-blocks, just raw JSON):
            {
              "questionText": "A sentence containing a blank space represented by ________.",
              "options": ["A", "B", "C", "D"],
              "correctIndex": 0,
              "category": "$selectedCategory",
              "explanation": "Explanation here..."
            }
        """.trimIndent()

        val requestPayload = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 1.0f
            ),
            systemInstruction = Content(parts = listOf(Part(text = "You are an expert English grammar professor and premium sports game writer. Your goal is to write flawless grammar questions representing advanced rules like inversion or subjunctive mood.")))
        )

        try {
            val jsonAdapter = moshi.adapter(GeminiRequest::class.java)
            val requestBodyJson = jsonAdapter.toJson(requestPayload)

            val url = "$BASE_URL?key=${BuildConfig.GEMINI_API_KEY}"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: code = ${response.code}, message = ${response.message}")
                    return@withContext null
                }

                val responseBodyStr = response.body?.string() ?: return@withContext null
                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val geminiResponse = responseAdapter.fromJson(responseBodyStr)

                val contentText = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (contentText != null) {
                    val questionAdapter = moshi.adapter(GeminiGrammarQuestion::class.java)
                    // clean potential triple backticks response if markdown wraps it
                    val cleanJson = contentText.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val result = questionAdapter.fromJson(cleanJson)
                    if (result != null) {
                        return@withContext GrammarQuestion(
                            id = "dynamic_${System.currentTimeMillis()}",
                            questionText = result.questionText,
                            options = result.options,
                            correctIndex = result.correctIndex,
                            category = result.category,
                            explanation = result.explanation
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Gemini API: ${e.message}", e)
        }
        return@withContext null
    }
}

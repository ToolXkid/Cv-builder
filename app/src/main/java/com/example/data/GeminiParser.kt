package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.model.CvData
import com.example.model.Education
import com.example.model.WorkExperience
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiParser {
    private const val TAG = "GeminiParser"
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // OkHttpClient with generous timeouts for LLM responses
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Parses unstructured resume text into a structured CvData object using Gemini 3.5 Flash.
     */
    suspend fun parseResumeText(rawText: String): CvData? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured.")
            return@withContext null
        }

        val prompt = """
            You are an expert CV Parser AI. Analyze the following raw text of a resume/CV and extract all details.
            Format the response strictly as a JSON object matching this schema exactly:
            {
              "name": "Full Name",
              "title": "Professional Title (e.g., Software Engineer)",
              "email": "Email address",
              "phone": "Phone number",
              "location": "Location (City, Country)",
              "linkedin": "LinkedIn profile link or username",
              "summary": "Brief professional summary of the user",
              "experience": [
                {
                  "company": "Company Name",
                  "title": "Job Title",
                  "startDate": "Start Date (e.g. June 2021)",
                  "endDate": "End Date or 'Present'",
                  "description": "Short bullet points or description of duties"
                }
              ],
              "education": [
                {
                  "institution": "School or University Name",
                  "degree": "Degree earned (e.g., Bachelor of Science)",
                  "year": "Graduation year or date range"
                }
              ],
              "skills": ["Skill1", "Skill2", "Skill3"]
            }
            Do not include any markdown format tags like ```json or prefix text. Return only the JSON object.
            
            Raw Resume/CV Text:
            $rawText
        """.trimIndent()

        try {
            // Build Gemini REST Request Body
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)
                
                // Configure JSON structured output
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            
            val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            
            val request = Request.Builder()
                .url(requestUrl)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API call failed with code: ${response.code}")
                    return@withContext null
                }

                val responseBodyStr = response.body?.string() ?: return@withContext null
                Log.d(TAG, "Gemini Raw Response: $responseBodyStr")

                // Parse standard Gemini structure: candidates[0].content.parts[0].text
                val rootJson = JSONObject(responseBodyStr)
                val candidates = rootJson.getJSONArray("candidates")
                if (candidates.length() == 0) return@withContext null
                
                val partContent = candidates.getJSONObject(0).getJSONObject("content")
                val parts = partContent.getJSONArray("parts")
                if (parts.length() == 0) return@withContext null
                
                val rawModelOutputJson = parts.getJSONObject(0).getString("text")
                Log.d(TAG, "Extracted JSON text: $rawModelOutputJson")

                // Deserialize JSON into CvData using Moshi
                val cvDataAdapter = moshi.adapter(CvData::class.java)
                val cvData = cvDataAdapter.fromJson(rawModelOutputJson)
                
                // Return fresh CV data timestamped
                cvData?.copy(lastModified = System.currentTimeMillis())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in parseResumeText", e)
            null
        }
    }
}

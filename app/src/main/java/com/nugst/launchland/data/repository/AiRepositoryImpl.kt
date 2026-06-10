package com.nugst.launchland.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.nugst.launchland.util.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepositoryImpl(private val securityManager: SecurityManager) {

    suspend fun generateResponse(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = securityManager.getApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("MISSING_API_KEY"))
        }

        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            val response = generativeModel.generateContent(prompt)
            Result.success(response.text ?: "No response from AI")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

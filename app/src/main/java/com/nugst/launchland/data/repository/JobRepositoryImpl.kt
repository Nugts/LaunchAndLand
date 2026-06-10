package com.nugst.launchland.data.repository

import com.nugst.launchland.domain.repository.JobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class JobRepositoryImpl(
    private val client: OkHttpClient = OkHttpClient()
) : JobRepository {

    override suspend fun extractJobDescription(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Failed to fetch URL: ${response.code}"))
                }
                
                val body = response.body?.string() ?: ""
                
                // Basic check for common anti-scraping walls
                if (url.contains("linkedin.com") || body.contains("security challenge") || body.isEmpty()) {
                    return@withContext Result.failure(IOException("Anti-scraping wall detected or empty content. Please paste the description manually."))
                }

                // In a real app, we'd use JSoup to parse specific tags. 
                // For this foundational implementation, we return the body or a placeholder.
                Result.success(body.take(2000)) // Limit size for basic implementation
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

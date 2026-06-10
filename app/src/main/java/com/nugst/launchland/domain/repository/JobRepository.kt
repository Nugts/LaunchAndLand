package com.nugst.launchland.domain.repository

interface JobRepository {
    suspend fun extractJobDescription(url: String): Result<String>
}

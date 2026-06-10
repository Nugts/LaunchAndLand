package com.nugst.launchland.data.local.dao

import androidx.room.*
import com.nugst.launchland.data.local.entity.ChatMessage
import com.nugst.launchland.data.local.entity.ChatThread
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY updatedAt DESC")
    fun getAllThreads(): Flow<List<ChatThread>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: ChatThread)

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("SELECT * FROM chat_threads WHERE title LIKE '%' || :query || '%'")
    fun searchThreads(query: String): Flow<List<ChatThread>>
}

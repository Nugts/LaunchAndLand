package com.nugst.launchland.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nugst.launchland.data.local.dao.ChatDao
import com.nugst.launchland.data.local.dao.UserDao
import com.nugst.launchland.data.local.entity.ChatMessage
import com.nugst.launchland.data.local.entity.ChatThread
import com.nugst.launchland.data.local.entity.DocumentMetadata
import com.nugst.launchland.data.local.entity.UserProfile

@Database(
    entities = [
        UserProfile::class,
        ChatThread::class,
        ChatMessage::class,
        DocumentMetadata::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LaunchlandDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun userDao(): UserDao
}

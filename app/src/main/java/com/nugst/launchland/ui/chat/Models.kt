package com.nugst.launchland.ui.chat

data class ChatMessage(
    val id: Long = 0,
    val threadId: String,
    val content: String,
    val role: String, // "user" or "assistant"
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val education: String = "",
    val skills: String = "",
    val experience: String = ""
)

data class Recommendation(
    val title: String,
    val type: String,
    val description: String
)

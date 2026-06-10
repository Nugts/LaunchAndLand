package com.nugst.launchland.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String = "default_user",
    val name: String,
    val email: String,
    val education: String,
    val skills: String,
    val experience: String,
    val extracurriculars: String
)

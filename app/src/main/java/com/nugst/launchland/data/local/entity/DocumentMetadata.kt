package com.nugst.launchland.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_metadata")
data class DocumentMetadata(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "Resume" or "Cover Letter"
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis()
)

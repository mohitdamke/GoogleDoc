package com.example.googledoc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_documents")
data class DocumentEntity(
    @PrimaryKey val documentId: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

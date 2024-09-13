package com.example.googledoc.domain

import android.content.Context
import androidx.room.Room
import com.example.googledoc.domain.DocumentDatabase

class DocumentRepository(context: Context) {

    private val database: DocumentDatabase = Room.databaseBuilder(
        context.applicationContext,
        DocumentDatabase::class.java,
        "document_database"
    ).build()

    val documentDao = database.documentDao()

    // Add other repository methods as needed
}

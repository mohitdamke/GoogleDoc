package com.example.googledoc.domain

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.googledoc.data.DocumentEntity

@Dao
interface DocumentDao {
    @Insert
    suspend fun saveDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("SELECT * FROM offline_documents WHERE documentId = :id")
    suspend fun getDocument(id: String): DocumentEntity?

}

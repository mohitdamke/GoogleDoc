package com.example.googledoc.domain.repository

import android.content.Context
import androidx.room.Room
import com.example.googledoc.common.Database
import com.example.googledoc.data.Document
import com.example.googledoc.domain.DocumentDao
import com.example.googledoc.domain.DocumentDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DocumentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentDao: DocumentDao
) {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getDocuments(userId: String?): List<Document> {
        val result = db.collection(Database.Documents)
            .whereEqualTo(Database.OwnerId, userId)
            .get()
            .await()
        return result.documents.map { document ->
            document.toObject(Document::class.java) ?: Document()
        }
    }

}

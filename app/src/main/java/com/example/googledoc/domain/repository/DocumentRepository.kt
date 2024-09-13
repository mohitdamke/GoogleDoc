package com.example.googledoc.domain.repository

import com.example.googledoc.common.Database
import com.example.googledoc.data.Document
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DocumentRepository @Inject constructor() {

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

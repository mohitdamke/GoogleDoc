package com.example.googledoc.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googledoc.common.Database
import com.example.googledoc.data.Document
import com.example.googledoc.domain.repository.DocumentRepository
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    val docRef = db.collection(Database.Documents)
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // LiveData for managing the list of documents
    private val _documents = MutableLiveData<List<Document>>()
    val documents: LiveData<List<Document>> get() = _documents

    // LiveData for managing the current document being edited
    private val _currentDocument = MutableLiveData<Document?>()
    val currentDocument: LiveData<Document?> get() = _currentDocument

    // Loading state for data fetch operations
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Error handling state
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error


    init {
        viewModelScope.launch {
            _documents.value = repository.getDocuments(userId) // Fetch documents from repository
        }
    }


    fun fetchDocument(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot = docRef.document(documentId).get().await()
                val document = documentSnapshot.toObject(Document::class.java)

                if (documentSnapshot.exists() && document != null) {
                    // Ensure user has access
                    val sharedWith = documentSnapshot.get("sharedWith") as? Map<*, *>
                    val permission = sharedWith?.get(userId)
                    if (permission != null) {
                        _currentDocument.postValue(document)
                    } else {
                        _error.postValue("Access denied")
                    }
                } else {
                    _error.postValue("Document does not exist")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }

    fun createDocument(title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val documentId = docRef.document().id
            val newDocument = Document(
                documentId = documentId, title = title, content = content, ownerId = userId ?: "",
                sharedWith = mapOf(userId!! to "edit") // Default share with owner as "edit"
            )

            try {
                docRef.document(documentId).set(newDocument).await()
                fetchDocument(documentId)  // Refresh document list after creating a new one
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Function to update the content of an existing document
    fun updateDocument(documentId: String, content: String, title: String) {

        val updates = mapOf(
            Database.Title to title,
            Database.Content to content
        )

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                docRef.document(documentId).update(
                    updates
                ).await()
                fetchDocument(documentId = documentId)  // Refresh document list after creating a new one
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    // Function to delete a document
    fun deleteDocument(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                docRef.document(documentId).delete().await()
                val updatedDocuments = repository.getDocuments(userId)
                _documents.postValue(updatedDocuments)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun getUserIdByEmail(email: String): String? {
        var userId: String? = null
        db.collection(Database.Users)
            .whereEqualTo(Database.Email, email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    userId = document.id  // Assuming userId is stored in Firestore document ID
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error fetching user ID by email", it)
            }

        return userId  // Return the user ID after the query
    }


    fun shareDocument(documentId: String, email: String, permission: String) {
        // Validate the permission value
        if (permission != "view" && permission != "edit") {
            _error.postValue("Invalid permission type")
            return
        }

        // Fetch the user ID by email, assuming you have a way to get the user ID by their email
        val userId = getUserIdByEmail(email)

        if (userId == null) {
            _error.postValue("User with this email does not exist")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                // Update the `sharedWith` field in Firestore with the email and permission
                val updates = mapOf(
                    "sharedWith.$userId" to permission
                )
                docRef.document(documentId).update(updates).await()

                // Optionally, refresh the current document to show updated shared permissions
                fetchDocument(documentId)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }



}
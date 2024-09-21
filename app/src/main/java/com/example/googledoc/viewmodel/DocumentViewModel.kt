package com.example.googledoc.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googledoc.common.Database
import com.example.googledoc.data.Document
import com.example.googledoc.data.DocumentEntity
import com.example.googledoc.domain.DocumentDao
import com.example.googledoc.domain.repository.DocumentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val documentDao: DocumentDao,
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

    // LiveData for managing the offline status of documents
    private val _offlineStatusMap = MutableLiveData<Map<String, Boolean>>(emptyMap())
    val offlineStatusMap: LiveData<Map<String, Boolean>> get() = _offlineStatusMap

    private val _success = MutableLiveData<String>()
    val success: LiveData<String> get() = _success

    // Loading state for data fetch operations
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Error handling state
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error


    init {
        viewModelScope.launch {
            _documents.value = repository.getDocuments(userId) // Fetch documents from repository
            updateOfflineStatusForDocuments()
        }
    }

    fun fetchDocument(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot = docRef.document(documentId).get().await()
                val document = documentSnapshot.toObject(Document::class.java)

                if (document != null) {
                    // Log the fetched document
                    Log.d("FetchDocument", "Fetched document: $document")

                    // Ensure user has access
                    val sharedWith = documentSnapshot.get("sharedWith") as? Map<String, String>
                    Log.d("FetchDocument", "Shared with: $sharedWith")

                    // Check if the userId is available
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
                Log.e("FetchDocumentError", "Error fetching document: ${e.message}")
                _error.postValue(e.message)
            }
        }
    }

    fun createDocument(title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val documentId = docRef.document().id
            val newDocument = Document(
                documentId = documentId,
                title = title,
                content = content,
                ownerId = userId ?: return@launch,  // Handle null case by stopping execution,
                sharedWith = mapOf(userId to "edit") // Default share with owner as "edit"
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

    private suspend fun getUserIdByEmail(email: String): String? {
        return try {
            val querySnapshot = db.collection(Database.Users)
                .whereEqualTo(Database.Email, email)
                .get()
                .await()
            // Return the first user ID found
            querySnapshot.documents.firstOrNull()?.id
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching user ID by email", e)
            null
        }
    }

    suspend fun editPermission(documentId: String, email: String, permission: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("TAG EDIT", "FirestoreUpdate 1")

            if (permission != "view" && permission != "edit") {
                _error.postValue("Invalid permission type")
                return@launch
            }
            Log.d("TAG EDIT", "Permission type validated.")

            val userId = getUserIdByEmail(email) ?: run {
                Log.d("TAG EDIT", "User ID not found for email: $email")
                _error.postValue("User with this email does not exist")
                return@launch
            }

            Log.d("TAG EDIT", "User ID retrieved: $userId")

            _isLoading.postValue(true)
            try {
                Log.d("TAG EDIT", "Fetching current document.")
                val documentSnapshot = docRef.document(documentId).get().await()
                val sharedWith = documentSnapshot.get("sharedWith") as? Map<String, String> ?: emptyMap()

                Log.d("TAG EDIT", "Current sharedWith map retrieved: $sharedWith")

                // Create a new map for the update
                val updatedSharedWith = sharedWith.toMutableMap()
                updatedSharedWith[userId] = permission // Update or add the new permission
                Log.d("TAG EDIT", "Updated sharedWith map: $updatedSharedWith")

                // Update the document with the new sharedWith map
                val updates = mapOf("sharedWith" to updatedSharedWith)
                Log.d("TAG EDIT", "Updating document $documentId with: $updates")

                docRef.document(documentId).update(updates).await()
                Log.d("TAG EDIT", "Document updated successfully.")
                _success.postValue("Document shared successfully with $email as $permission")
            } catch (e: Exception) {
                Log.e("TAG EDIT", "Error updating document: ${e.message}")
                _error.postValue(e.message ?: "An error occurred")
            } finally {
                _isLoading.postValue(false)
                Log.d("TAG EDIT", "Loading state reset.")
            }
        }
    }

    fun saveDocumentOffline(context: Context, document: Document) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentEntity = DocumentEntity(
                    documentId = document.documentId,
                    title = document.title,
                    content = document.content,
                    timestamp = document.timestamp
                )
                documentDao.saveDocument(documentEntity)
                updateOfflineStatusForDocuments()
                // Show a success Toast message on the main thread
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Document saved offline", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Failed to save document offline: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Removing document from offline storage with toast notification
    fun removeDocumentOffline(context: Context, document: Document) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentEntity = DocumentEntity(
                    documentId = document.documentId,
                    title = document.title,
                    content = document.content,
                    timestamp = document.timestamp
                )
                documentDao.deleteDocument(documentEntity)
                updateOfflineStatusForDocuments()

                // Show a success Toast message on the main thread
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Document removed from offline storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Failed to remove document: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Checking if a document is offline with toast notification
    fun isDocumentOffline(context: Context, documentId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = documentDao.getDocument(documentId)
                val isOffline = document != null

                // Show a toast based on the result
                viewModelScope.launch(Dispatchers.Main) {
                    val message = if (isOffline) {
                        "Document is available offline"
                    } else {
                        "Document is not saved offline"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                    onResult(isOffline)
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error checking document offline status: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun updateOfflineStatusForDocuments() {
        val documentList = _documents.value ?: return
        val statusMap = documentList.associate { doc ->
            doc.documentId to (documentDao.getDocument(doc.documentId) != null)
        }
        _offlineStatusMap.postValue(statusMap)
    }

}
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
    private var userEmail: String? = null


    // LiveData for managing the list of documents
    private val _documents = MutableLiveData<List<Document>>()
    val documents: LiveData<List<Document>> get() = _documents

    // LiveData for managing the current document being edited
    private val _currentDocument = MutableLiveData<Document?>()
    val currentDocument: LiveData<Document?> get() = _currentDocument

    // LiveData for managing the offline status of documents
    private val _offlineStatusMap = MutableLiveData<Map<String, Boolean>>(emptyMap())
    val offlineStatusMap: LiveData<Map<String, Boolean>> get() = _offlineStatusMap

    // Loading state for data fetch operations
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Error handling state
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error


    init {
        viewModelScope.launch {
            fetchUserEmail()  // Fetch user email at initialization
            _documents.value = repository.getDocuments(userId) // Fetch documents from repository
            updateOfflineStatusForDocuments()
        }
    }

    private suspend fun fetchUserEmail() {
        userId?.let { id ->
            try {
                val document = db.collection("users").document(id).get().await()
                userEmail = document.getString("email")  // Fetch email from Firestore
            } catch (e: Exception) {
                _error.postValue("Failed to fetch user email: ${e.message}")
            }
        }
    }

    fun updateDocument(documentId: String, newContent: String) {
        val documentRef = FirebaseFirestore.getInstance().collection("documents").document(documentId)

        documentRef.update("content", newContent)
            .addOnSuccessListener {
                Log.d("DocumentViewModel", "Document successfully updated")
                // Regenerate PDF after update
                regeneratePdf(documentId) // This method will take care of merging changes into the PDF
            }
            .addOnFailureListener { e ->
                Log.w("DocumentViewModel", "Error updating document", e)
            }
    }
    private fun regeneratePdf(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch the latest document content
                val documentSnapshot = docRef.document(documentId).get().await()
                val updatedContent = documentSnapshot.getString("content") ?: return@launch

                // Generate the PDF based on the updated content
                // Replace with your existing logic for PDF generation
                val pdfFilePath = generatePdf(updatedContent, documentId)

                Log.d("PDF Generation", "PDF updated at: $pdfFilePath")
            } catch (e: Exception) {
                Log.e("PDF Generation Error", "Error updating PDF", e)
            }
        }
    }

    // Example of generating PDF (replace this with your actual PDF generation logic)
    private fun generatePdf(content: String, documentId: String): String {
        // Your logic to create or update the PDF goes here.
        // For instance, use PdfDocument to create a PDF with the new content.
        return "path/to/your/pdf/file.pdf" // Return the path of the generated PDF
    }


    fun fetchDocument(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot = docRef.document(documentId).get().await()
                val document = documentSnapshot.toObject(Document::class.java)

                if (document != null) {
                    Log.d("FetchDocument", "Fetched document: $document")

                    // Log the entire document snapshot data for debugging
                    Log.d("FetchDocumentData", "Document data: ${documentSnapshot.data}")

                    // Post the fetched document to LiveData
                    _currentDocument.postValue(document)
                } else {
                    _error.postValue("Document does not exist")
                }
            } catch (e: Exception) {
                Log.e("FetchDocumentError", "Error fetching document", e) // Log the exception
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
                ownerId = userId ?: return@launch,
                ownerEmail = userEmail ?: "", // Set ownerEmail
                sharedWith = mapOf("$userEmail" to "edit") // Share with the owner's email
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


    suspend fun editPermission(documentId: String, email: String, permission: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Validate the permission type
            if (permission != "view" && permission != "edit") {
                _error.postValue("Invalid permission type")
                return@launch
            }

            _isLoading.postValue(true)
            try {
                // Fetch the current document
                val documentSnapshot = docRef.document(documentId).get().await()
                val sharedWith =
                    documentSnapshot.get("sharedWith") as? Map<String, String> ?: emptyMap()

                // Create a mutable map to update sharedWith
                val updatedSharedWith = sharedWith.toMutableMap()
                updatedSharedWith[email] = permission // Update or add the permission for the email

                // Update the Firestore document
                docRef.document(documentId).update("sharedWith", updatedSharedWith).await()

            } catch (e: Exception) {
                _error.postValue(e.message ?: "An error occurred")
            } finally {
                _isLoading.postValue(false)
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
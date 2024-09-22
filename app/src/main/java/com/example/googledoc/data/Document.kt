package com.example.googledoc.data

data class Document(
    var documentId: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val ownerId: String = "",
    val ownerEmail: String = "",
    val sharedWith: Map<String, String> = emptyMap(),
)



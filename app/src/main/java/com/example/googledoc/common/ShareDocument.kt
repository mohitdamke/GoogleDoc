package com.example.googledoc.common

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.googledoc.data.Document
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@Composable
fun ShareDocument(documentId: String) {
    val context = LocalContext.current

    // Example link to share
    val shareText = "Check out this document: https://example.com/documents/$documentId"

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(intent, "Share document"))
}

@Composable
fun ShareDocumentAsFile(document: Document) {
    val context = LocalContext.current

    // Create a file to share
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "${document.title}.pdf")

    // Save the content to the file (this is a placeholder; use actual PDF generation code here)
    val outputStream: OutputStream = FileOutputStream(file)
    outputStream.use {
        it.write(document.content.toByteArray())
    }

    val uri = Uri.fromFile(file)
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "application/pdf" // Adjust this based on file type
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share document"))
}

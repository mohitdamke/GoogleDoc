package com.example.googledoc.common

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.googledoc.data.Document
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@Composable
fun ShareDocument(documentId: String) {
    val context = LocalContext.current

    // Create the deep link to share
//    val shareText = "Check out this document: googledoc://document/$documentId"
    val shareText = "Check out this document: googledoc://document/$documentId"

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    // Start the share activity
    context.startActivity(Intent.createChooser(intent, "Share document"))
}
@Composable
fun ShareDocumentAsFile(document: Document) {
    val context = LocalContext.current

    // Create a file in the external storage directory
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "${document.title}.pdf")

    // Write the content to the file (you'll need actual PDF generation code here)
    val outputStream: OutputStream = FileOutputStream(file)
    outputStream.use {
        it.write(document.content.toByteArray()) // Write content to file
    }

    // Get the URI using FileProvider
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    // Prepare the intent
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "application/pdf"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant temporary read access
    }

    // Start the share activity
    context.startActivity(Intent.createChooser(intent, "Share document"))
}

package com.example.googledoc

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.googledoc.presentation.ShareDocScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DocumentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val data: Uri? = intent?.data
            val documentId = data?.lastPathSegment

            // Pass the documentId to DocumentScreen
            if (documentId != null) {
                ShareDocScreen(documentId = documentId)
            } else {
                Toast.makeText(this, "The link is invalid", Toast.LENGTH_SHORT).show()
                // Handle error (e.g., show an error message)
            }
        }
    }
}

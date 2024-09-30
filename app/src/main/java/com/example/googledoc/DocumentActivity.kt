package com.example.googledoc

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.example.googledoc.navigation.DocNavigationGraph
import com.example.googledoc.ui.theme.CustomAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DocumentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomAppTheme {
            val data: Uri? = intent?.data
            val documentId = remember { data?.lastPathSegment }
            // Safely get the last path segment
            // Pass the documentId to DocumentScreen
            if (!documentId.isNullOrEmpty()) {
                DocNavigationGraph(documentId = documentId)
            } else {
                Toast.makeText(this, "The link is invalid", Toast.LENGTH_SHORT).show()
                // Handle error (e.g., show an error message)
                finish() // Close the activity if the link is invalid
            }
        }}
    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        // When back is pressed, finish this activity to clear it from the back stack
        finish()
    }
}

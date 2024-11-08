package com.example.googledoc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.googledoc.presentation.PdfViewerScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OpenPdfActivity : AppCompatActivity() {

    private lateinit var openFileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the file picker
        openFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        // Open the PDF using PdfViewerScreen
                        openPdf(uri)
                    }
                }
            }

        // Check if there's an intent with a URI passed in (for deep linking)
        intent?.data?.let { uri ->
            openPdf(uri)
        } ?: run {
            // Launch the file picker if no URI is passed
            openFilePicker()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf" // Filter for PDF files
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        openFileLauncher.launch(intent)
    }

    private fun openPdf(uri: Uri) {
        // Open the PDF using PdfViewerScreen
        setContent {
            PdfViewerScreen(pdfUri = uri.toString())
        }
    }
}

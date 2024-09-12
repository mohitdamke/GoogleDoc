package com.example.googledoc.presentation

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.googledoc.common.SaveAsPdf
import com.example.googledoc.data.Document
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.viewmodel.DocumentViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    documentId: String,  // Passed from navigation

) {
    val context = LocalContext.current
    val documentViewModel: DocumentViewModel = hiltViewModel()
    val document by documentViewModel.currentDocument.observeAsState()
    val isLoading by documentViewModel.isLoading.observeAsState(false)

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Fetch the document data when the screen loads
    LaunchedEffect(documentId) {
        documentViewModel.fetchDocument(documentId)
        println("Document fetched: ${documentViewModel.currentDocument.value}")
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showBottomSheet = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Document")
            }
        }, topBar = {
            TopAppBar(title = {
                Text(
                    text = document?.title ?: "Loading",
                    textAlign = TextAlign.Start,
                    fontSize = 24.sp,
                    minLines = 1,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            })
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            document?.let {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                loadData(it.content, "text/html", "UTF-8")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                if (document != null) {
                    DocumentBottomSheetContent(
                        onDismiss = {
                            showBottomSheet = false
                        },
                        // Pass a lambda that can invoke ShareDocument
                        onLinkShare = {

                            // Example link to share
                            val shareText =
                                "Check out this document: https://example.com/documents/$documentId"

                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }

                            context.startActivity(Intent.createChooser(intent, "Share document"))
                        },
                        onFileShare = {

                            val file = File(
                                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                                "${document!!.title}.pdf"
                            )

// Save the content to the file (replace this with actual PDF content generation)
                            val outputStream: OutputStream = FileOutputStream(file)
                            outputStream.use {
                                it.write(document!!.content.toByteArray())
                            }

// Use FileProvider to get a content URI
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider", // Make sure it matches the provider authority in AndroidManifest.xml
                                file
                            )

                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = "application/pdf"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

// Start the share intent
                            context.startActivity(Intent.createChooser(intent, "Share document"))
                        },
                        onEdit = {
                            navController.navigate(
                                Routes.Edit.route.replace("{documentId}", documentId)
                            )
                        },
                        onDownloadPdf = { document ->
                            SaveAsPdf(context, document)
                        },
                        document = document!!
                    )
                } else {
                    // Fallback content if document is null
                    Text("No document available")
                }
            }
        }
    }
}

@Composable
fun DocumentBottomSheetContent(
    onDismiss: () -> Unit,
    onFileShare: () -> Unit,
    onLinkShare: () -> Unit,
    onEdit: () -> Unit,
    onDownloadPdf: (Document) -> Unit,
    document: Document
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Options", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Row {

            Button(onClick = {
                onFileShare()
                onDismiss()
            }) {
                Text("File Share")
            }

            Button(onClick = {
                onLinkShare()
                onDismiss()
            }) {
                Text("Link Share")
            }
        }
        Button(onClick = {
            onEdit()
            onDismiss()
        }) {
            Text("Edit")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            onDownloadPdf(document)
            onDismiss()
        }) {
            Text("Download as PDF")
        }
    }
}



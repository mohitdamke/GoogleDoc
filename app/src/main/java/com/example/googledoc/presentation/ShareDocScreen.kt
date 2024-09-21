package com.example.googledoc.presentation

import android.content.Intent
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.googledoc.common.SaveAsPdf
import com.example.googledoc.data.Document
import com.example.googledoc.viewmodel.DocumentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareDocScreen(
    documentId: String,
    navController: NavController = rememberNavController(),
) {
    val docViewModel: DocumentViewModel = hiltViewModel()
    val context = LocalContext.current
    val documentViewModel: DocumentViewModel = hiltViewModel()
    val document by documentViewModel.currentDocument.observeAsState()
    val isLoading by documentViewModel.isLoading.observeAsState(false)
    val errorMessage by documentViewModel.error.observeAsState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) {
        // Fetch the document when this screen is displayed
        docViewModel.fetchDocument(documentId)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Document")
            }
        },
        topBar = {
            TopAppBar(title = {
                Text(
                    text = document?.title ?: "Loading",
                    textAlign = TextAlign.Start,
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            })
        }
    ) { paddingValues ->
        if (docViewModel.isLoading.value == true) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            document?.let {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                loadData(it.content, "text/html", "UTF-8")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            } ?: run {
                // Handle error: Document not found
                Text("Document not found.")
            }
        }

        if (showBottomSheet) {
            DocumentBottomSheetContent(
                onDismiss = { showBottomSheet = false },
                onFileShare = { /* File share logic here */ },
                onLinkShare = {
                    val shareText = "Check out this document: https://googledoc/document/$documentId"
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(intent, "Share document"))
                },
                onEdit = {
                    navController.navigate("edit/$documentId") // Adjust route as necessary
                },
                onDownloadPdf = {
                    document?.let { it1 -> SaveAsPdf(context, it1) }
                },
                document = document!!
            )
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
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(text = "Share Document", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onFileShare()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("File Share")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onLinkShare()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Link Share")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onEdit()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onDownloadPdf(document)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Download as PDF")
        }
    }
}



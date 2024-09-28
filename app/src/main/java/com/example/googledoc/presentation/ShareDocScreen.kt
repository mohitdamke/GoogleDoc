package com.example.googledoc.presentation

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.navigation.compose.rememberNavController
import com.example.googledoc.data.Document
import com.example.googledoc.download.saveAsPdf
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.viewmodel.DocumentViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareDocScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    navController: NavController = rememberNavController(),
) {
    val context = LocalContext.current
    val docViewModel: DocumentViewModel = hiltViewModel()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val documentViewModel: DocumentViewModel = hiltViewModel()
    val document by documentViewModel.currentDocument.observeAsState()
    val isLoading by documentViewModel.isLoading.observeAsState(false)
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val userPermission = document?.sharedWith?.get(currentUserId) ?: "view"
    val errorMessage by documentViewModel.error.observeAsState()

    LaunchedEffect(documentId) {
        docViewModel.fetchDocument(documentId)
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        val userEmail = currentUser.email
        Log.d("YourTag", "User email fetched successfully: $userEmail")
        // Proceed with document retrieval/editing logic
    } else {
        Log.e("YourTag", "Failed to fetch user email: User is not logged in")
        Toast.makeText(context, "Failed to fetch user email", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            Log.e("YourTag", message)

        }
    }
    Scaffold(floatingActionButton = {
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        })
    }) { paddingValues ->
        if (isLoading) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Spacer(modifier = modifier.height(16.dp))
                document?.let {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.domStorageEnabled = true
                                loadData(it.content, "text/html", "UTF-8")
                                webViewClient = object : WebViewClient() {
                                    @Deprecated("Deprecated in Java")
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?, url: String?
                                    ): Boolean {
                                        view?.loadUrl(url!!) // Handle links inside the WebView
                                        return true
                                    }
                                }
                            }
                        }, modifier = modifier.weight(1f)
                    )
                } ?: run {
                    // Handle error: Document not found
                    Text("Document not found.")
                }

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        }, sheetState = sheetState
                    ) {
                        document?.let { doc ->
                            DocumentBottomSheetContent(
                                onDismiss = { showBottomSheet = false },
                                onLinkShare = {
                                    val shareText =
                                        "Check out this document: https://googledoc/document/$documentId"
                                    Log.d(
                                        "TAG DocumentViewScreen ", "DocumentViewScreen: $documentId"
                                    )
                                    val intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, "Share document")
                                    )
                                },
                                onFileShare = {
                                    val file = File(
                                        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                                        "${doc.title}.pdf"
                                    )
                                    val outputStream: OutputStream = FileOutputStream(file)
                                    outputStream.use {
                                        it.write(doc.content.toByteArray()) // Write content to file
                                    }
                                    val uri = FileProvider.getUriForFile(
                                        context, "${context.packageName}.fileprovider", file
                                    )
                                    val intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        type = "application/pdf"
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, "Share document")
                                    )
                                },
                                onEdit = {

                                    Log.d("ShareDocScreen TAG", "User permission: $userPermission")

                                    if (userPermission == "edit") {
                                        navController.navigate(
                                            Routes.Edit.route.replace("{documentId}", documentId)
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "You don't have permission to edit this document.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onDownloadPdf = { document ->
                                    saveAsPdf(context, document)
                                },
                                document = doc
                            )
                        } ?: run {
                            Text("No document available")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentBottomSheetContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onFileShare: () -> Unit,
    onLinkShare: () -> Unit,
    onEdit: () -> Unit,
    onDownloadPdf: (Document) -> Unit,
    document: Document
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(label = { Text("File Share") }, icon = {
            Icon(Icons.Default.Share, contentDescription = "File Share")
        }, selected = false, onClick = {
            onFileShare()
            onDismiss()
        })

        NavigationDrawerItem(label = { Text("Link Share") }, icon = {
            Icon(Icons.Default.Link, contentDescription = "Link Share")
        }, selected = false, onClick = {
            onLinkShare()
            onDismiss()
        })

        Spacer(modifier = Modifier.height(8.dp))

        // Edit Option
        NavigationDrawerItem(label = { Text("Edit") }, icon = {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }, selected = false, onClick = {
            onEdit()
            onDismiss()
        })

        Spacer(modifier = Modifier.height(8.dp))

        // Download as PDF Option
        NavigationDrawerItem(label = { Text("Download as PDF") }, icon = {
            Icon(Icons.Default.PictureAsPdf, contentDescription = "Download as PDF")
        }, selected = false, onClick = {
            onDownloadPdf(document)
            onDismiss()
        })
    }
}
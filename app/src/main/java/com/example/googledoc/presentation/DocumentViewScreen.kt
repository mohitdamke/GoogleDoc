package com.example.googledoc.presentation

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
fun DocumentViewScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    documentId: String,  // Passed from navigation
    pdfUri: Uri? = null // Optionally pass a Uri to a PDF file
) {
    val context = LocalContext.current
    val documentViewModel: DocumentViewModel = hiltViewModel()
    val document by documentViewModel.currentDocument.observeAsState()
    val isLoading by documentViewModel.isLoading.observeAsState(false)
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
    val userPermission = remember(document?.sharedWith, currentUserEmail) {
        document?.sharedWith?.get(currentUserEmail) ?: "view"
    }
    if (pdfUri == null) {
        LaunchedEffect(documentId) {
            documentViewModel.fetchDocument(documentId)
            println("Document fetched: ${documentViewModel.currentDocument.value}")
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showBottomSheet = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Document")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = document?.title ?: "Loading",
                        textAlign = TextAlign.Start,
                        fontSize = 24.sp,
                        minLines = 1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = modifier.padding(start = 10.dp)
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = modifier.clickable {
                            scope.launch {
                                navController.navigateUp()
                            }
                        }, tint = Gray
                    )
                }
            )
        },

        ) { paddingValues ->
        if (isLoading && pdfUri == null) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (pdfUri != null) {
                    // Display PDF using PDFView
                    PdfViewerScreen(pdfUri = pdfUri.toString())
                } else {
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
                            }
                        )
                    }
                }
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        }, sheetState = sheetState
                    ) {
                        if (document != null) {
                            DocumentBottomSheetContent(
                                onDismiss = {
                                    showBottomSheet = false
                                },
                                // Pass a lambda that can invoke ShareDocument
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

                                    // Start the share activity
                                    context.startActivity(
                                        Intent.createChooser(
                                            intent, "Share document"
                                        )
                                    )
                                },
                                onFileShare = {

                                    val file = File(
                                        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                                        "${document!!.title}.pdf"
                                    )

                                    // Write the content to the file (you'll need actual PDF generation code here)
                                    val outputStream: OutputStream = FileOutputStream(file)
                                    outputStream.use {
                                        it.write(document!!.content.toByteArray()) // Write content to file
                                    }

                                    // Get the URI using FileProvider
                                    val uri = FileProvider.getUriForFile(
                                        context, "${context.packageName}.fileprovider", file
                                    )

                                    // Prepare the intent
                                    val intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        type = "application/pdf"
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant temporary read access
                                    }

                                    // Start the share activity
                                    context.startActivity(
                                        Intent.createChooser(
                                            intent, "Share document"
                                        )
                                    )
                                },
                                onEdit = {
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
                                onShareDocument = { email, permission ->
                                    scope.launch {
                                        documentViewModel.editPermission(
                                            documentId = documentId,
                                            email = email,
                                            permission = permission
                                        )
                                    }
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
    onShareDocument: (String, String) -> Unit,
    document: Document
) {
    val context = LocalContext.current
    var emailToShare by rememberSaveable { mutableStateOf("") }
    var permission by remember { mutableStateOf("view") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd)
        ) {
            OutlinedTextField(
                value = emailToShare,
                onValueChange = { emailToShare = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More", modifier = modifier.clickable {
                            expanded = !expanded
                        }
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Send, contentDescription = "Send Permission",
                        modifier = modifier.clickable {
                            if (emailToShare.isNotEmpty() && permission.isNotEmpty()) {
                                onShareDocument(emailToShare, permission)
                                Toast.makeText(
                                    context,
                                    "Document shared with $emailToShare as $permission",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter a valid email and select a permission",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            onDismiss() // Dismiss the drawer after the action

                        },
                    )
                },
                placeholder = { Text("Enter email to share with") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp), // Use smaller radius for a modern look
                singleLine = true // Keep the input single line,

            )

            // Dropdown menu for selecting permissions
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.wrapContentHeight()  // Ensure the dropdown fills the width
            ) {
                DropdownMenuItem(
                    text = { Text("View", fontSize = 16.sp) },
                    onClick = {
                        permission = "view"
                        expanded = false // Close the dropdown after selection
                    }
                )
                Divider()
                DropdownMenuItem(
                    text = { Text("Edit", fontSize = 16.sp) },
                    onClick = {
                        permission = "edit"
                        expanded = false // Close the dropdown after selection
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))


        Spacer(modifier = Modifier.height(8.dp))
        // File Share Option
        NavigationDrawerItem(label = { Text("File Share") }, icon = {
            Icon(Icons.Default.Share, contentDescription = "File Share")
        }, selected = false, onClick = {
            onFileShare()
            onDismiss()
        })
        Spacer(modifier = Modifier.height(8.dp))

        // Link Share Option
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

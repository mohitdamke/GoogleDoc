package com.example.googledoc.presentation

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.googledoc.R
import com.example.googledoc.common.FormatTimestamp
import com.example.googledoc.data.Document
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.size.FontDim
import com.example.googledoc.size.TextDim
import com.example.googledoc.viewmodel.DocumentViewModel
import com.example.googledoc.viewmodel.LoginViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier, navController: NavController
) {
    val documentViewModel: DocumentViewModel = hiltViewModel()
    val documents by documentViewModel.documents.observeAsState(emptyList())
    val isLoading by documentViewModel.isLoading.observeAsState(false)
    val offlineStatusMap by documentViewModel.offlineStatusMap.observeAsState(emptyMap())
    val currentUser = Firebase.auth.currentUser?.uid
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val context = LocalContext.current
    var selectedDocument by remember { mutableStateOf<Document?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val backgroundColor =
        MaterialTheme.colorScheme.background // Replace with your desired background color
    val iconColor =
        MaterialTheme.colorScheme.onBackground // Replace with your desired background color
    val textColor = MaterialTheme.colorScheme.primary // Replace with your desired text color

    // Launcher for file picker

    val filePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri: Uri? ->
                uri?.let {
                    val pdfUri = Uri.encode(it.toString())
                    navController.navigate(Routes.PdfView.route.replace("{pdfUri}", pdfUri))
                }
            })


    // Fetch the user's documents when this screen loads
    LaunchedEffect(Unit) {
        try {
            if (currentUser != null) {
                documentViewModel.fetchDocument(documentId = currentUser)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching documents: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }


    LaunchedEffect(key1 = Unit) {
        if (currentUser == null) {
            navController.navigate(Routes.Login.route) {
                popUpTo(0) // Clear backstack
            }
        }
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ModalNavigationDrawer(
        modifier = modifier.background(White),
        scrimColor = White,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = modifier.padding(top = 10.dp))
                Text(
                    text = "Google Doc",
                    fontSize = TextDim.titleTextSize,
                    fontFamily = FontDim.Bold,
                    color = textColor,
                    modifier = Modifier.padding(10.dp)
                )
                Spacer(modifier = modifier.padding(top = 10.dp))
                HorizontalDivider()
                NavigationDrawerItem(label = { Text(text = "Logout") }, icon = {
                    Icon(
                        imageVector = Icons.Default.Logout, contentDescription = "logout"
                    )
                }, selected = false, onClick = {
                    loginViewModel.getGoogleSignInClient(context as Activity) // Ensure it's initialized
                    loginViewModel.signOut()
                    scope.launch {
                        Toast.makeText(context, "Signed out successfully.", Toast.LENGTH_SHORT)
                            .show()
                        navController.navigate(Routes.Login.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    }
                })
            }
        }) {
        Scaffold(topBar = {
            CenterAlignedTopAppBar(title = {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.googledoc),
                        contentDescription = "doc",
                        modifier = modifier.size(30.dp)
                    )
                    Spacer(modifier = modifier.padding(start = 10.dp))
                    Text(
                        text = "Google Doc",
                        fontSize = TextDim.titleTextSize,
                        fontFamily = FontDim.Bold,
                        color = textColor,
                        modifier = Modifier
                    )
                }
            }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(backgroundColor),
                actions = {
                    Icon(imageVector = Icons.Default.Search,
                        contentDescription = "search",
                        modifier = modifier
                            .clickable {
                                navController.navigate(Routes.Search.route)
                            }
                            .size(30.dp), tint = iconColor
                    )
                }, navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ViewHeadline,
                        contentDescription = "more",
                        modifier = modifier
                            .size(30.dp)
                            .clickable {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }, tint = iconColor

                    )
                })
        }, floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showBottomSheet = true
                },
            ) {
                Icon(
                    Icons.Default.Add, contentDescription = "New Document", tint = iconColor
                )
            }
        }) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(paddingValues)
                ) {
                    LazyColumn(modifier = modifier.padding(10.dp)) {
                        items(documents) { document ->
                            DocumentItem(
                                document = document,
                                onClick = {
                                    navController.navigate(
                                        Routes.View.route.replace(
                                            "{documentId}", document.documentId
                                        )
                                    )
                                },
                                onMore = {
                                    selectedDocument = document
                                    scope.launch {
                                        sheetState.show()
                                    }
                                },
                            )
                            // Single ModalBottomSheet outside of LazyColumn
                            if (selectedDocument != null) {
                                ModalBottomSheet(
                                    onDismissRequest = {
                                        selectedDocument = null
                                        scope.launch {
                                            sheetState.hide()
                                        }
                                    }, sheetState = sheetState
                                ) {
                                    NavigationDrawerItem(label = {
                                        Text(
                                            text = "Delete",
                                            color = textColor
                                        )
                                    }, icon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "delete",
                                            tint = iconColor


                                        )
                                    }, selected = false, onClick = {
                                        selectedDocument?.let { document ->
                                            documentViewModel.deleteDocument(document.documentId)
                                            scope.launch {
                                                sheetState.hide()
                                                selectedDocument = null
                                            }
                                        }
                                    })
                                    NavigationDrawerItem(label = {
                                        Text(
                                            text = "Save Offline",
                                            color = textColor
                                        )
                                    },
                                        icon = {
                                            val isOffline =
                                                offlineStatusMap[selectedDocument?.documentId]
                                                    ?: false
                                            Icon(
                                                imageVector = if (isOffline) Icons.Default.Check else Icons.Default.Download,
                                                contentDescription = if (isOffline) "Remove from Offline" else "Save Offline",
                                                tint = iconColor

                                            )
                                        },
                                        selected = false,
                                        onClick = {
                                            selectedDocument?.let { document ->
                                                if (offlineStatusMap[document.documentId] == true) {
                                                    documentViewModel.removeDocumentOffline(
                                                        context, document
                                                    )
                                                } else {
                                                    documentViewModel.saveDocumentOffline(
                                                        context = context, document = document
                                                    )
                                                }
                                                scope.launch {
                                                    sheetState.hide()
                                                    selectedDocument = null
                                                }
                                            }
                                        })
                                }
                            }
                        }
                    }
                }
            }
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    }, sheetState = sheetState
                ) {
                    BottomSheetContent(onNewDocument = {
                        navController.navigate(
                            Routes.Edit.route.replace(
                                oldValue = "{documentId}", newValue = "new"
                            )
                        )
                        showBottomSheet = false
                    }, onOpenFromStorage = {
                        // Handle file picker for opening document from phone storage
                        // PDF Uri to be passed to PdfViewer

                        filePickerLauncher.launch(arrayOf("application/pdf"))
                        showBottomSheet = false
                    })
                }
            }
        }
    }
}

@Composable
private fun BottomSheetContent(
    modifier: Modifier = Modifier, onNewDocument: () -> Unit, onOpenFromStorage: () -> Unit
) {
    val backgroundColor =
        MaterialTheme.colorScheme.background // Replace with your desired background color
    val iconColor =
        MaterialTheme.colorScheme.onBackground // Replace with your desired background color
    val textColor = MaterialTheme.colorScheme.primary // Replace with your desired text color

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Option to create a new document
        Button(
            onClick = onNewDocument,
            colors = ButtonDefaults.buttonColors(backgroundColor), // Set button background to white
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(2.dp)
        ) {
            Icon(Icons.Default.Create, contentDescription = "New Document", tint = iconColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Create New Document",
                color = textColor,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Option to open a document from storage
        Button(
            onClick = onOpenFromStorage,
            colors = ButtonDefaults.buttonColors(backgroundColor), // Set button background to white
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(2.dp)
        ) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = "Open Document from Storage",
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Open from Phone Storage",
                color = textColor,
            )
        }
    }
}

// Function to pick a PDF from storage

@Composable
fun DocumentItem(
    document: Document,
    onClick: () -> Unit,
    onMore: () -> Unit,
) {
    val backgroundColor =
        MaterialTheme.colorScheme.background // Replace with your desired background color
    val iconColor =
        MaterialTheme.colorScheme.onBackground // Replace with your desired background color
    val textColor = MaterialTheme.colorScheme.primary // Replace with your desired text color

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = document.title,
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )
            Text(
                text = "Last Edited: ${FormatTimestamp(document.timestamp)}",
                color = textColor,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Row {
            IconButton(onClick = onMore) {
                Icon(Icons.Default.MoreVert, contentDescription = "Share", tint = iconColor)
            }
        }
    }
}

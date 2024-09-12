package com.example.googledoc.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.googledoc.common.FormatTimestamp
import com.example.googledoc.data.Document
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.viewmodel.DocumentViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier, navController: NavController
) {
    val documentViewModel: DocumentViewModel = hiltViewModel()
    // Observe the list of documents
    val documents by documentViewModel.documents.observeAsState(emptyList())
    val isLoading by documentViewModel.isLoading.observeAsState(false)
    val currentUser = Firebase.auth.currentUser?.uid
    var documentToShare by remember { mutableStateOf<String?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Fetch the user's documents when this screen loads
    LaunchedEffect(Unit) {
        documentViewModel.fetchDocument(documentId = currentUser!!)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Drawer title", modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item") },
                    selected = false,
                    onClick = { /*TODO*/ }
                )
                TextButton(onClick = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) // Clear the backstack
                    }
                }) {
                    Text("Logout")
                }
            }
        },
    ) {
        Scaffold(topBar = {
            CenterAlignedTopAppBar(title = { Text("My Documents") },
                actions = {

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "search",
                        modifier = modifier.clickable {
                            navController.navigate(Routes.Search.route)
                        })
                }, navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.PersonOutline,
                        contentDescription = "more",
                        modifier = modifier.clickable {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        })
                }
            )
        }, floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(
                    Routes.Edit.route.replace(
                        oldValue = "{documentId}", newValue = "new"
                    )
                )
            }) {
                Icon(Icons.Default.Add, contentDescription = "New Document")
            }
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

                    LazyColumn(modifier = modifier.padding(16.dp)) {
                        items(documents) { document ->
                            DocumentItem(document = document, onClick = {
                                navController.navigate(
                                    Routes.View.route.replace(
                                        "{documentId}", document.documentId
                                    )
                                )
                            }, onDelete = {
                                documentViewModel.deleteDocument(document.documentId)
                            },
                                onShare = {
                                    showShareDialog = true
                                    documentToShare = document.documentId
                                }
                            )
                        }
                    }
                }
                // Show share dialog
                if (showShareDialog && documentToShare != null) {
                    ShareDocumentDialog(
                        documentId = documentToShare!!,
                        onDismiss = { showShareDialog = false },
                        viewModel = documentViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentItem(
    document: Document, onClick: () -> Unit, onDelete: () -> Unit, onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = document.title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "Last Edited: ${FormatTimestamp(document.timestamp)}",
                style = MaterialTheme.typography.labelMedium
            )
        }
        Row {
            IconButton(onClick = onShare) {
                Icon(Icons.Default.MoreVert, contentDescription = "Share")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}


@Composable
fun ShareDocumentDialog(
    documentId: String,
    onDismiss: () -> Unit,
    viewModel: DocumentViewModel
) {
    var email by remember { mutableStateOf("") }
    var permission by remember { mutableStateOf("view") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Document") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("User Email") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Permission")
                Row {
                    RadioButton(
                        selected = permission == "view",
                        onClick = { permission = "view" }
                    )
                    Text("View", modifier = Modifier.padding(start = 8.dp))
                    RadioButton(
                        selected = permission == "edit",
                        onClick = { permission = "edit" }
                    )
                    Text("Edit", modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.shareDocument(documentId = documentId, email = email, permission = permission)
                onDismiss()
            }) {
                Text("Share")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

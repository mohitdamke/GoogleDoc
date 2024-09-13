package com.example.googledoc.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewHeadline
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val loginViewModel: LoginViewModel = viewModel()
    val context = LocalContext.current
    // Fetch the user's documents when this screen loads
    LaunchedEffect(Unit) {
        documentViewModel.fetchDocument(documentId = currentUser!!)
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = modifier.padding(top = 10.dp))
                Text(
                    text = "Google Doc",
                    fontSize = TextDim.titleTextSize,
                    fontFamily = FontDim.Bold,
                    modifier = Modifier.padding(10.dp)
                )
                Spacer(modifier = modifier.padding(top = 10.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = "Logout") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "logout"
                        )
                    },
                    selected = false,
                    onClick = {
                        loginViewModel.signOut()
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) // Clear the backstack
                        }
                    }
                )
            }
        }) {
        Scaffold(topBar = {
            CenterAlignedTopAppBar(
                title = {
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
                            modifier = Modifier
                        )
                    }
                },

                actions = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "search",
                        modifier = modifier
                            .clickable {
                                navController.navigate(Routes.Search.route)
                            }
                            .size(30.dp)
                    )
                },
                navigationIcon = {
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
                            DocumentItem(document = document,
                                onClick = {
                                    navController.navigate(
                                        Routes.View.route.replace(
                                            "{documentId}", document.documentId
                                        )
                                    )
                                },
                                onDelete = {
                                    documentViewModel.deleteDocument(document.documentId)
                                },
                                onShare = {

                                },
                                isOffline = offlineStatusMap[document.documentId] ?: false,
                                onToggleOffline = { save ->
                                    if (save) {
                                        documentViewModel.saveDocumentOffline(
                                            context = context,
                                            document = document
                                        )
                                    } else {
                                        documentViewModel.removeDocumentOffline(
                                            context = context,
                                            document = document
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}


@Composable
fun DocumentItem(
    document: Document,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    isOffline: Boolean, // Track if the document is saved offline
    onToggleOffline: (Boolean) -> Unit
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
            IconButton(onClick = { onToggleOffline(!isOffline) }) {
                Icon(
                    imageVector = if (isOffline) Icons.Default.Check else Icons.Default.Download,
                    contentDescription = if (isOffline) "Remove from Offline" else "Save Offline"
                )
            }
        }
    }
}

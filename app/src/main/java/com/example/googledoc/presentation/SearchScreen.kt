package com.example.googledoc.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.googledoc.common.FormatTimestamp
import com.example.googledoc.data.Document
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.size.FontDim
import com.example.googledoc.size.TextDim
import com.example.googledoc.viewmodel.DocumentViewModel

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val documentViewModel: DocumentViewModel = hiltViewModel()
    // State for search query and filtered documents
    var searchQuery by remember { mutableStateOf("") }
    val documents by documentViewModel.documents.observeAsState(emptyList())

    // Filter documents based on search query
    val filteredDocuments = documents.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(
            searchQuery,
            ignoreCase = true
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query -> searchQuery = query },
                placeholder = { Text("Search") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp), trailingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "search")
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (searchQuery.isEmpty()) {
                // Show placeholder or empty state
                Column(
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "Start typing to search for documents",
                        fontSize = TextDim.bodyTextSize,
                        fontFamily = FontDim.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                }
            } else if (filteredDocuments.isEmpty()) {
                Column(
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // Show no results message
                    Text(
                        text = "No documents found",
                        fontSize = TextDim.bodyTextSize,
                        fontFamily = FontDim.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                // Display filtered documents
                // Display filtered documents
                LazyColumn {
                    items(filteredDocuments) { document ->
                        DocumentItem_2(document = document, onClick = {
                            navController.navigate(
                                Routes.Edit.route.replace(
                                    "{documentId}", document.documentId
                                )
                            )
                        }, onDelete = {
                            documentViewModel.deleteDocument(document.documentId)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentItem_2(
    document: Document, onClick: () -> Unit, onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = document.title, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Last Edited: ${FormatTimestamp(document.timestamp)}")
            Spacer(modifier = Modifier.height(8.dp))
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

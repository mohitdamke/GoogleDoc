package com.example.googledoc.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.presentation.DocumentViewScreen
import com.example.googledoc.presentation.EditDocumentScreen
import com.example.googledoc.presentation.HomeScreen
import com.example.googledoc.presentation.LoginScreen
import com.example.googledoc.presentation.PdfViewerScreen
import com.example.googledoc.presentation.SearchScreen

@Composable
fun NavigationGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Login.route) {
        composable(route = Routes.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(route = Routes.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Routes.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(route = Routes.Edit.route) {
            val documentId = it.arguments?.getString("documentId") ?: "new"
            EditDocumentScreen(navController = navController, documentId = documentId)
        }
        composable(
            route = "${Routes.PdfView.route}/{pdfUri}",
            arguments = listOf(navArgument("pdfUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val pdfUriString = backStackEntry.arguments?.getString("pdfUri")
            val pdfUri = pdfUriString?.let { Uri.parse(it) } // Convert String? to Uri
            if (pdfUri != null ) {
                PdfViewerScreen(pdfUri = pdfUri)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No PDF available to display")
                }
            }

            // Replace with your actual PDF view composable
        }

        composable(route = Routes.View.route) {
            val documentId = it.arguments?.getString("documentId") ?: "new"
            DocumentViewScreen(navController = navController, documentId = documentId)
        }
    }

}
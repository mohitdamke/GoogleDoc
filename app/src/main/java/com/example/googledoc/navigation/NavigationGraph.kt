package com.example.googledoc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.presentation.DocumentViewScreen
import com.example.googledoc.presentation.EditDocumentScreen
import com.example.googledoc.presentation.HomeScreen
import com.example.googledoc.presentation.LoginScreen
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
        composable(route = Routes.View.route) {
            val documentId = it.arguments?.getString("documentId") ?: "new"
            DocumentViewScreen(navController = navController, documentId = documentId)
        }
    }

}
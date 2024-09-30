package com.example.googledoc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.presentation.EditDocumentScreen
import com.example.googledoc.presentation.HomeScreen
import com.example.googledoc.presentation.LoginScreen
import com.example.googledoc.presentation.ShareDocScreen

@Composable
fun DocNavigationGraph(documentId: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.ShareDoc.route
    ) {

        composable(route = Routes.ShareDoc.route) {
            ShareDocScreen(navController = navController, documentId = documentId?: "")
        }

        composable(route = Routes.Edit.route) {
            EditDocumentScreen(navController = navController, documentId = documentId ?: "")
        }


        composable(route = Routes.Home.route) {
            HomeScreen(navController = navController)
        }
    }}
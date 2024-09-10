package com.example.googledoc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.googledoc.presentation.HomeScreen
import com.example.googledoc.presentation.LoginScreen

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
    }

}
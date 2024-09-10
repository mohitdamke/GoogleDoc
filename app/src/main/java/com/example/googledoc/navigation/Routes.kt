package com.example.googledoc.navigation

sealed class Routes(val route : String){
    data object Home : Routes("home")
    data object Login : Routes("login")

}
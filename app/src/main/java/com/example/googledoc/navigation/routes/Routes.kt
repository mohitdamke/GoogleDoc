package com.example.googledoc.navigation.routes

sealed class Routes(val route : String){
    data object Home : Routes("home")
    data object Login : Routes("login")
    data object Search : Routes("search")
    data object Edit : Routes("edit/{documentId}")
    data object View : Routes("view/{documentId}")
    data object PdfView : Routes("pdfview/{pdfUri}")
}
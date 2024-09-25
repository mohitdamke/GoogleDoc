package com.example.googledoc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.googledoc.navigation.NavigationGraph
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.ui.theme.GoogleDocTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoogleDocTheme {
                val navController = rememberNavController()
                NavigationGraph()
                handleIncomingIntent(intent, navController)
            }
        }
    }
    private fun handleIncomingIntent(intent: Intent, navController: NavController) {
        if (Intent.ACTION_VIEW == intent.action) {
            intent.data?.let { uri ->
                // Make sure the URI is valid and pass it to the PDF viewer
                navController.navigate(Routes.PdfView.route.replace("{pdfUri}", uri.toString()))
            }
        }
    }
}
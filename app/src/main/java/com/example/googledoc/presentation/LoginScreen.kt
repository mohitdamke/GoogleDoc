package com.example.googledoc.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(navController: NavController) {
    val loginViewModel: LoginViewModel = viewModel()
    val activity = LocalContext.current as Activity

    // Check if the user is already logged in
    if (loginViewModel.isUserLoggedIn()) {
        LaunchedEffect(Unit) {
            navController.navigate(Routes.Home.route){
                popUpTo(0) // Clear backstack
            }
        }
    }

    // Result launcher to handle Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            loginViewModel.firebaseAuthWithGoogle(account) { success ->
                if (success) {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(0) // Clear backstack
                    }
                } else {
                    // Handle login failure
                    loginViewModel.showErrorMessage(activity, "Authentication failed. Please try again.")
                }
            }
        } catch (e: ApiException) {
            loginViewModel.showErrorMessage(activity, "Google Sign-In failed: ${e.localizedMessage}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to Google Doc App", fontSize = 24.sp, modifier = Modifier.padding(bottom = 32.dp))

        Button(onClick = {
            val signInIntent = loginViewModel.getGoogleSignInClient(activity).signInIntent
            launcher.launch(signInIntent)
        }) {
            Text("Sign in with Google")
        }
    }
}

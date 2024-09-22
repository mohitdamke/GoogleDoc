package com.example.googledoc.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.googledoc.R
import com.example.googledoc.navigation.routes.Routes
import com.example.googledoc.size.FontDim
import com.example.googledoc.size.TextDim
import com.example.googledoc.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException


@Composable
fun LoginScreen(modifier: Modifier = Modifier, navController: NavController) {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val activity = LocalContext.current as Activity
    val isLoading by loginViewModel.isLoading.observeAsState(false)
    val isLoggedIn by loginViewModel.isUserLoggedIn.observeAsState(initial = false)
    // Check if the user is already logged in
    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            navController.navigate(Routes.Home.route) {
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
                    loginViewModel.showErrorMessage(
                        activity, "Authentication failed. Please try again."
                    )
                }
            }
        } catch (e: ApiException) {
            loginViewModel.showErrorMessage(
                activity, "Google Sign-In failed: ${e.localizedMessage}"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = modifier.padding(top = 100.dp))
        Column(
            modifier = Modifier.padding(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

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
            Spacer(modifier = modifier.padding(top = 20.dp))

            Text(
                text = "Collaborate in real-time with your team",
                fontSize = TextDim.bodyTextSize,
                fontFamily = FontDim.Bold,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
        Spacer(modifier = modifier.padding(top = 200.dp))

        Button(
            onClick = {
                val signInIntent = loginViewModel.getGoogleSignInClient(activity).signInIntent
                launcher.launch(signInIntent)
            },
            colors = ButtonDefaults.buttonColors(Color.White), // Set button background to white
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(2.dp) // Remove button shadow for flat look
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically // Center items vertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google",
                    modifier = Modifier.size(30.dp) // Set image size
                )

                Spacer(modifier = Modifier.width(8.dp)) // Add space between image and text

                Text(
                    text = "Sign in with Google",
                    fontFamily = FontDim.Regular,
                    fontSize = TextDim.tertiaryTextSize,
                    modifier = Modifier.weight(1f), // Text takes up remaining space
                    textAlign = TextAlign.Center, // Center the text horizontally
                    color = Color.Black // Set text color to black for contrast on white button
                )
            }
                if (isLoading) {
                    Column(
                        modifier = modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }


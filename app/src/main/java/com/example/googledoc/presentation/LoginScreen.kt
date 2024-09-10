package com.example.googledoc.presentation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.googledoc.BuildConfig
import com.example.googledoc.auth.SignInViewModel
import com.example.googledoc.navigation.Routes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val authViewModel: SignInViewModel = hiltViewModel()
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val result = account.getResult(ApiException::class.java)
                val credentials = GoogleAuthProvider.getCredential(result.idToken, null)
                authViewModel.googleSignIn(credentials)
            } catch (it: ApiException) {
                print(it)
            }
        }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val googleState = authViewModel.googleState.value

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "LoginScreen")


            Spacer(modifier = Modifier.height(30.dp))

            Icon(imageVector = Icons.Default.MailOutline, contentDescription = null,
                modifier = Modifier
                    .clickable {
                        val gso =
                            GoogleSignInOptions
                                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(BuildConfig.ServerClientID)
                                .requestEmail()
                                .build()

                        val mGoogleSignInClient =
                            GoogleSignIn.getClient(context, gso)

                        launcher.launch(mGoogleSignInClient.signInIntent)
                    }
                    .size(40.dp)
            )
            Spacer(modifier = Modifier.padding(top = 20.dp))
            if (googleState.isLoading) {
                CircularProgressIndicator()
            }
            LaunchedEffect(key1 = googleState.isSuccess) {
                scope.launch {
                    if (googleState.isSuccess != null) {
                        Toast.makeText(
                            context,
                            "SignIn With Google Account is Success ",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.navigate(Routes.Home.route)

                    }
                }
            }
            LaunchedEffect(key1 = googleState.isError) {
                scope.launch {
                    if (googleState.isError == null) {
                        Toast.makeText(
                            context,
                            "Failed to SignIn With Google Account",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
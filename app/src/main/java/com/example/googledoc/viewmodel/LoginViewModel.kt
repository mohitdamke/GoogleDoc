package com.example.googledoc.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googledoc.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Google Sign-In client configuration
    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(activity, gso)
    }

    // Firebase Authentication with Google
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?, onResult: (Boolean) -> Unit) {
        account?.let {
            val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
            viewModelScope.launch {
                try {
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        onResult(task.isSuccessful)
                    }
                } catch (e: Exception) {
                    onResult(false)
                }
            }
        } ?: run {
            onResult(false)
        }
    }

    // Check if user is already logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Sign out
    fun signOut() {
        auth.signOut()
    }
}

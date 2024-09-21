package com.example.googledoc.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserLoggedIn: LiveData<Boolean> get() = _isUserLoggedIn
    // Loading state for data fetch operations
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        _isUserLoggedIn.value = auth.currentUser != null
        auth.addAuthStateListener { firebaseAuth ->
            _isUserLoggedIn.value = firebaseAuth.currentUser != null
        }
    }


    // Google Sign-In client configuration
    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        googleSignInClient = GoogleSignIn.getClient(activity, gso)

        // Force account chooser even if the user has already signed in
        googleSignInClient.signOut()

        return googleSignInClient
    }

    // Firebase Authentication with Google
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?, onResult: (Boolean) -> Unit) {
        account?.let {
            val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
            viewModelScope.launch {
                _isLoading.postValue(false)

                try {
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        onResult(task.isSuccessful)
                    }
                } catch (e: Exception) {
                    onResult(false)
                }
                finally {
                    _isLoading.postValue(true)
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

    fun showErrorMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Sign out
    fun signOut() {
        viewModelScope.launch {
            _isLoading.postValue(true)

            if (::googleSignInClient.isInitialized) {
                googleSignInClient.signOut()
            }
            auth.signOut()
            _isLoading.postValue(false)

        }
    }


}

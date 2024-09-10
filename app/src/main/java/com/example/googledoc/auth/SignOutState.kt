package com.example.googledoc.auth

data class SignOutState(
    val isLoading: Boolean = false,
    val isSuccess: String? = "",
    val isError: String? = "",
)
package com.example.bookbyte.domain.authenticationModule.models

import com.example.bookbyte.domain.userManagementModule.models.User

data class AuthResponse(
    val authenticated: Boolean,
    val user: User? = null,
    val authMessage: String? = null
)
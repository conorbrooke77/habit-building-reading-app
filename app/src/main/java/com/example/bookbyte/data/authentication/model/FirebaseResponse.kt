package com.example.bookbyte.data.authentication.model

import com.google.firebase.auth.FirebaseUser

data class FirebaseResponse(
    val authenticated: Boolean,
    val user: FirebaseUser? = null,
    val errorMessage: String? = null
)

package com.example.bookbyte.domain.authenticationModule.repository

import com.example.bookbyte.domain.authenticationModule.models.AuthResponse


interface AuthRepository {
    suspend fun signInWithUsername(username: String, password: String): AuthResponse
    suspend fun signInWithEmail(username: String, password: String): AuthResponse
    suspend fun signUpWithUsername(username: String, email: String, password: String): AuthResponse
    suspend fun signOut(): AuthResponse

}
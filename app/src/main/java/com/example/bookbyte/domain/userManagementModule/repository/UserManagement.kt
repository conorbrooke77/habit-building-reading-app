package com.example.bookbyte.domain.userManagementModule.repository

interface UserManagement {
    suspend fun sendPasswordResetEmail(email: String): Pair<Boolean, String>
    suspend fun isUniqueUsername(username: String): Boolean
}
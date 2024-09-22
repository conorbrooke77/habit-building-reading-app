package com.example.bookbyte.data.usermanagement

import com.example.bookbyte.domain.userManagementModule.repository.UserManagement
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.bookbyte.common.constants.Constants
import kotlinx.coroutines.tasks.await

class UserManagementRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val database: FirebaseDatabase
): UserManagement {

    override suspend fun sendPasswordResetEmail(email: String) : Pair<Boolean, String> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Pair(true, "Check your email to reset your password.")
        } catch (e: Exception) {
            Pair(false, "Failed to send reset email. Error: $e")
        }
    }

    override suspend fun isUniqueUsername(username: String): Boolean {
        val snapshot = database.getReference(Constants.FIREBASE_USERNAME_REFERENCE).get().await()
        return !snapshot.hasChild(username)
    }
}
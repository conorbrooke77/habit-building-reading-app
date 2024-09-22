package com.example.bookbyte.data.authentication.repository

import com.example.bookbyte.common.constants.Constants
import com.example.bookbyte.domain.authenticationModule.models.AuthResponse
import com.example.bookbyte.domain.authenticationModule.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.bookbyte.data.utils.FirebaseHelper

class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : AuthRepository {

    override suspend fun signInWithUsername(username: String, password: String): AuthResponse {

        return try {
            val databaseReference = firebaseDatabase.getReference(Constants.FIREBASE_USERNAME_REFERENCE)

            // Use the helper function to get the DataSnapshot
            val query = databaseReference.child(username).getValueForSingleEvent()

            // Check if the username exists
            if (query.exists()) {
                // Assuming email is stored as a child field under the username node
                val email = query.child("email").getValue(String::class.java)

                if (email != null) {
                    // Sign in with the email and password
                    signInWithEmail(email, password) // This should return AuthResponse
                } else {
                    AuthResponse(false, null, "Authentication failed: Email not found.")
                }
            } else {
                AuthResponse(false, null, "Authentication failed: Username not found.")
            }

        } catch (e: Exception) {
            // Return error AuthResponse in case of failure
            AuthResponse(false, null, e.message ?: "An error occurred during sign-in.")
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResponse  {
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                // Checks if the sign in was successful
//                if (task.isSuccessful)
//                    callback(true,"Sign In Successful!")
//                else
//                    callback(false,"Authentication failed!")
//            }
        return AuthResponse(false, null, "")
    }

    override suspend fun signUpWithUsername(username: String, email: String, password: String): AuthResponse  {

//        return try {
//            val authResults = auth.createUserWithEmailAndPassword(email, password).await()
//            val userId = authResults.user?.uid ?: throw Exception("User ID not found")
//
//            val user = User(username, email, userId)
//
//            databaseReference.getReference("Users").child(userId).setValue(user).await()
//            //Saving username to usernames node in database for future referencing unique usernames
//            databaseReference.getReference("usernames").child(username).setValue(true).await()
//
//            Pair(true, "Account Created. User details saved.")
//        } catch (e: Exception) {
//            auth.currentUser?.delete()?.await()
//
//            Pair(false, "Failed to create user or save details: ${e.message}")
//        }
        return AuthResponse(false, null, "")
    }

    override suspend fun signOut(): AuthResponse {
        TODO("Not yet implemented")
    }

}
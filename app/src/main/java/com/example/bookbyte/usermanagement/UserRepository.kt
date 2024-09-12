package com.example.bookbyte.usermanagement

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class UserRepository {

    private var auth = FirebaseAuth.getInstance()
    private val apiURL = "https://habit-building-reading-a-bfcfb-default-rtdb.europe-west1.firebasedatabase.app/"
    private val databaseReference = FirebaseDatabase.getInstance(apiURL)
    val currentUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

     fun loginWithUsername(username: String, password: String, callback: (Boolean, String) -> Unit) {

        databaseReference.getReference("Users").orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    // Checks if the username exists within the database
                    if (snapshot.exists()) {
                        // Since usernames are unique, this directly gets the first child
                        val userSnapshot = snapshot.children.firstOrNull()

                        // Extract the email if it exists
                        val email = userSnapshot?.child("email")?.getValue(String::class.java)

                        if (email != null)
                            loginWithEmail(email, password, callback)
                        else
                            callback(false, "Users Email Not Found!")

                    } else
                        callback(false, "Authentication failed!")
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, "Database error: ${error.message}")
                }
            })
    }

     fun loginWithEmail(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // Checks if the sign in was successful
                if (task.isSuccessful)
                    callback(true,"Sign In Successful!")
                else
                    callback(false,"Authentication failed!")
            }
    }

    suspend fun createUser(username: String, email: String, password: String) : Pair<Boolean, String> {

        return try {
            val authResults = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResults.user?.uid ?: throw Exception("User ID not found")

            val user = User(username, email, userId)

            databaseReference.getReference("Users").child(userId).setValue(user).await()
            //Saving username to usernames node in database for future referencing unique usernames
            databaseReference.getReference("usernames").child(username).setValue(true).await()

            Pair(true, "Account Created. User details saved.")
        } catch (e: Exception) {
            auth.currentUser?.delete()?.await()

            Pair(false, "Failed to create user or save details: ${e.message}")
        }
    }

    suspend fun sendPasswordResetEmail(email: String) : Pair<Boolean, String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Pair(true, "Check your email to reset your password.")
        } catch (e: Exception) {
            Pair(false, "Failed to send reset email. Error: $e")
        }
    }

    suspend fun isUniqueUsername(username: String): Boolean {
        val snapshot = databaseReference.getReference("usernames").get().await()
        return !snapshot.hasChild(username)
    }

}
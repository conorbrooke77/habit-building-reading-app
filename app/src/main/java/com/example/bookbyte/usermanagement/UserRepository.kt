package com.example.bookbyte.usermanagement

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
                    callback(true,"Login Successful!")
                else
                    callback(false,"Authentication failed!")
            }
    }

    fun createUser(username: String, email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val userId = currentUser?.uid ?: ""
                    val user = User(username, email, userId)

                    databaseReference.getReference("Users").child(userId).setValue(user).addOnCompleteListener { userTask ->
                        // User details are successfully saved in the database
                        if (userTask.isSuccessful)
                            callback(true, "Account Created. User details saved.")
                        else {
                            // Handle the error in saving user details
                            userTask.exception?.let {
                                callback(true, "Failed to save user details: ${it.message}") }
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    callback(true, "Authentication failed.")
                }
            }
    }
}
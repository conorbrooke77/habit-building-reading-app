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
            .addOnCompleteListener() { task ->
                // Checks if the sign in was successful
                if (task.isSuccessful)
                    callback(true,"Login Successful!")
                else
                    callback(false,"Authentication failed!")
            }
    }

    // Could be redundant code
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

}
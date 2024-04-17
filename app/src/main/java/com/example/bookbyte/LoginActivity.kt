package com.example.bookbyte

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.bookbyte.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var forgotPasswordBtn: Button
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var loginBtn: AppCompatButton
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth


//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            val intent = Intent(this, PdfUploadActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Use your actual layout file name

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        forgotPasswordBtn = findViewById(R.id.buttonForgotPassword)
        loginBtn = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)
        auth = FirebaseAuth.getInstance()

        loginBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isEmpty()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Username is blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Password is blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Attempt to log in
            loginWithUsername(username, password)
        }

        forgotPasswordBtn.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
    }

    private fun loginWithUsername(username: String, password: String) {
        val databaseReference = FirebaseDatabase.getInstance("https://habit-building-reading-a-bfcfb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")

        databaseReference.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var email: String? = null
                        for (userSnapshot in snapshot.children) {
                            email = userSnapshot.child("email").getValue(String::class.java)
                            break // Username is unique, get the first email found
                        }

                        if (email != null) {
                            loginWithEmail(email, password)
                        } else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(baseContext, "User not found.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(baseContext, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(baseContext, "Login Successful.", Toast.LENGTH_SHORT).show()
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        App.updateStreakIfNeeded(this)
                    }
                    // Proceed to your main activity or dashboard
                    val intent = Intent(this, UserLibraryActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun navigateToRegister(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

}
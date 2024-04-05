package com.example.bookbyte

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonRegister: AppCompatButton
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonRegister = findViewById(R.id.buttonRegister)
        progressBar = findViewById(R.id.progressBar)

        val databaseReference = FirebaseDatabase.getInstance("https://habit-building-reading-a-bfcfb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")

        auth = FirebaseAuth.getInstance()

        buttonRegister.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            var username = editTextUsername.text.toString().trim()
            var email = editTextEmail.text.toString().trim()
            var password = editTextPassword.text.toString().trim()
            var confirmPassword = editTextConfirmPassword.text.toString().trim()

            // Check if email is blank
            if (email.isEmpty()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Email is blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Regex pattern for validating the email
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            // Check if email matches the pattern
            if (!email.matches(emailPattern.toRegex())) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if password is blank
            if (password.isEmpty()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Password is blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if password is at least 8 characters long and contains at least one digit
            if (password.length < 8 || !password.any { it.isDigit() }) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Password must be at least 8 characters long and contain at least one digit", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        progressBar.visibility = View.GONE

                        val currentUser = auth.currentUser
                        val email = currentUser?.email ?: ""
                        val userId = currentUser?.uid ?: ""

                        // Assuming 'username' is obtained from your UI
                        val user = User(username, email, userId)

                        databaseReference.child(userId).setValue(user).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // User details are successfully saved in the database
                                Toast.makeText(baseContext, "Account Created. User details saved.", Toast.LENGTH_SHORT).show()
                                // Proceed to your main activity or dashboard
                                val intent = Intent(this, DifficultySelectionActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // Handle the error in saving user details
                                task.exception?.let {
                                    Log.e("FirebaseError", "Failed to save user details", it)
                                    Toast.makeText(baseContext, "Failed to save user details: ${it.message}", Toast.LENGTH_SHORT).show()
                                }                            }
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }

    fun navigateToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
}
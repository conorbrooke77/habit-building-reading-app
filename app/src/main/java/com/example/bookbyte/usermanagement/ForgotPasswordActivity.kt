package com.example.bookbyte.usermanagement

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.bookbyte.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonResetPassword: AppCompatButton
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonResetPassword = findViewById(R.id.buttonResetPassword)
        progressBar = findViewById(R.id.progressBar)
        auth = FirebaseAuth.getInstance()

        progressBar.visibility = View.GONE // Initially hide the progressBar

        buttonResetPassword.setOnClickListener {
            val email = editTextEmail.text.toString().trim()

            if (email.isEmpty()) {
                editTextEmail.error = "Email is required."
                editTextEmail.requestFocus()
                return@setOnClickListener
            }
            progressBar.visibility = View.VISIBLE
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Check your email to reset your password.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                } else {
                    Toast.makeText(baseContext, "Failed to send reset email.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun navigateToRegister(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
}
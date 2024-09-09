package com.example.bookbyte.usermanagement

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.bookbyte.App
import com.example.bookbyte.R
import com.example.bookbyte.UserLibraryActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        forgotPasswordBtn = findViewById(R.id.buttonForgotPassword)
        loginBtn = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)

        loginBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

        }
    }

    fun navigateToRegisterActivity(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
    fun navigateToForgotPasswordActivity(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

}
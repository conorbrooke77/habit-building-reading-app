package com.example.bookbyte.usermanagement

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.bookbyte.R
import com.example.bookbyte.UserLibraryActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var forgotPasswordBtn: Button
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var loginBtn: AppCompatButton
    private lateinit var progressBar: ProgressBar
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        forgotPasswordBtn = findViewById(R.id.buttonForgotPassword)
        loginBtn = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)

        viewModel.loginResult.observe(this) { result ->
            progressBar.visibility = View.GONE

            if (result.success) {
                Toast.makeText(this, "Login Successful.", Toast.LENGTH_SHORT).show()
                //TODO : Check for transition effect
                startActivity(Intent(this, UserLibraryActivity::class.java))
                finish()
            } else
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
        }

        loginBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            viewModel.login(username, password)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun navigateToRegisterActivity(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
    @Suppress("UNUSED_PARAMETER")
    fun navigateToForgotPasswordActivity(view: View) {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

}
package com.example.bookbyte.usermanagement

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import com.example.bookbyte.R

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var editTextEmail: EditText
    private lateinit var buttonResetPassword: AppCompatButton
    private lateinit var progressBar: ProgressBar

    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        editTextEmail = findViewById(R.id.editTextEmail)
        buttonResetPassword = findViewById(R.id.buttonResetPassword)
        progressBar = findViewById(R.id.progressBar)

        progressBar.visibility = View.GONE

        viewModel.forgotPasswordResults.observe(this) { results ->
            progressBar.visibility = View.GONE

            if (results.success) {
                Toast.makeText(this, results.message, Toast.LENGTH_LONG).show()

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else
                Toast.makeText(this, results.message, Toast.LENGTH_LONG).show()
        }

        buttonResetPassword.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val email = editTextEmail.text.toString().trim()
            viewModel.sendPasswordResetEmail(email)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun navigateToRegister(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
}
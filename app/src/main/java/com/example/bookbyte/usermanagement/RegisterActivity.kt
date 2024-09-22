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
import com.example.bookbyte.ui.DifficultySelectionActivity
import com.example.bookbyte.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonRegister: AppCompatButton
    private lateinit var progressBar: ProgressBar

    private val viewModel: RegisterViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonRegister = findViewById(R.id.buttonRegister)
        progressBar = findViewById(R.id.progressBar)

        viewModel.registerResults.observe(this) {results ->
            progressBar.visibility = View.GONE

            if (results.success) {
                Toast.makeText(this, results.message, Toast.LENGTH_LONG).show()
                startActivity(Intent(this, DifficultySelectionActivity::class.java))
                finish()
            } else
                Toast.makeText(this, results.message, Toast.LENGTH_LONG).show()
        }

        buttonRegister.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val username = editTextUsername.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()

            viewModel.register(username, email, password, confirmPassword)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun navigateToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
}
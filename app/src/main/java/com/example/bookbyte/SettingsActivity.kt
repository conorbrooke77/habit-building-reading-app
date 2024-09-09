package com.example.bookbyte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.bookbyte.usermanagement.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    fun navigateBack(view: View) {
        finish()
    }

    fun navigateToPDFUpload(view: View) {

        val intent = Intent(this, PdfUploadActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToGutenbergLibrary(view: View) {

        val intent = Intent(this, GutenbergLibraryActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToProfile(view: View) {

        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToStatistics(view: View) {

        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToGeneralSettings(view: View) {

        val intent = Intent(this, GeneralSettingsActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToAdjustSegments(view: View) {

        val intent = Intent(this, AdjustSegments::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToUserLibrary(view: View) {

        val intent = Intent(this, UserLibraryActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun logout(view: View) {
        // Sign out the current user
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)
        // Clearing the activity stack to prevent the user from going back to the MainActivity after logging out
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
}
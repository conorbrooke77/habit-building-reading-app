package com.example.bookbyte

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.Toast

class GeneralSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_settings)

        val switch = findViewById<Switch>(R.id.switchNotification)
        val preferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        switch.isChecked = preferences.getBoolean("NotificationsEnabled", true)

        // Set a listener on the switch to handle changes
        switch.setOnCheckedChangeListener { _, isChecked ->
            // Save the new preference state
            preferences.edit().putBoolean("NotificationsEnabled", isChecked).apply()

            // Show a toast for feedback
            if (isChecked) {
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun navigateBackToLibrary(view: View) {

        val intent = Intent(this, UserLibraryActivity::class.java)
        startActivity(intent)
        finish()
    }
}
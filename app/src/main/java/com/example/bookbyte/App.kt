package com.example.bookbyte

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        DEBUG.consoleMessage("App is Initialized")
    }
    companion object {
        fun saveReadingStreak(streak: Int, context: Context) {
            val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putInt("ReadingStreak", streak).apply()
        }

        fun getReadingStreak(context: Context): Int {
            val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            return sharedPreferences.getInt("ReadingStreak", 0) // 0 is the default value
        }
    }

}
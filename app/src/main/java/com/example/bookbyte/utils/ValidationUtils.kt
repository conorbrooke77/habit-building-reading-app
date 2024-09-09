package com.example.bookbyte.utils

import android.net.wifi.hotspot2.pps.Credential.UserCredential

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateCredentials(userCredentials: String, password: String): Boolean {
        if (userCredentials.isBlank())
            return false

        if (userCredentials.length > 64) {
            return false
        }

        return password.isNotBlank()
    }
    // Can add other validation methods here in the future
}
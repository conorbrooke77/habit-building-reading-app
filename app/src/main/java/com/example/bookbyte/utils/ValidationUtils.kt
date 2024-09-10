package com.example.bookbyte.utils

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateCredentials(userCredentials: String, password: String): Pair<Boolean, String> {
        if (userCredentials.isBlank())
            return Pair(false, "Email or Username has been left blank")

        if (userCredentials.length > 64)
            return Pair(false, "Email or Username contains too many characters")

        return Pair(password.isNotBlank(), "Password has been left blank")
    }

    fun validateCredentials(username: String, userCredentials: String, password: String, confirmPassword:String): Pair<Boolean, String> {

        if (username.isBlank())
            return Pair(false, "Username has been left blank")
        // Check if password is at least 8 characters long and shorter then 256 characters and also contains at least one digit
        if (password.length < 10)
            return Pair(false, "Password must be at least 10 characters long")

        if (password.length > 128)
            return Pair(false, "Password can't be more than 128 characters long")

        if(!password.any { it.isDigit() })
            return Pair(false, "Password must contain at least one digit")

        if (password != confirmPassword)
            return Pair(false, "Passwords do not match")

        return validateCredentials(userCredentials, password)
    }
}
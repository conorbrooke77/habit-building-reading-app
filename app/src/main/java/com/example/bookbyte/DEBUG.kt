package com.example.bookbyte

import android.content.Context
import android.widget.Toast

class DEBUG {
    companion object {

        // Just for message debugging
        fun messageBox(context: Context, message: String?) {
            Toast.makeText(context, message ?: "No message", Toast.LENGTH_SHORT).show()
        }
    }
}
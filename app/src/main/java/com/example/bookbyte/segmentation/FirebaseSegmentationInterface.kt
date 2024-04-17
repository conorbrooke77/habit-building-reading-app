package com.example.bookbyte.segmentation
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException

class FirebaseSegmentationInterface {

    fun triggerSegmentation(fileName: String) {
        // Get an instance of FirebaseFunctions
        val functions = FirebaseFunctions.getInstance("europe-west1")

        val user = Firebase.auth.currentUser
        val userId = user?.uid  // Ensure the user is not null

        // Check if user is null and handle accordingly
        if (userId == null) {
            Log.d("FirebaseSegmentationInterface", "User must be logged in to perform this operation")
            return
        }

        // Create data to send to function
        val data = hashMapOf(
            "fileName" to fileName,
            "userId" to userId
        )

        functions.getHttpsCallable("segmentPdf").call(data).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result?.data as? Map<*, *>
                    Log.d("FirebaseSegmentationInterface", "Success: ${result?.get("message")}")
                } else {
                    val exception = task.exception as? FirebaseFunctionsException
                    Log.d("FirebaseSegmentationInterface", "Error: ${exception?.message}, Code: ${exception?.code}")
                }
        }
    }
}
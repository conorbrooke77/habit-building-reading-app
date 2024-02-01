package com.example.bookbyte.segmentation

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.bookbyte.App
import com.example.bookbyte.Dashboard
import com.example.bookbyte.R
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class SegmentedTextViewerActivity(): AppCompatActivity() {

    private lateinit var completeSegmentBtn: AppCompatButton
    private lateinit var readingStreak: TextView
    private var segmentedData: String? = null
    private var secondSegment: String? = null
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segmented_text_viewer)

        readingStreak = findViewById(R.id.readingStreak)
        readingStreak.text = '0' + App.getReadingStreak(this).toString()

        if (intent.getStringExtra("secondSegmentKey") == null) {
            // Reference to your Firebase Cloud Storage bucket
            val storageRef = FirebaseStorage.getInstance().reference.child("parsedTexts")

            // List all files in the directory
            storageRef.listAll()
                .addOnSuccessListener { listResult ->
                    val files = listResult.items

                    val sortedFiles = files.sortedByDescending { it.name }
                    if (sortedFiles.isNotEmpty()) {
                        // Get the most recent file
                        val recentFile = sortedFiles.first()

                        // Download the file as a string
                        recentFile.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                            val content = String(bytes)

                            val midpoint = content.length / 2

                            // Split the string into two halves
                            val firstSegment = content.substring(0, midpoint)
                            secondSegment = content.substring(midpoint)


                            // Update the UI with the first half or as needed
                            updateUI(firstSegment)
                        }.addOnFailureListener {
                            // Handle any errors
                            Log.e("SegmentedTextViewer", "Failed to download file", it)
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("SegmentedTextViewer", "Failed to list files", exception)
                }
        } else {
            // Update the UI with the first half or as needed
            updateUI(intent.getStringExtra("secondSegmentKey"))
        }

        completeSegmentBtn = findViewById(R.id.completeSegmentBtn)

        // Set up the click listener
        completeSegmentBtn.setOnClickListener {
            showDialog()
        }
    }


    private fun updateUI(segmentedData: String?) {
        findViewById<TextView>(R.id.segmentedTextView).text = segmentedData
    }

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_complete_reading)

        val continueBtn: AppCompatButton = dialog.findViewById(R.id.continueBtn)
        continueBtn.setOnClickListener {
            updateStreak()
        }

        dialog.show()
    }

    private fun updateStreak() {
        var dailyStreak = App.getReadingStreak(this)
        App.saveReadingStreak(++dailyStreak, this)

        val intent = Intent(this, Dashboard::class.java)
        intent.putExtra("secondSegmentKey", secondSegment)
        startActivity(intent)
        finish() // Call finish() after starting the new activity

    }
}
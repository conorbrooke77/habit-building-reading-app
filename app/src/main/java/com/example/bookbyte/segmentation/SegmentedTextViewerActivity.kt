package com.example.bookbyte.segmentation

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.bookbyte.App
import com.example.bookbyte.Dashboard
import com.example.bookbyte.R
import com.example.bookbyte.SettingsActivity
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SegmentedTextViewerActivity(): AppCompatActivity() {

    private lateinit var completeSegmentBtn: AppCompatButton
    private lateinit var readingStreak: TextView
    private lateinit var hamburgerButton: ImageView
    private var segmentedData: String? = null
    private var secondSegment: String? = null
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segmented_text_viewer)

        readingStreak = findViewById(R.id.readingStreak)
        //Fix zero in-front
        readingStreak.text = App.displayReadingStreak(this)
        hamburgerButton = findViewById(R.id.hamburgerBtn)

        hamburgerButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val isLibrary = intent.getBooleanExtra("isLibrary", false)
        val title = intent.getStringExtra("title")

        if (isLibrary) {
            val storageRef = FirebaseStorage.getInstance().reference.child("books/A_Room_with_a_View_Forster__E__M___Edward_Morgan_.txt")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bytes = storageRef.getBytes(Long.MAX_VALUE).await()
                    val text = String(bytes)

                    // Split the text into words. This simple split may need to be refined based on the text content
                    val words = text.split("\\s+".toRegex()).filter { it.isNotEmpty() }

                    if (words.size >= 2300) {
                        // Take the first 1000 words and join them into a string
                        val firstSegment = words.drop(303).take(1000).joinToString(" ")
                        secondSegment = words.drop(1000).take(1000).joinToString(" ")

                        // Use the first segment as needed and save the second for later use
                        CoroutineScope(Dispatchers.Main).launch {
                            updateUI(firstSegment)
                            // Save 'secondSegment' for later use
                        }
                    } else {
                        // Handle case where the book has fewer than 2000 words
                        CoroutineScope(Dispatchers.Main).launch {
                            updateUI(words.joinToString(" "))
                            // Note: No second segment in this case
                        }
                    }
                } catch (e: Exception) {
                    // Handle errors such as file not found or network issues
                    e.printStackTrace()
                }
            }
        } else if (intent.getStringExtra("secondSegment") != null) {
            updateUI(intent.getStringExtra("secondSegment"))
        } else {
            // Reference to your Firebase Cloud Storage bucket
            val storageRef = FirebaseStorage.getInstance().reference.child("parsed")

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
                            updateUI(content)

                        }.addOnFailureListener {
                            // Handle any errors
                            Log.e("SegmentedTextViewer", "Failed to download file", it)
                        }
                    } else {
                        Log.e("SegmentedTextViewer", "No files to load")
                    }
                }.addOnFailureListener { exception ->
                    Log.e("SegmentedTextViewer", "Failed to list files", exception)
                }
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

        val intent = Intent(this, Dashboard::class.java).apply {
            putExtra("secondSegment", secondSegment)
            putExtra("title", title)

            // Pass other data as needed
        }
        startActivity(intent)
        finish() // Call finish() after starting the new activity
    }
}
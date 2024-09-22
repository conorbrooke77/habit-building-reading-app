package com.example.bookbyte.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.example.bookbyte.R
import com.example.bookbyte.app.App
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var streak: TextView
    private lateinit var segmentSize: TextView
    private lateinit var readingLevel: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var score: TextView
    private val auth = FirebaseAuth.getInstance()
    private val pages: MutableLiveData<Long> = MutableLiveData()
    private val databaseReference = FirebaseDatabase.getInstance("https://habit-building-reading-a-bfcfb-default-rtdb.europe-west1.firebasedatabase.app").reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        streak = findViewById(R.id.streak)
        segmentSize = findViewById(R.id.segment)
        readingLevel = findViewById(R.id.level)
        progressBar = findViewById(R.id.progress_reading_level)
        score = findViewById(R.id.score)

        progressBar.max = 1000

        streak.text = App.displayReadingStreak(this)
        getWordAmount() // This now just fetches the data and updates LiveData

        pages.observe(this) { count ->
            segmentSize.text = (count * 220).toString() // Assuming 220 is the multiplier you need
            updateReadingProgress(count * 220)
        }
    }

    private fun getWordAmount() {
        val userId = auth.currentUser?.uid ?: return // Exit if no user ID

        databaseReference.child("Users").child(userId).child("pageCount").get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val count = it.value as? Long ?: 0L // Safely cast to Long and default to 0 if null
                    pages.postValue(count) // Use postValue to set value on LiveData
                    Log.d("SegmentAdapter", "Successfully fetched pageCount: $count")
                } else {
                    Log.d("SegmentAdapter", "DataSnapshot does not exist at the expected path")
                    pages.postValue(0) // Default to 0 if path doesn't exist
                }
            }
            .addOnFailureListener {
                Log.e("SegmentAdapter", "Failed to update word amount", it)
                pages.postValue(0) // Default to 0 on failure
            }
    }

    private fun updateReadingProgress(wordCount: Long) {
        val ranges = listOf(1000L, 2000L, 3000L, 4000L)
        val levels = listOf("New Reader", "Average Reader", "Advanced Reader")
        val maxProgress = 1000  // Progress bar max is based on the range of 1000 words within each level

        var progress = 0

        for (i in ranges.indices) {
            if (wordCount < ranges[i]) {
                score.text = wordCount.toString() + "/" + ranges[i].toString()
                readingLevel.text = levels.getOrElse(i - 1) { "Beginner" } // Default to Beginner if below the first range
                val progressInLevel = if (i > 0) (wordCount - ranges[i - 1]) else wordCount
                progress = (progressInLevel * maxProgress / (ranges[i] - ranges.getOrElse(i - 1) { 0L })).toInt()
                break
            }
        }

        if (wordCount >= ranges.last()) {
            readingLevel.text = levels.last()
            progress = maxProgress
        }

        readingLevel.text = readingLevel.text
        progressBar.progress = progress
    }

    fun navigateBackToLibrary(view: View) {

        val intent = Intent(this, UserLibraryActivity::class.java)
        startActivity(intent)
        finish()
    }
}
package com.example.bookbyte.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.bookbyte.R
import com.example.bookbyte.segmentation.SegmentAdapter
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdjustSegments : AppCompatActivity() {
    private lateinit var seekBar: SeekBar
    private lateinit var adjustSize: MaterialButton
    private var currentWordCount = 0
    private val auth = FirebaseAuth.getInstance()
    private val pages: MutableLiveData<Long> = MutableLiveData()
    private val databaseReference = FirebaseDatabase.getInstance("https://habit-building-reading-a-bfcfb-default-rtdb.europe-west1.firebasedatabase.app").reference
    private var segmentAdapter = SegmentAdapter(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjust_segments)

        seekBar = findViewById(R.id.seekBar)
        adjustSize = findViewById(R.id.adjustSize)
        getWordAmount() // This now just fetches the data and updates LiveData

        pages.observe(this) { count ->
            initializeSeekBar(count * 220)

        }

        initializeSeekBar(2000)
        adjustSize.setOnClickListener() {
            segmentAdapter.updateWordAmountInFirebase(currentWordCount.toLong())
            Toast.makeText(this, "Word count updated to $currentWordCount", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, UserLibraryActivity::class.java)
            startActivity(intent)
            finish()
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

    private fun initializeSeekBar(originalWordAmount: Long) {
        val min = 560
        val max = 5060
        val step = 220
        val totalSteps = (max - min) / step

        seekBar.max = totalSteps
        seekBar.progress = ((originalWordAmount - min) / step).toInt()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentWordCount = min + (progress * step)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Can be used to implement any specific action on touch
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Can be used to implement any action after releasing the touch
            }
        })

    }

    fun navigateBackToLibrary(view: View) {
        val intent = Intent(this, UserLibraryActivity::class.java)
        startActivity(intent)
        finish()
    }
}
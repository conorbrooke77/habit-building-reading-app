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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SegmentedTextViewerActivity(): AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var completeSegmentBtn: AppCompatButton
    private var segmentedData: String? = null
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segmented_text_viewer)

        databaseRef = FirebaseDatabase.getInstance().getReference("/segmentedText")

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                segmentedData = dataSnapshot.getValue(String::class.java)
                updateUI(segmentedData)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SegmentedTextViewer", "Database read failed", error.toException())
            }
        }

        databaseRef.addValueEventListener(valueEventListener!!)

        completeSegmentBtn = findViewById(R.id.completeSegmentBtn)

        // Set up the click listener
        completeSegmentBtn.setOnClickListener {
            showDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        valueEventListener?.let { databaseRef.removeEventListener(it) }
    }

    private fun updateUI(segmentedData: String?) {
        val textView: TextView = findViewById(R.id.segmentedTextView)
        textView.text = segmentedData ?: getString(R.string.no_data_available)
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

        startActivity(intent)
        finish() // Call finish() after starting the new activity

    }
}
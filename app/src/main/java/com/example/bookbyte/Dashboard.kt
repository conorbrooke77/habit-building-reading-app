package com.example.bookbyte

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.bookbyte.segmentation.SegmentedTextViewerActivity

class Dashboard(): AppCompatActivity() {
    private lateinit var btn: AppCompatButton
    private lateinit var readingStreak: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        readingStreak = findViewById(R.id.readingStreak)
        readingStreak.text = App.displayReadingStreak(this)

        val secondSegment = intent.getStringExtra("secondSegment")

        btn = findViewById(R.id.button)
        // Set up the click listener
        btn.setOnClickListener {
            val intent = Intent(this, SegmentedTextViewerActivity::class.java).apply {
                putExtra("secondSegment", secondSegment)
            }
            startActivity(intent)
        }

        Log.e("Dashboard", "Has loaded!")

    }

}
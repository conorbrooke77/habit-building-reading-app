package com.example.bookbyte

import android.content.Intent
import android.os.Bundle
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
        readingStreak.text = '0' + App.getReadingStreak(this).toString()

        // Retrieve the secondSegment string
        val secondSegment = intent.getStringExtra("secondSegmentKey")
        btn = findViewById(R.id.button)

        // Set up the click listener
        btn.setOnClickListener {
            val intent = Intent(this, SegmentedTextViewerActivity::class.java)
            intent.putExtra("secondSegmentKey", secondSegment)
            startActivity(intent)
            finish()
        }
    }

}
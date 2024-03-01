package com.example.bookbyte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.example.bookbyte.segmentation.SegmentedTextViewerActivity

class BookDetailActivity : AppCompatActivity() {
    private lateinit var btn: AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        val title = intent.getStringExtra("title")
        val authors = intent.getStringExtra("authors")
        val coverUrl = intent.getStringExtra("coverUrl")

        findViewById<TextView>(R.id.bookTitleTextView).text = title
        findViewById<TextView>(R.id.bookAuthorsTextView).text = authors
        Glide.with(this).load(coverUrl).into(findViewById(R.id.bookCoverImageView))


        btn = findViewById(R.id.loadBookButton)
        // Set up the click listener
        btn.setOnClickListener {
            val intent = Intent(this, SegmentedTextViewerActivity::class.java).apply {
                putExtra("isLibrary", true)
                putExtra("title", title)

                // Pass other data as needed
            }
            startActivity(intent)
        }
    }
}
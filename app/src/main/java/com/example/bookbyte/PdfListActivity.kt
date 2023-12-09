package com.example.bookbyte

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView

class PdfListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_list)

        val listView = findViewById<ListView>(R.id.pdfListView)

        // Retrieve the stored PDF URIs
        val pdfs = intent.getStringArrayExtra("PDF_URIS")?.toList() ?: listOf()

        // Set the adapter
        val adapter = PdfListAdapter(this, pdfs)
        listView.adapter = adapter
    }
}
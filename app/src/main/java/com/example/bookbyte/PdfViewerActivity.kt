package com.example.bookbyte

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.barteksc.pdfviewer.PDFView

class PdfViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        val pdfView = findViewById<PDFView>(R.id.pdfView)

        // Get the URI of the PDF passed from the previous activity
        val pdfUriString = intent.getStringExtra("PDF_URI")
        pdfUriString?.let {
            val pdfUri = Uri.parse(it)
            pdfView.fromUri(pdfUri).load()
        }

    }
}
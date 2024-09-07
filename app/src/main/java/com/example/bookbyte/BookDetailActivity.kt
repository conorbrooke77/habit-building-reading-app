package com.example.bookbyte

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.example.bookbyte.segmentation.SegmentedTextViewerActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class BookDetailActivity : AppCompatActivity() {
    private lateinit var btn: AppCompatButton
    private var pdfFilename: String? = null
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        val title = intent.getStringExtra("title")
        val authors = intent.getStringExtra("authors")
        val coverUrl = intent.getStringExtra("coverUrl")
        pdfFilename = intent.getStringExtra("filename")
        progressBar = findViewById(R.id.progressBar)


        findViewById<TextView>(R.id.bookTitleTextView).text = title
        findViewById<TextView>(R.id.bookAuthorsTextView).text = authors
        Glide.with(this).load(coverUrl).into(findViewById(R.id.bookCoverImageView))


        btn = findViewById(R.id.loadBookButton)
        // Set up the click listener
        btn.setOnClickListener {
            btn.setBackgroundColor(Color.parseColor("#FF825A"))
            progressBar.visibility = View.VISIBLE
            pdfFilename?.let {
                downloadAndUploadPdf(it)
            }
        }
    }

    private fun downloadAndUploadPdf(filename: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val storageRef = FirebaseStorage.getInstance().reference.child("books/$filename")
                val localFile = File.createTempFile("tmp", ".pdf", cacheDir)

                // Download the file
                storageRef.getFile(localFile).await()

                // Convert to Uri and upload
                val pdfUri = Uri.fromFile(localFile)
                uploadPdfToFirebase(pdfUri)

                // Optionally delete the temp file
                localFile.delete()
            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@BookDetailActivity, "Failed to load PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun uploadPdfToFirebase(uri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        val storageRef = Firebase.storage.reference
        Log.d("BookDetailActivity", "Pdf Name: $pdfFilename")

        // A customized path and filename
        val pdfName = pdfFilename?.removeSuffix(".pdf")
        Log.d("BookDetailActivity", "Pdf Name: $pdfName")
        // Create a new reference to store the file in the format 'pdfs/pdfName/pdf.pdf'
        val pdfRef = storageRef.child("${user?.uid}/pdfs/$pdfName/${pdfFilename}")

        val metadata = StorageMetadata.Builder()
        user?.uid?.let {
            metadata.setCustomMetadata("userId", it)
        }

        // Start the upload task with metadata
        val uploadTask = pdfRef.putFile(uri, metadata.build())


        uploadTask.addOnSuccessListener {
            DEBUG.consoleMessage("Upload Successful : PdfUploadActivity")
            progressBar.visibility = View.GONE

            val intent = Intent(this, UserLibraryActivity::class.java).apply {
                putExtra("FILE_NAME", pdfName)
                putExtra("SEGMENT_INDEX", 1)
                putExtra("Source", "PdfUploadActivity");
            }
            startActivity(intent)

        }.addOnFailureListener {
            DEBUG.consoleMessage("Upload Failed : PdfUploadActivity")

        }.addOnProgressListener {
            // You can use this to show upload progress
        }
    }

}
package com.example.bookbyte.segmentation

import android.app.Dialog
import android.content.Intent
import com.github.barteksc.pdfviewer.util.FitPolicy
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bookbyte.App
import com.example.bookbyte.R
import com.example.bookbyte.SettingsActivity
import com.example.bookbyte.StatisticsUpdater
import com.example.bookbyte.UserLibraryActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class SegmentedTextViewerActivity(): AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var completeSegmentBtn: MaterialButton
    private lateinit var readingStreak: TextView
    private lateinit var hamburgerButton: ImageView
    private lateinit var segmentBtnLayout: LinearLayout
    private lateinit var fileName: String
    private var statisticsUpdater = StatisticsUpdater()
    private var segmentIndex: Int = 0
    private val user = FirebaseAuth.getInstance().currentUser
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var readOnTheSameDay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segmented_text_viewer)

        fileName = intent.getStringExtra("FILE_NAME").toString()
        segmentIndex = intent.getIntExtra("SEGMENT_INDEX", 1)

        readingStreak = findViewById(R.id.readingStreak)
        readingStreak.text = App.displayReadingStreak(this)
        hamburgerButton = findViewById(R.id.hamburgerBtn)
        completeSegmentBtn = findViewById(R.id.loadBookButton)
        pdfView = findViewById(R.id.pdfView)
        segmentBtnLayout = findViewById(R.id.segmentBtnLayout)

        startTime = System.currentTimeMillis()

        pdfView.setOnClickListener {

            // Show the button only if the last page is visible
            if (pdfView.currentPage == pdfView.pageCount - 1) {
                completeSegmentBtn.visibility = View.VISIBLE
                segmentBtnLayout.visibility = View.VISIBLE
            } else {
                completeSegmentBtn.visibility = View.GONE
                segmentBtnLayout.visibility = View.GONE
            }
        }

        hamburgerButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        completeSegmentBtn.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_complete_reading)

            val continueBtn = dialog.findViewById<MaterialButton>(R.id.continueBtn)

            continueBtn.setOnClickListener {
                updateStreak()
                segmentIndex += 1

                endTime = System.currentTimeMillis()
                val timeTaken = endTime - startTime // Duration in milliseconds

                statisticsUpdater.updateTotalSegmentsValue()

                val intent = Intent(this, UserLibraryActivity::class.java).apply {
                    putExtra("SEGMENT_INDEX", segmentIndex)
                    putExtra("FILE_NAME", fileName)
                    putExtra("Source", "SegmentedTextViewerActivity")
                    putExtra("TIME_TAKEN", timeTaken) // Passing the time taken to the next activity
                    putExtra("READ_SAME_DAY", readOnTheSameDay)
                }

                dialog.dismiss()  // Ensure dialog is dismissed before transitioning
                startActivity(intent)
                finish()  // Finish the current activity
            }

            dialog.show()
        }


        loadPdfFromFirebase("${user?.uid}/segments/${fileName}/segment ${segmentIndex}.pdf")
    }

    private fun loadPdfFromFirebase(path: String) {
        val storageRef = FirebaseStorage.getInstance().getReference(path)
        val localFile = File.createTempFile("tempPdf", "pdf")

        storageRef.getFile(localFile).addOnSuccessListener {
            renderPdf(localFile)
        }.addOnFailureListener {
            Log.e("PDFView", "Error downloading PDF", it)
        }
    }

    private fun renderPdf(file: File) {
        try {
            pdfView.fromFile(file)
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true) // double tap to zoom
                .defaultPage(0)
                .enableAnnotationRendering(false)
                .password(null)
                .scrollHandle(null)
                .pageFitPolicy(FitPolicy.WIDTH) // fit each page in the PDFView to the width
                .pageSnap(true) // snap pages to screen boundaries
                .autoSpacing(true) // add dynamic spacing to fit each page on its own on the screen
                .fitEachPage(true) // fit each page to the viewer, might be deprecated depending on version
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                .spacing(0) // spacing between pages in
                .onPageChange { page, pageCount ->
                    if (page == pageCount - 1) {
                        completeSegmentBtn.visibility = View.VISIBLE
                        segmentBtnLayout.visibility = View.VISIBLE
                    } else {
                        completeSegmentBtn.visibility = View.GONE
                        segmentBtnLayout.visibility = View.GONE
                    }
                }
                .load()
        } catch (e: Exception) {
            Log.e("PDFRender", "Error rendering PDF", e)
        }
    }

    private fun updateStreak() {
        val lastReadingStreakUpdate = App.getLastUpdate(this)
        val currentTime = System.currentTimeMillis()
        val eightHoursInMillis = 8 * 60 * 60 * 1000 // 8 hours in milliseconds - 8 * 60 * 60 * 1000


        // Check if the current time is at least 8 hours greater than the last update time
        if (currentTime - lastReadingStreakUpdate >= eightHoursInMillis) {
            var dailyStreak = App.getReadingStreak(this)
            dailyStreak++

            statisticsUpdater.updateLongestStreak(dailyStreak)
            App.saveReadingStreak(dailyStreak, this)

        } else {
            Log.d("updateStreak", "Streak update skipped: Less than 8 hours since last update.")
            readOnTheSameDay = true
        }
    }
}
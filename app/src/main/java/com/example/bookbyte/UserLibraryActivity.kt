package com.example.bookbyte

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bookbyte.segmentation.FirebaseSegmentationInterface
import com.example.bookbyte.segmentation.SegmentAdapter
import com.example.bookbyte.segmentation.SegmentedTextViewerActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class UserLibraryActivity : AppCompatActivity() {
    private lateinit var fileName: String
    private lateinit var progressBar: ProgressBar
    private var segmentAdapter = SegmentAdapter(this)
    private var statisticsUpdater = StatisticsUpdater()
    private var segmentCompletionTime: Long = 0
    private var segmentIndex: Int = 0
    private val storageReference = FirebaseStorage.getInstance().reference
    private val user = FirebaseAuth.getInstance().currentUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_library)

        progressBar = findViewById(R.id.progressBar)
        showProgressBar()

        var averageCompletionTime = -1L

        val source = intent.getStringExtra("Source");
        if ("PdfUploadActivity" == source) {
            fileName = intent.getStringExtra("FILE_NAME").toString()
            segmentIndex = intent.getIntExtra("SEGMENT_INDEX", 1)
            createSegmentInfoFile()
        } else if ("SegmentedTextViewerActivity" == source) {

            fileName = intent.getStringExtra("FILE_NAME").toString()
            segmentIndex = intent.getIntExtra("SEGMENT_INDEX", 1)
            segmentCompletionTime = intent.getLongExtra("TIME_TAKEN", 0)
            val readOnTheSameDay = intent.getBooleanExtra("READ_SAME_DAY", false)
            statisticsUpdater.checkReadingPaceStatistics(segmentCompletionTime)

            lifecycleScope.launch {
                try {
                    averageCompletionTime = statisticsUpdater.calculateAndDisplayAverage()
                    if (averageCompletionTime != -1L) { // Check if the average time is valid

                        segmentAdapter.generateSegmentSize(App.getMissedDays(this@UserLibraryActivity), App.getReadingStreak(this@UserLibraryActivity), segmentCompletionTime, averageCompletionTime, readOnTheSameDay)
                    } else {
                        Log.d("UserLibraryActivity", "Average completion time is not set yet.")
                    }
                } catch (e: Exception) {
                    Log.e("UserLibraryActivity", "Failed to fetch average time", e)
                }
            }

            checkSegmentIndexValidity()
        } else {
            loadSegmentsFromStorage()
        }
    }

    private fun checkSegmentIndexValidity() {
        val dirRef = FirebaseStorage.getInstance().reference.child("${user?.uid}/segments/$fileName")

        dirRef.listAll()
            .addOnSuccessListener { listResult ->
                // If segmentIndex is greater than the number of files, it's an error
                if (segmentIndex > listResult.items.size) {

                    //loadDialog for successful word count increase
                    if(segmentIndex != 2){
                        FirebaseSegmentationInterface().triggerSegmentation(fileName)
                        showAdaptionDialog()
                    }
                    segmentIndex = 1
                    updateSegmentInfoFile()
                } else {
                    updateSegmentInfoFile()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve segment files.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAdaptionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_segments_adapted)
        val continueButton = dialog.findViewById<MaterialButton>(R.id.continueBtn)
        continueButton.setOnClickListener {

            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateSegmentInfoFile() {
        val fileRef = storageReference.child("${user?.uid}/segments_info/$fileName.txt")
        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val content = String(bytes)
            val parts = content.split(" ")
            if (parts.size >= 2) {
                val updatedContent = "${parts[0]} $segmentIndex"
                fileRef.putBytes(updatedContent.toByteArray()).addOnSuccessListener {
                    loadSegmentsFromStorage()  // Call to load after successful update
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to update segment info.", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load existing segment info for update.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createSegmentInfoFile() {
        val fileInfo = "$fileName $segmentIndex"
        val fileRef = storageReference.child("${user?.uid}/segments_info/$fileName.txt")

        fileRef.putBytes(fileInfo.toByteArray())
            .addOnSuccessListener {
                // Successfully written file
                Toast.makeText(this, "Segment info file created.", Toast.LENGTH_SHORT).show()
                loadSegmentsFromStorage()  // Call to load the newly added segment info
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create segment info file.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSegmentsFromStorage() {
        val listRef = FirebaseStorage.getInstance().reference.child("${user?.uid}/segments_info/")
        listRef.listAll()
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->
                    item.downloadUrl.addOnSuccessListener { uri ->
                        // Here you can either download the file to read its contents or directly use the file name
                        displayPdfSegment(item.name, uri.toString())
                    }
                }
                hideProgressBar()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve segment info", Toast.LENGTH_SHORT).show()
            }
    }
    private fun displayPdfSegment(fileName: String, fileUri: String) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(fileUri)
        ref.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            // Convert bytes to string, assuming text file contains "filename segmentIndex"
            val content = String(bytes)
            val parts = content.split(" ")
            if (parts.size >= 2) {
                val fileName = parts[0]
                val segmentIndexFromFile = parts[1].toIntOrNull() ?: 0

                val (author, pdfName) = processFileName(fileName)
                // Assuming you have a LinearLayout to add views to
                val layout = findViewById<LinearLayout>(R.id.pdfContainer)
                val itemView = LayoutInflater.from(this).inflate(R.layout.segment_item, layout, false)

                // Update the views in the item layout
                itemView.findViewById<TextView>(R.id.pdf_file_name).text = pdfName
                itemView.findViewById<TextView>(R.id.pdf_author).text = "By $author"

                // Set the click listener for the item layout
                itemView.setOnClickListener {
                    navigateToSegmentedTextViewer(fileName, segmentIndexFromFile)
                }

                // Add the completed item layout to the container
                layout.addView(itemView)
            }
        }.addOnFailureListener {
            // Handle error in reading the file
            Toast.makeText(this, "Failed to load segment info", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processFileName(fileName: String): Pair<String, String> {
        val parts = fileName.split("-")

        // Assuming the author's name is always the first two parts split by '-'
        val author = if (parts.size >= 2) "${parts[0]} ${parts[1]}" else fileName

        // Combine the rest of the parts to form the full title and then take the first 50 characters
        val fullTitle = parts.drop(2).joinToString("-").dropLast(4)  // Drop the last 4 characters to remove '.pdf'
        var pdfName = if (fullTitle.length > 50) fullTitle.substring(0, 40) else fullTitle
        pdfName = "$pdfName..."
        return Pair(author, pdfName)
    }

    private fun navigateToSegmentedTextViewer(fileName: String, segmentIndexFromFile: Int) {
        val intent = Intent(this, SegmentedTextViewerActivity::class.java).apply {
            putExtra("FILE_NAME", fileName)
            putExtra("SEGMENT_INDEX", segmentIndexFromFile)
        }
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }


    fun navigateToSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }
}
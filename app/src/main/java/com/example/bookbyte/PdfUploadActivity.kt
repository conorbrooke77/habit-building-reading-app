package com.example.bookbyte

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bookbyte.segmentation.SegmentedTextViewerActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

class PdfUploadActivity : AppCompatActivity() {

//    private lateinit var browseFiles: AppCompatButton
//    private lateinit var hamburgerButton: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var uploadBtn: MaterialButton


//    private val pickPdfResultLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()) { result ->
//
//        if (result.resultCode == RESULT_OK) {
//            result.data?.data?.also { uri: Uri ->
//                uploadPdfToFirebase(uri)
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_upload)

        grantPermissionToReadFromStorage()

//        browseFiles = findViewById(R.id.button_browseFiles)
//        hamburgerButton = findViewById(R.id.hamburger_btn)
//        progressBar = findViewById(R.id.progressBar)
//
//        progressBar.visibility = View.GONE // Initially, the progress bar is not visible
//
//
//        browseFiles.setOnClickListener {
//            pickPdfFile()
//        }
//
//        hamburgerButton.setOnClickListener {
//            openSettingsActivity()
//        }
        uploadBtn = findViewById(R.id.upload_btn)
        uploadBtn.setOnClickListener {

            // When you want to show the custom dialog
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.custom_warning_dialog)

//            val etTotalWords = dialog.findViewById<EditText>(R.id.etTotalWords)
//            val btnIncreaseWordCount = dialog.findViewById<Button>(R.id.btnIncreaseWordCount)
//            val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
//
//            btnIncreaseWordCount.setOnClickListener {
//                // Logic to increase word count
//            }
//
//            btnConfirm.setOnClickListener {
//                // Logic to confirm the action
//                dialog.dismiss()
//            }

            dialog.show()
        }

    }

    private fun openSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
//    private fun pickPdfFile() {
//        val chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
//            type = "application/pdf"
//        }
//
//        pickPdfResultLauncher.launch(chooseFileIntent)
//    }

    private fun uploadPdfToFirebase(uri: Uri) {
        val storageRef = Firebase.storage.reference

        progressBar.visibility = View.VISIBLE // Show the progress bar when upload starts

        // A customized path and filename
        val pdfRef = storageRef.child("pdfs/${System.currentTimeMillis()}.pdf")
        val uploadTask = pdfRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            DEBUG.consoleMessage("Upload Successful : PdfUploadActivity")
            progressBar.visibility = View.GONE // Hide the progress bar on failure

            val intent = Intent(this, SegmentedTextViewerActivity::class.java)
            startActivity(intent)

        }.addOnFailureListener {
            DEBUG.consoleMessage("Upload Failed : PdfUploadActivity")
            progressBar.visibility = View.GONE // Hide the progress bar on failure

        }.addOnProgressListener { taskSnapshot ->
            // You can use this to show upload progress
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            progressBar.progress = progress
        }
    }

// Grant Permission Functionality
    companion object {
        private const val PERMISSIONS_REQUEST_TO_READ_STORAGE = 1
    }
    private fun grantPermissionToReadFromStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionExplanationAndRequest()
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_TO_READ_STORAGE
                )
            }
        }
    }

    private fun showPermissionExplanationAndRequest() {

        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("Permission Required")
            setMessage("This app requires access to your external storage to function properly. Please grant the permission.")
            setPositiveButton("OK") { _, _ ->
                // Try again to request the permission
                ActivityCompat.requestPermissions(
                    this@PdfUploadActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_TO_READ_STORAGE
                )
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }
        // Show the AlertDialog
        val dialog = builder.create()
        dialog.show()
    }
}
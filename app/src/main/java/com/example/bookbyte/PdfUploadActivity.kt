package com.example.bookbyte

import android.Manifest
import android.app.ActivityOptions
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bookbyte.segmentation.SegmentedTextViewerActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.storage

class PdfUploadActivity : AppCompatActivity() {

    private lateinit var btnBrowse: MaterialButton
    private lateinit var btnUpload: MaterialButton
    private lateinit var documentName: TextView
    private var pdfFilename: String? = null
    private var pdfUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_upload)
        grantPermissionToReadFromStorage()

        btnUpload = findViewById(R.id.btnUpload)
        btnBrowse = findViewById(R.id.btnBrowse)
        documentName = findViewById(R.id.documentName)

        btnBrowse.setOnClickListener {
            pickPdfFile()
            //add code below to change the background tint color of the button to #FF825A
            btnBrowse.setBackgroundColor(Color.parseColor("#FF825A"))
            btnUpload.setBackgroundColor(Color.parseColor("#FF602E"))
        }

        // Upload PDF dialog box
        btnUpload.setOnClickListener {
            // When you want to show the custom dialog
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.custom_warning_dialog)

            val btnConfirmUpload = dialog.findViewById<MaterialButton>(R.id.btnConfirmUpload)
            val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)


            btnConfirmUpload.setOnClickListener {
                uploadPdfToFirebase(pdfUri!!)
                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun pickPdfFile() {
        val chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
        }

        pickPdfResultLauncher.launch(chooseFileIntent)
    }

    private val pickPdfResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == RESULT_OK) {
            result.data?.data?.also { uri: Uri ->
                pdfFilename = getFileName(uri)
                documentName.text = pdfFilename
                pdfUri = uri
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex >= 0) {  // Check if the column index is valid
                        result = cursor.getString(columnIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.lastPathSegment  // Fallback to getting the last segment of the path
        }
        return result
    }

    private fun uploadPdfToFirebase(uri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        val storageRef = Firebase.storage.reference

        // A customized path and filename
        val pdfName = pdfFilename?.removeSuffix(".pdf")

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

    fun navigateToLibrary(view: View) {
        val intent = Intent(this, UserLibraryActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
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
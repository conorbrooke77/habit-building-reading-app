package com.example.bookbyte

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bookbyte.PDF.PdfListActivity


class MainActivity : AppCompatActivity() {

    companion object {
        // Permission request
        private const val PERMISSIONS_REQUEST_TO_READ_STORAGE = 1
    }

    // The activity result launcher: URI: it's a unique sequence of characters (string) that identifies a resource e.g PDF
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            uri: Uri? -> uri?.let {
        savePdf(uri)
    }
    }

    private lateinit var browseFiles: AppCompatButton
    private lateinit var readingMaterialsButton: AppCompatButton
    private lateinit var hamburgerButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        browseFiles = findViewById<AppCompatButton>(R.id.button_browseFiles)
        readingMaterialsButton = findViewById<AppCompatButton>(R.id.button_showMaterial)
        hamburgerButton = findViewById<ImageView>(R.id.hamburger_btn)

        grantPermissionToReadFromStorage()

        browseFiles.setOnClickListener {
            launcher.launch("application/pdf")
        }

        readingMaterialsButton.setOnClickListener {
            createPdfListActivity()
        }

        hamburgerButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createPdfListActivity() {
        val pdfUris = getAllPdfs().toTypedArray()

//      Creates an Intent for starting a new activity (PdfListActivity).
//      'this' refers to the current Context (usually the current Activity), and PdfListActivity is the class of the activity to start.
        val intent = Intent(this, PdfListActivity::class.java)

//      Puts the array of PDF URIs into the intent with a key "PDF_URIS". This allows the PdfListActivity to access this array of URIs.
        intent.putExtra("PDF_URIS", pdfUris)

//      Starts PdfListActivity Activity and passes the intent to it, so it can use the data (the PDF URIs)
        startActivity(intent)
    }

    private fun grantPermissionToReadFromStorage() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                Companion.PERMISSIONS_REQUEST_TO_READ_STORAGE
            )
        }
    }

    private fun savePdf(uri: Uri) {
        // Get SharedPreferences instance and create new file "pdf_storage"
        val localUserStorage = getSharedPreferences("pdf_storage", MODE_PRIVATE)

        // Retrieve the existing URIs (if any)
        val alreadyLoadedPDFs = localUserStorage.getStringSet("uris", mutableSetOf()) ?: mutableSetOf()

        // Add the new URI to the set
        alreadyLoadedPDFs.add(uri.toString())

        // Save the updated set back to SharedPreferences
        val sharedPreferenceEditor = localUserStorage.edit()
        sharedPreferenceEditor.putStringSet("uris", alreadyLoadedPDFs)
        sharedPreferenceEditor.apply()
    }

    //Gets a the PDFs from the
    private fun getAllPdfs(): Set<String> {
        var sharedPrefs = getSharedPreferences("pdf_storage", MODE_PRIVATE)
        return sharedPrefs.getStringSet("uris", mutableSetOf()) ?: mutableSetOf()
    }

}

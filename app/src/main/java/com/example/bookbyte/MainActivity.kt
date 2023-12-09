package com.example.bookbyte

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bookbyte.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var browseFiles: AppCompatButton
    private lateinit var readingMaterialsButton: AppCompatButton
    private lateinit var hamburgerButton: ImageView

    // Permission request
    private val PERMISSIONS_REQUEST_TO_READ_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        browseFiles = findViewById<AppCompatButton>(R.id.button_browseFiles)
        readingMaterialsButton = findViewById<AppCompatButton>(R.id.button_showMaterial)
        hamburgerButton = findViewById<ImageView>(R.id.hamburger_btn)

        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_TO_READ_STORAGE
            )
        }

        browseFiles.setOnClickListener {
            launcher.launch("application/pdf")
        }

        readingMaterialsButton.setOnClickListener {
            messageBox("Click")
            val pdfUris = getAllPdfs().toTypedArray()
            val intent = Intent(this, PdfListActivity::class.java)
            intent.putExtra("PDF_URIS", pdfUris)
            startActivity(intent)
        }

        hamburgerButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    // The activity result launcher
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        uri: Uri? -> uri?.let {
            savePdf(uri)
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

    private fun getAllPdfs(): Set<String> {
        var sharedPrefs = getSharedPreferences("pdf_storage", MODE_PRIVATE)
        return sharedPrefs.getStringSet("uris", mutableSetOf()) ?: mutableSetOf()
    }

    // Just for debugging
    private fun messageBox(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Checks storage read permissions are granted
    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSIONS_REQUEST_TO_READ_STORAGE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launcher.launch("application/pdf")
            }
        }
    }

}
package com.example.bookbyte

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bookbyte.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {
        // Identifier for the permission request
        private const val PERMISSIONS_REQUEST_TO_READ_STORAGE = 1
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var browseFiles: AppCompatButton

    // Define the activity result launcher
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            savePdfUri(uri)
        }
    }

    private fun savePdfUri(uri: Uri) {
        // Get SharedPreferences instance
        val sharedPrefs = getSharedPreferences("pdf_storage", MODE_PRIVATE)

        // Retrieve the existing URIs (if any)
        val uris = sharedPrefs.getStringSet("uris", mutableSetOf()) ?: mutableSetOf()

        // Add the new URI to the set
        uris.add(uri.toString())

        // Save the updated set back to SharedPreferences
        sharedPrefs.edit().apply {
            putStringSet("uris", uris)
            apply()
        }
    }

    private fun getAllPdfUris(): Set<String> {
        val sharedPrefs = getSharedPreferences("pdf_storage", MODE_PRIVATE)
        return sharedPrefs.getStringSet("uris", mutableSetOf()) ?: mutableSetOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_TO_READ_STORAGE
            )
        }

        permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_TO_READ_STORAGE
            )
        }
        browseFiles = findViewById<AppCompatButton>(R.id.button_browseFiles)

        browseFiles.setOnClickListener {
            launcher.launch("application/pdf")
        }

    }

    private fun messageBox(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_TO_READ_STORAGE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launcher.launch("application/pdf")
            }
            else -> {

            }
        }
    }

}
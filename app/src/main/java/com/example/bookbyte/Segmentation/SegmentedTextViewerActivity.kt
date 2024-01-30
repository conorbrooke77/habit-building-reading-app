package com.example.bookbyte.Segmentation

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bookbyte.DEBUG
import com.example.bookbyte.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SegmentedTextViewerActivity(private val segmentWordSize: Int): AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var segmentedData: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segmented_text_viewer)

        // Initialize the database reference
        databaseRef = FirebaseDatabase.getInstance().getReference("/segmentedText")

        // Attach a listener to read the data
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and whenever data changes
                DEBUG.consoleMessage("Database Data Change Made")
                segmentedData = dataSnapshot.getValue(String::class.java).toString()
            }

            override fun onCancelled(error: DatabaseError) {
                DEBUG.consoleMessage("Database Failed To Read Content")

            }
        })
    }



    private fun updateUI(segmentedData: String?) {
        // Update your UI with the segmented data
        val textView: TextView = findViewById(R.id.segmentedTextView)
        textView.text = segmentedData ?: "No data available"
    }

}
package com.example.bookbyte

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DifficultySelectionActivity : AppCompatActivity() {
    private lateinit var buttonContinue: AppCompatButton
    private lateinit var seekBar: SeekBar
    private lateinit var circleEasy: View
    private lateinit var circleMedium: View
    private lateinit var circleHard: View
    private lateinit var buttonCustom: AppCompatButton
    private var wordAmount: Int = 0

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_selection)

        buttonContinue = findViewById(R.id.buttonContinue)
        seekBar = findViewById(R.id.seekBar)
        buttonCustom = findViewById(R.id.buttonCustom)

        circleEasy = findViewById(R.id.circleEasy)
        circleMedium = findViewById(R.id.circleMedium)
        circleHard = findViewById(R.id.circleHard)

        auth = FirebaseAuth.getInstance()

        buttonCustom.setOnClickListener {

            // When you want to show the custom dialog
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.custom_dialog)

            val totalWordsContainer = dialog.findViewById<EditText>(R.id.etTotalWords)
            val btnIncreaseWordCount = dialog.findViewById<Button>(R.id.btnIncreaseWordCount)
            val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)

            btnIncreaseWordCount.setOnClickListener {
                val currentCount = totalWordsContainer.text.toString().toIntOrNull()
                    ?: 0 // Get the current count or default to 0 if null
                val newCount = currentCount + 10 // Increase the count by 10
                totalWordsContainer.setText(newCount.toString())
            }


            btnConfirm.setOnClickListener {
                val wordCount = totalWordsContainer.text.toString().toIntOrNull()

                if (wordCount == null || wordCount > 10000) {
                    // Show a toast message if the value is not a number or is greater than 10000
                    Toast.makeText(
                        this,
                        "Please enter a number less than 10001.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Request focus back on the totalWordsContainer EditText
                    totalWordsContainer.requestFocus()
                } else {
                    wordAmount = totalWordsContainer.text.toString().toIntOrNull() ?: 0
                    val databaseReference = FirebaseDatabase.getInstance("https://habit-building-reading-a-bfcfb-default-rtdb.europe-west1.firebasedatabase.app/").reference

                    val userId = auth.currentUser?.uid
                    if (userId != null) {

                        databaseReference.child("Users").child(userId).child("wordAmount")
                            .setValue(wordAmount)
                            .addOnSuccessListener {
                                Log.d("Database", "Word amount saved successfully")
                                // Handle success, perhaps inform the user
                            }
                            .addOnFailureListener {
                                Log.d("Database", "Failed to save word amount")
                                // Handle failure, perhaps inform the user
                            }
                    }

                    dialog.dismiss()
                }
            }
            dialog.show()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Set the background resource for the circles based on the progress
                circleEasy.setBackgroundResource(if (progress == 0) R.drawable.circle_shape_active else R.drawable.circle_shape)
                circleMedium.setBackgroundResource(if (progress == 1) R.drawable.circle_shape_active else R.drawable.circle_shape)
                circleHard.setBackgroundResource(if (progress == 2) R.drawable.circle_shape_active else R.drawable.circle_shape)

                // Set wordAmount based on the progress position
                wordAmount = when (progress) {
                    0 -> 1000
                    1 -> 2000
                    2 -> 3000
                    else -> wordAmount // Keep the current value if progress is out of expected range
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        buttonContinue.setOnClickListener {
            Toast.makeText(
                this,
                wordAmount.toString(),
                Toast.LENGTH_SHORT,
            ).show()
            val intent = Intent(this, PdfUploadActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
    }
}
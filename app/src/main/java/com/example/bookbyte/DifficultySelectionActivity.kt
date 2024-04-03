package com.example.bookbyte

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatButton

class DifficultySelectionActivity : AppCompatActivity() {
    private lateinit var buttonContinue: AppCompatButton
    private lateinit var seekBar: SeekBar
    private lateinit var circleEasy: View
    private lateinit var circleMedium: View
    private lateinit var circleHard: View
    private lateinit var buttonCustom: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_selection)

        buttonContinue = findViewById(R.id.buttonContinue)
        seekBar = findViewById(R.id.seekBar)
        buttonCustom = findViewById(R.id.buttonCustom)

        circleEasy = findViewById(R.id.circleEasy)
        circleMedium = findViewById(R.id.circleMedium)
        circleHard = findViewById(R.id.circleHard)

        buttonCustom.setOnClickListener {

            // When you want to show the custom dialog
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.custom_dialog)

            val etTotalWords = dialog.findViewById<EditText>(R.id.etTotalWords)
            val btnIncreaseWordCount = dialog.findViewById<Button>(R.id.btnIncreaseWordCount)
            val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)

            btnIncreaseWordCount.setOnClickListener {
                // Logic to increase word count
            }

            btnConfirm.setOnClickListener {
                // Logic to confirm the action
                dialog.dismiss()
            }

            dialog.show()
        }

        buttonContinue.setOnClickListener {
            val intent = Intent(this, PdfUploadActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                circleEasy.setBackgroundResource(if (progress == 0) R.drawable.circle_shape_active else R.drawable.circle_shape)
                circleMedium.setBackgroundResource(if (progress == 1) R.drawable.circle_shape_active else R.drawable.circle_shape)
                circleHard.setBackgroundResource(if (progress == 2) R.drawable.circle_shape_active else R.drawable.circle_shape)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

    }
}
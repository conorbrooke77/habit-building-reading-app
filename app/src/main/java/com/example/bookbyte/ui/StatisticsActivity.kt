package com.example.bookbyte.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.bookbyte.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class StatisticsActivity : AppCompatActivity() {
    private lateinit var lineChart: LineChart
    private lateinit var fastestTime: TextView
    private lateinit var slowestTime: TextView
    private lateinit var totalSegmentsText: TextView
    private lateinit var longestStreakText: TextView

    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Global variables to hold content from files
    private var readingAverage: List<Float> = listOf()
    private val readingPace = MutableLiveData<Pair<String, String>>()
    private val totalSegments = MutableLiveData<Int>()
    private val longestStreak = MutableLiveData<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        fastestTime = findViewById(R.id.fastestTime)
        slowestTime = findViewById(R.id.slowestTime)
        totalSegmentsText = findViewById(R.id.totalSegments)
        longestStreakText = findViewById(R.id.longestStreak)
        lineChart = findViewById(R.id.chart)

        if (auth.currentUser != null) {
            val userId = auth.currentUser!!.uid
            fetchLongestStreak("$userId/statistics/longestStreak.txt")
            fetchReadingAverage("$userId/statistics/readingAverage.txt")
            fetchReadingPace("$userId/statistics/readingPace.txt")
            fetchTotalSegments("$userId/statistics/totalSegments.txt")
        }
        setupLineChart()

        observeData()
    }

    private fun observeData() {
        readingPace.observe(this, Observer { pace ->
            slowestTime.text = pace.first
            fastestTime.text = pace.second
        })

        totalSegments.observe(this, Observer { segments ->
            totalSegmentsText.text = segments.toString()
        })

        longestStreak.observe(this, Observer { streak ->
            longestStreakText.text = streak.toString()
        })
    }

    private fun fetchReadingAverage(path: String) {
        val storageRef = storage.reference.child(path)
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val content = String(bytes)
            processReadingAverage(content)
        }.addOnFailureListener {
            // Handle errors
        }
    }
    private fun processReadingAverage(data: String) {
        readingAverage = data.trim().split(" ").map {
            it.toFloat() / 60000f  // Convert each value from milliseconds to minutes
        }
        setupLineChart()  // Call this method to update chart based on reading average
    }

    private fun fetchLongestStreak(path: String) {
        val storageRef = storage.reference.child(path)
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val content = String(bytes)
            val streak = content.trim().toInt()
            longestStreak.postValue(streak)  // Post the value to MutableLiveData
        }.addOnFailureListener {
            // Handle errors, e.g., show error message or log
        }
    }

    private fun fetchReadingPace(path: String) {
        val storageRef = storage.reference.child(path)
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val content = String(bytes)
            val values = content.trim().split(" ").map { it.toFloat() }
            if (values.size >= 2) {
                // Convert both the slowest and fastest times from milliseconds to "minutes:seconds" format
                val slowestTime = millisecondsToMinutesSeconds(values[0])
                val fastestTime = millisecondsToMinutesSeconds(values[1])
                readingPace.postValue(Pair(slowestTime, fastestTime))  // Update MutableLiveData with formatted times
            } else {
                // Handle unexpected data format
            }
        }.addOnFailureListener {
            // Handle errors
        }
    }


    private fun millisecondsToMinutesSeconds(milliseconds: Float): String {
        val totalSeconds = (milliseconds / 1000).toInt()  // Convert milliseconds to total seconds
        val minutes = totalSeconds / 60  // Find the number of full minutes
        val seconds = totalSeconds % 60  // Find the remaining seconds
        return String.format("%d:%02d", minutes, seconds)  // Format as "minutes:seconds"
    }


    private fun fetchTotalSegments(path: String) {
        val storageRef = storage.reference.child(path)
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val content = String(bytes)
            val segments = content.trim().toInt()
            totalSegments.postValue(segments)  // Post the value to MutableLiveData
        }.addOnFailureListener {
            // Handle errors
        }
    }

    private fun setupLineChart() {
        val entries = ArrayList<Entry>() // List of Entry objects, each representing one point in the graph

        // Use actual data from readingAverage
        readingAverage.forEachIndexed { index, readingTimeInMinutes ->
            entries.add(Entry(index.toFloat(), readingTimeInMinutes))
        }

        val dataSet = LineDataSet(entries, "Segment Reading Time") // Add entries to dataset
        dataSet.color = ContextCompat.getColor(this, R.color.colorSecondary)
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.black)
        dataSet.setDrawValues(false) // Disable drawing the text values at each point
        dataSet.setDrawCircles(false) // Disable drawing the circle indicators at each point

        // Customize dataset and chart as needed
        dataSet.lineWidth = 2.5f

        // Set XAxis to the bottom
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        // Optional: Style the xAxis as needed
        xAxis.axisLineWidth = 2.5f
        xAxis.setDrawGridLines(false)  // Optionally remove grid lines

        xAxis.textSize = 13f // Set the font size for the X-axis labels

        // Customize Left YAxis
        lineChart.axisLeft.textSize = 13f // Set the font size for the left Y-axis labels
        lineChart.legend.textSize = 13f // Adjust the legend text size
        lineChart.axisLeft.setDrawGridLines(false) // Optional: disable grid lines on the left Y-axis too

        // Customize Right YAxis
        lineChart.axisRight.textSize = 13f // Set the font size for the right Y-axis labels (if enabled)
        // Customize the left YAxis
        lineChart.axisLeft.axisLineWidth = 2.5f

        // Customize the right YAxis
        lineChart.axisRight.isEnabled = false  // Optionally disable the right Y axis

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.invalidate() // Refresh the chart
    }

    fun navigateBackToLibrary(view: View) {

        val intent = Intent(this, UserLibraryActivity::class.java)
        startActivity(intent)
        finish()
    }
}
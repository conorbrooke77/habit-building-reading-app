package com.example.bookbyte.segmentation

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.ceil
import kotlin.math.roundToInt

class SegmentAdapter(private val lifecycleOwner: LifecycleOwner) {  // Pass lifecycleOwner if used from an Activity or Fragment
    private val auth = FirebaseAuth.getInstance()
    private val pages: MutableLiveData<Long> = MutableLiveData()
    private val databaseReference = FirebaseDatabase.getInstance("https://habit-building-reading-a-bfcfb-default-rtdb.europe-west1.firebasedatabase.app").reference

    init {
        fetchWordAmount()
    }

    private fun fetchWordAmount() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.d("SegmentAdapter", "User is not logged in")
            pages.value = 0  // Handle the case where the user is not logged in
            return
        }
        Log.d("SegmentAdapter", "Attempting to fetch pageCount for user ID: $userId")
        databaseReference.child("Users").child(userId).child("pageCount")
            .get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val count = dataSnapshot.value as Long
                    pages.value = count
                    Log.d("SegmentAdapter", "Successfully fetched pageCount: $count")
                } else {
                    Log.d("SegmentAdapter", "DataSnapshot does not exist at the expected path")
                    pages.value = 0
                }
            }
            .addOnFailureListener {
                pages.value = 0  // Handle failure
                Log.e("SegmentAdapter", "Failed to fetch pageCount", it)
            }
    }

    fun generateSegmentSize(daysMissed: Int, currentStreak: Int, timeToCompleteSegment: Long, averageCompletionTime: Long, sameDay: Boolean) {
        pages.observe(lifecycleOwner, Observer { pageAmount ->
            Log.d("SegmentAdapter", "Observed wordAmount: $pageAmount")
            val updatedWordAmount = calculateNewWordAmount((pageAmount*220), daysMissed, currentStreak, timeToCompleteSegment,averageCompletionTime, sameDay)
            updateWordAmountInFirebase(updatedWordAmount)
        })
    }

    private fun calculateNewWordAmount(
        currentWordAmount: Long,
        daysMissed: Int,
        currentStreak: Int,
        timeToCompleteSegment: Long,
        averageCompletionTime: Long,
        sameDay: Boolean
    ): Long {
        // Base word
        Log.d("SegmentAdapter", "Current Word Amount: $currentWordAmount")

        var newWordAmount = currentWordAmount+40
        Log.d("SegmentAdapter", "Average completion time: $averageCompletionTime")
        Log.d("SegmentAdapter", "Last completion time: $timeToCompleteSegment")
        Log.d("SegmentAdapter", "Missed Days: $daysMissed")
        Log.d("SegmentAdapter", "Current Streak: $currentStreak")
        Log.d("SegmentAdapter", "Same day : $sameDay")

        if (sameDay) {
            newWordAmount += 60// Add 100 words for reading on the same day
        }
        //Divide the newWordAmount by 220 to get the number of pages but round up to the nearest whole number
        // val averageCompletionTime = (ceil(newWordAmount.toDouble() / 220).toLong() * 2) * 60 * 1000 // 2 minutes per page as an example average time

        // Calculate percentage difference from average time
        val timeDifference = timeToCompleteSegment - averageCompletionTime
        val percentageDifference = timeDifference.toDouble() / averageCompletionTime.toDouble()
        Log.d("SegmentAdapter", "Current Word Amount: $newWordAmount")

        // Adjust based on the time taken to complete the last segment
        if (percentageDifference > 0.3) {
            // Took much longer than average, decrease word count by 100 words
            newWordAmount -= 80
        } else if (percentageDifference > 0 && percentageDifference <= 0.3) {
            // Took slightly longer than average, decrease word count by 50 words
            newWordAmount -= 50
        } else if (percentageDifference < -0.3 && newWordAmount < 5000) {
            // Took much less time than average, increase word count by 150 words
            newWordAmount += 150
        } else if (percentageDifference < -0.2 && newWordAmount < 5000) {
            // Took less time than average by a significant margin, increase word count by 100 words
            newWordAmount += 100
        } else if (percentageDifference < -0.1 && newWordAmount < 5000) {
            // Took slightly less time than average, increase word count by 50 words
            newWordAmount += 50
        }
        Log.d("SegmentAdapter", "Current Word Amount after time adjustment: $newWordAmount")


// Reward for streak milestones every 10 days
        if (currentStreak % 5 == 0 && currentStreak != 0) {
            newWordAmount += 50 // Add 50 words for each 10-day milestone
        }

// Adjust based on streak consistency
        if (currentStreak > 10) {
            newWordAmount += 100 // Add 40 words as a reward for maintaining a streak longer than 5 days
        } else if (currentStreak > 5) {
            newWordAmount += 80 // Add 40 words as a reward for maintaining a streak longer than 5 days
        } else if (currentStreak > 2) {
            newWordAmount += 60 // Add 20 words for breaking a streak shorter than 5 days
        }

        // Adjust for missed days but prevent drastic reductions
        if (daysMissed in 1..3) { // Soft limit for missed days
            newWordAmount -= 30 * daysMissed // Decrement by 30 words per missed day
        } else if (daysMissed in 4..9) {
            newWordAmount -= 200 // Cap reduction after prolonged inactivity
        } else if (daysMissed in 11..14) {
            newWordAmount -= 230
        } else if (daysMissed > 14){
            newWordAmount -= 350 // Cap reduction after prolonged inactivity
        }

        // Ensure word amount does not drop below a minimum or exceed a maximum
        val minWordAmount = 500L // Minimum words per segment
        val maxWordAmount = 10000L // Maximum words per segment
        newWordAmount = newWordAmount.coerceIn(minWordAmount, maxWordAmount)
        Log.d("SegmentAdapter", "Current Word Amount: $newWordAmount")

        return newWordAmount
    }

    private fun updateWordAmountInFirebase(wordAmount: Long) {

        val pageCount = (wordAmount.toDouble() / 220).roundToInt()
        val userId = auth.currentUser?.uid ?: return
        databaseReference.child("Users").child(userId).child("pageCount").setValue(pageCount)
            .addOnSuccessListener {
                Log.d("SegmentAdapter", "Word amount updated successfully: $pageCount")
            }
            .addOnFailureListener {
                Log.e("SegmentAdapter", "Failed to update word amount", it)
            }
    }
}

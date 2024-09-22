package com.example.bookbyte.ui

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await


class StatisticsUpdater {
    private val user = FirebaseAuth.getInstance().currentUser
    private val dir = FirebaseStorage.getInstance().reference.child("${user?.uid}/statistics/")

    fun updateTotalSegmentsValue() {
        val totalSegmentsFileRef = dir.child("totalSegments.txt")

        totalSegmentsFileRef.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->
                try {
                    val totalSegments = String(bytes).trim().toIntOrNull() ?: 0
                    val updatedTotalSegments = totalSegments + 1

                    // Update the file with the new total segments count
                    totalSegmentsFileRef.putBytes(updatedTotalSegments.toString().toByteArray())
                        .addOnSuccessListener {
                            Log.d("StatisticsUpdater", "Updated Total Segments: $updatedTotalSegments")
                        }
                        .addOnFailureListener { e ->
                            Log.e("StatisticsUpdater", "Failed to update total segments", e)
                        }
                } catch (e: Exception) {
                    Log.e("StatisticsUpdater", "Error processing total segments data", e)
                }
            }
            .addOnFailureListener { e ->
                if (e is StorageException && e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    // If the file doesn't exist, creating it with the initial count of 1
                    totalSegmentsFileRef.putBytes("1".toByteArray())
                        .addOnSuccessListener {
                            Log.d("StatisticsUpdater", "Total Segments file created with initial value of 1")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("StatisticsUpdater", "Failed to create total segments file", exception)
                        }
                } else {
                    Log.e("StatisticsUpdater", "Failed to fetch total segments", e)
                }
            }
    }

    fun updateLongestStreak(currentStreak: Int) {
        val longestStreakFileRef = dir.child("longestStreak.txt")

        longestStreakFileRef.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->
                val longestStreak = String(bytes).trim().toIntOrNull() ?: 0
                if (currentStreak > longestStreak) {
                    // Current streak is greater, update the file
                    longestStreakFileRef.putBytes(currentStreak.toString().toByteArray())
                        .addOnSuccessListener {
                            Log.d("StatisticsUpdater", "Updated Longest Streak: $currentStreak")
                        }
                        .addOnFailureListener { e ->
                            Log.e("StatisticsUpdater", "Failed to update longest streak", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                if (e is StorageException && e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    // If the file doesn't exist, create it with the current streak
                    longestStreakFileRef.putBytes(currentStreak.toString().toByteArray())
                        .addOnSuccessListener {
                            Log.d("StatisticsUpdater", "Longest Streak file created with initial value of $currentStreak")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("StatisticsUpdater", "Failed to create longest streak file", exception)
                        }
                } else {
                    Log.e("StatisticsUpdater", "Failed to fetch longest streak", e)
                }
            }
    }

    suspend fun getLongestStreak(): Int {
        val longestStreakFileRef = dir.child("longestStreak.txt")
        val deferredStreak = CompletableDeferred<Int>()

        try {
            val bytes = longestStreakFileRef.getBytes(Long.MAX_VALUE).await()
            val longestStreak = String(bytes).trim().toInt()
            deferredStreak.complete(longestStreak)
        } catch (e: Exception) {
            Log.e("StatisticsUpdater", "Failed to retrieve longest streak", e)
            deferredStreak.complete(0) // Default to 0 on failure
        }

        return deferredStreak.await()
    }


    suspend fun getTotalSegments(): Int {
        val totalSegmentsFileRef = dir.child("totalSegments.txt")
        val deferredTotalSegments = CompletableDeferred<Int>()

        try {
            val bytes = totalSegmentsFileRef.getBytes(Long.MAX_VALUE).await()
            val totalSegments = String(bytes).trim().toInt()
            Log.d("StatisticsUpdater", "Retrieved Total Segments: $totalSegments")
            deferredTotalSegments.complete(totalSegments)

        } catch (e: Exception) {
            Log.e("StatisticsUpdater", "Failed to retrieve total segments", e)
            deferredTotalSegments.complete(0) // Default to 0 on failure
        }

        return deferredTotalSegments.await()
    }


    fun checkReadingPaceStatistics(timeToCompleteReadingSegment: Long) {
        val paceFileRef = dir.child("readingPace.txt")

        // Check if readingPace.txt exists
        paceFileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            // File exists, parse the contents
            val contents = String(bytes).trim()
            val times = contents.split(" ")
            if (times.size == 2) {
                var slowest = times[0].toLong()
                var fastest = times[1].toLong()

                var updateNeeded = false
                if (timeToCompleteReadingSegment < fastest) {
                    fastest = timeToCompleteReadingSegment
                    updateNeeded = true
                }
                if (timeToCompleteReadingSegment > slowest) {
                    slowest = timeToCompleteReadingSegment
                    updateNeeded = true
                }

                if (updateNeeded) {
                    paceFileRef.putBytes("$slowest $fastest".toByteArray()).addOnFailureListener {
                        Log.d("StatisticsUpdater", "Times recorded: $slowest $fastest")

                    }
                }

                addTimeToAverage(timeToCompleteReadingSegment)

            }
        }.addOnFailureListener {
            // File does not exist, create it with the current time as both the slowest and fastest
            paceFileRef.putBytes("$timeToCompleteReadingSegment $timeToCompleteReadingSegment".toByteArray())
            addTimeToAverage(timeToCompleteReadingSegment)
        }
    }

    private fun addTimeToAverage(newTime: Long) {
        val averageFileRef = dir.child("readingAverage.txt")
        averageFileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val existingTimes = String(bytes).trim().split(" ").map { it.toLong() }.toMutableList()
            existingTimes.add(newTime)
            if (existingTimes.size > 20) {
                existingTimes.removeAt(0)  // Keep only the latest 20 times
            }
            averageFileRef.putBytes(existingTimes.joinToString(" ").toByteArray()).addOnSuccessListener {
                // Optionally trigger the average calculation here if needed immediately after update
            }.addOnFailureListener {
                // Handle failure to update average file
            }
        }.addOnFailureListener {
            // If readingAverage.txt does not exist, create it
            averageFileRef.putBytes(newTime.toString().toByteArray())
        }
    }

    suspend fun calculateAndDisplayAverage(): Long {
        val averageFileRef = dir.child("readingAverage.txt")
        val deferredAverage = CompletableDeferred<Long>()

        try {
            val bytes = averageFileRef.getBytes(Long.MAX_VALUE).await()
            val times = String(bytes).trim().split(" ").map { it.toLong() }
            val average = if (times.isNotEmpty()) times.sum() / times.size else 0L

            Log.d("StatisticsUpdater", "Current Average Reading Time: $average")
            deferredAverage.complete(average) // Successfully calculated average

        } catch (e: Exception) {
            Log.e("StatisticsUpdater", "Failed to fetch average reading times", e)
            deferredAverage.complete(0L) // Default to 0 on failure
        }

        return deferredAverage.await() // This will return the average when it's ready
    }

}

package com.example.bookbyte

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import java.util.Calendar

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Log.i("DEBUG", "consoleMessage: App is Initialized")

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    // Permission is already granted; you can schedule an exact alarm
                    scheduleDailyReminder(this)
                }
            }
        }
        // The function is used to create a notification channel and schedule a daily reminder
        createNotificationChannel(this)

    }

    companion object {
        fun scheduleDailyReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager == null) {
                Log.e("scheduleDailyReminder", "Failed to get ALARM_SERVICE")
                return
            }

            val intent = Intent(context, ReminderBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                10,  // request code, ensure this is unique if using multiple alarms
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 20)  // 1:00 PM
            }

//            alarmManager.setInexactRepeating(
//                AlarmManager.RTC_WAKEUP,
//                calendar.timeInMillis,
//                AlarmManager.INTERVAL_DAY,
//                pendingIntent
//            )
//            // Log the exact time the alarm is set for
//            Log.d("scheduleDailyReminder", "Scheduling alarm at: ${calendar.time}")
//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
//            Log.d("scheduleDailyReminder", "Alarm is set to repeat every day")
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "notifyChannel"
                val channelName = "Streak Notification"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = "Channel for daily reminder notifications"
                }
                val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d("createNotificationChannel", "Notification channel created: $channelId")
            } else {
                Log.d("createNotificationChannel", "Notification channel not created. OS version < Oreo")
            }
        }

        private fun getUserId(context: Context): String {
            val user = FirebaseAuth.getInstance().currentUser
            return user?.uid ?: "defaultUser" // Return "defaultUser" if no user is logged in
        }

        private fun getSharedPreferences(context: Context): SharedPreferences {
            val userId = getUserId(context)
            return context.getSharedPreferences("${userId}_AppPrefs", Context.MODE_PRIVATE)
        }
        fun saveReadingStreak(streak: Int, context: Context) {
            val sharedPreferences = getSharedPreferences(context)
            if (streak != 0) {
                resetMissedDays(context)
            }

            val editor = sharedPreferences.edit()
            editor.putInt("ReadingStreak", streak)
            editor.putLong("LastUpdate", System.currentTimeMillis()) // Save the current time as last update
            editor.apply()
        }

        fun getLastUpdate(context: Context): Long {
            val sharedPreferences = getSharedPreferences(context)
            return sharedPreferences.getLong("LastUpdate", 0L) // Return the potentially updated streak
        }

        fun getReadingStreak(context: Context): Int {
            val sharedPreferences = getSharedPreferences(context)
            return sharedPreferences.getInt("ReadingStreak", 0) // Return the potentially updated streak
        }

        fun displayReadingStreak(context: Context): String {
            val streak = getReadingStreak(context)
            return if (streak < 10) "0$streak" else streak.toString()
        }

        fun getMissedDays(context: Context): Int {
            val sharedPreferences = getSharedPreferences(context)
            return sharedPreferences.getInt("MissedDays", 0) // 0 is the default value if not found
        }

        private fun resetMissedDays(context: Context) {
            val sharedPreferences = getSharedPreferences(context)
            val editor = sharedPreferences.edit()
            editor.putInt("MissedDays", 0)
            editor.apply()
        }

        fun updateStreakIfNeeded(context: Context) {
            val sharedPreferences = getSharedPreferences(context)
            val lastUpdate = sharedPreferences.getLong("LastUpdate", 0)
            if (lastUpdate == 0L) return // No update needed if never set

            val currentTime = System.currentTimeMillis()
            val hoursSinceLastUpdate = (currentTime - lastUpdate) / (1000 * 60 * 60)
            Log.d("App","Outside Hours: $hoursSinceLastUpdate")

            if (hoursSinceLastUpdate >= 24) {
                Log.d("App","In Hours: $hoursSinceLastUpdate")

                val daysMissed = (hoursSinceLastUpdate / 24).toInt()
                saveReadingStreak(0, context)
                sharedPreferences.edit().apply {
                    putInt("MissedDays", sharedPreferences.getInt("MissedDays", 0) + daysMissed)
                    apply()
                }
            }
        }
    }

}
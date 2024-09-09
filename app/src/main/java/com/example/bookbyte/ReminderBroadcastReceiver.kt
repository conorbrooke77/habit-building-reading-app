package com.example.bookbyte

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bookbyte.usermanagement.LoginActivity

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            // Re-schedule the alarm when the device is rebooted
            App.scheduleDailyReminder(context)
        } else {
            // Regular alarm time, show the notification
            showNotification(context)
        }
    }
    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val activityIntent = Intent(context, LoginActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            context,
            100,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE  // Add the FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "notifyChannel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reading Streak Reminder")
            .setContentText("Don't forget to complete your daily reading segment!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        Log.d("NotificationTest", "Notification builder set up")

        // Notify using the NotificationManager
        notificationManager.notify(200, builder.build())
        Log.d("NotificationTest", "Notification should now be visible")
    }


}
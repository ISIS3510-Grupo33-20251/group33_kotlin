package com.example.universe.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.universe.MainActivity
import com.example.universe.R

object NotificationHelper {

    private const val REMINDER_CHANNEL_ID = "reminder_channel"
    private const val REMINDER_CHANNEL_NAME = "Reminders"
    private const val REMINDER_CHANNEL_DESCRIPTION = "Notifications for reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = REMINDER_CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(
        context: Context,
        title: String,
        message: String,
        reminderId: String,
        entityType: String? = null,
        entityId: String? = null
    ) {
        createNotificationChannel(context)

        // Create intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminder_id", reminderId)
            putExtra("entity_type", entityType)
            putExtra("entity_id", entityId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            android.util.Log.e("NotificationHelper", "Notification permission not granted", e)
        }
    }
}
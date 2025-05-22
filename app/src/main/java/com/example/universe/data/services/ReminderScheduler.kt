package com.example.universe.data.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.universe.domain.models.Reminder
import com.example.universe.data.receivers.ReminderBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNotification(reminder: Reminder) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("title", reminder.title)
            putExtra("message", reminder.message)
            putExtra("entity_type", reminder.entityType.name)
            putExtra("entity_id", reminder.entityId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = reminder.remindAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val currentTime = System.currentTimeMillis()

        // Only schedule if the reminder is in the future
        if (triggerTime > currentTime) {
            try {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                    else -> {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                }
                Log.d("ReminderScheduler", "Scheduled reminder ${reminder.id} for ${reminder.remindAt}")
            } catch (e: SecurityException) {
                Log.e("ReminderScheduler", "Permission denied for exact alarms", e)
                // Fallback to inexact alarm
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            Log.w("ReminderScheduler", "Reminder ${reminder.id} is in the past, not scheduling")
        }
    }

    fun cancelNotification(reminderId: String) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("ReminderScheduler", "Cancelled reminder $reminderId")
    }

    fun rescheduleAllReminders(reminders: List<Reminder>) {
        reminders.forEach { reminder ->
            if (reminder.status == com.example.universe.domain.models.ReminderStatus.PENDING) {
                scheduleNotification(reminder)
            }
        }
    }
}
package com.example.universe.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.universe.data.db.AppDatabase
import com.example.universe.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: ""
        val entityType = intent.getStringExtra("entity_type")
        val entityId = intent.getStringExtra("entity_id")

        Log.d("ReminderReceiver", "Reminder fired: $reminderId - $title")

        // Show notification
        NotificationHelper.showReminderNotification(
            context = context,
            title = title,
            message = message,
            reminderId = reminderId,
            entityType = entityType,
            entityId = entityId
        )
    }
}
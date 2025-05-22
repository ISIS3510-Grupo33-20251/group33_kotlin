package com.example.universe.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.universe.data.repositories.ReminderRepositoryImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderRepository: ReminderRepositoryImpl

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d("BootReceiver", "Device booted, rescheduling reminders")

                // Reschedule all pending reminders
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        reminderRepository.rescheduleAllPendingReminders()
                        Log.d("BootReceiver", "Successfully rescheduled reminders")
                    } catch (e: Exception) {
                        Log.e("BootReceiver", "Failed to reschedule reminders", e)
                    }
                }
            }
        }
    }
}
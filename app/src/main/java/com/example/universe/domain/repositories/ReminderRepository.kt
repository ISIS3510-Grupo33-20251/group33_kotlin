package com.example.universe.domain.repositories;

import com.example.universe.domain.models.Reminder
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface ReminderRepository {
    fun getRemindersStream(userId: String): Flow<List<Reminder>>
    suspend fun getReminders(userId: String, localOnly: Boolean = false): Result<List<Reminder>>
    suspend fun createReminder(
            title: String,
            message: String,
            remindAt: LocalDateTime,
            entityType: String = "custom",
            entityId: String? = null
    ): Result<Reminder>
    suspend fun updateReminder(reminder: Reminder): Result<Unit>
    suspend fun deleteReminder(reminderId: String): Result<Unit>
    suspend fun syncReminders(): Result<Unit>
    suspend fun markReminderAsFired(reminderId: String): Result<Unit>
    suspend fun rescheduleAllPendingReminders(): Result<Unit>
    suspend fun clearCache()
}
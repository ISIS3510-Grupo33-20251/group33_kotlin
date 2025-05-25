package com.example.universe.data.repositories

import android.util.Log
import com.example.universe.data.api.ReminderApiService
import com.example.universe.data.db.dao.ReminderDao
import com.example.universe.data.mappers.ReminderMapper
import com.example.universe.data.services.ReminderScheduler
import com.example.universe.domain.models.Reminder
import com.example.universe.domain.models.ReminderEntityType
import com.example.universe.domain.models.ReminderStatus
import com.example.universe.domain.repositories.AuthRepository
import com.example.universe.domain.repositories.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderApiService: ReminderApiService,
    private val reminderDao: ReminderDao,
    private val reminderScheduler: ReminderScheduler,
    private val authRepository: AuthRepository
) : ReminderRepository {

    private val TAG = "ReminderRepository"

    override fun getRemindersStream(userId: String): Flow<List<Reminder>> {
        return reminderDao.getRemindersByUserFlow(userId)
            .map { entities -> entities.map { ReminderMapper.fromEntity(it) } }
    }

    override suspend fun getReminders(userId: String, localOnly: Boolean): Result<List<Reminder>> {
        return try {
            val localEntities = reminderDao.getRemindersByUser(userId)
            val localReminders = localEntities.map { ReminderMapper.fromEntity(it) }

            // If no local data and not local-only, try network
            if (!localOnly) {
                val networkResult = syncFromNetwork(userId)
                if (networkResult.isSuccess) {
                    val updatedEntities = reminderDao.getRemindersByUser(userId)
                    val updatedReminders = updatedEntities.map { ReminderMapper.fromEntity(it) }
                    val userReminders = updatedReminders.filter { reminder ->
                        reminder.userId.equals(userId)
                    }
                    return Result.success(userReminders)
                }
            }

            val userReminders = localReminders.filter { reminder ->
                reminder.userId.equals(userId)
            }

            if (localReminders.isNotEmpty()) {
                return Result.success(userReminders)
            }

            // Return empty list if all fails
            Result.success(emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reminders", e)
            Result.failure(e)
        }
    }

    override suspend fun createReminder(
        title: String,
        message: String,
        remindAt: LocalDateTime,
        entityType: String,
        entityId: String?
    ): Result<Reminder> {
        return try {
            val currentUser = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not found"))

            val reminder = Reminder(
                id = UUID.randomUUID().toString(),
                userId = currentUser.id,
                entityType = when (entityType.lowercase()) {
                    "task" -> ReminderEntityType.TASK
                    "meeting" -> ReminderEntityType.MEETING
                    else -> ReminderEntityType.CUSTOM
                },
                entityId = title,
                title = title,
                message = message,
                remindAt = remindAt,
                status = ReminderStatus.PENDING,
                createdAt = LocalDateTime.now(),
                isLocallyScheduled = false
            )

            // 1. Store locally first
            val entity = ReminderMapper.toEntity(reminder, isSynced = false)
            reminderDao.insertReminder(entity)
            Log.d(TAG, "Stored reminder locally: ${reminder.id}")

            // 2. Schedule local notification
            reminderScheduler.scheduleNotification(reminder)
            reminderDao.updateScheduledStatus(reminder.id, true)
            Log.d(TAG, "Scheduled notification for reminder: ${reminder.id}")

            // 3. Try to sync to backend (don't fail if this fails)
            try {
                syncReminderToNetwork(reminder)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync reminder to network, will retry later", e)
            }

            Result.success(reminder.copy(isLocallyScheduled = true))
        } catch (e: Exception) {
            Log.e(TAG, "Error creating reminder", e)
            Result.failure(e)
        }
    }

    override suspend fun updateReminder(reminder: Reminder): Result<Unit> {
        return try {
            // Update locally
            val entity = ReminderMapper.toEntity(reminder, isSynced = false)
            reminderDao.updateReminder(entity)

            // Reschedule notification if status is still pending
            if (reminder.status == ReminderStatus.PENDING) {
                reminderScheduler.scheduleNotification(reminder)
                reminderDao.updateScheduledStatus(reminder.id, true)
            } else {
                reminderScheduler.cancelNotification(reminder.id)
                reminderDao.updateScheduledStatus(reminder.id, false)
            }

            // Try to sync to backend
            try {
                syncReminderToNetwork(reminder)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync updated reminder to network", e)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reminder", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            // Cancel notification
            reminderScheduler.cancelNotification(reminderId)

            // Get the backend ID for deletion
            val backendId = reminderDao.getBackendId(reminderId)

            if (backendId != null) {
                // Try to delete from backend using backend ID
                try {
                    val token = authRepository.getAuthToken()
                    if (token != null) {
                        reminderApiService.deleteReminder("Bearer $token", backendId)
                        reminderDao.deleteReminder(reminderId) // Delete local record
                        Log.d(TAG, "Deleted reminder: local=$reminderId, backend=$backendId")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete from network, marking for deletion", e)
                    reminderDao.markAsDeleted(reminderId)
                }
            } else {
                // No backend ID, just delete locally
                reminderDao.deleteReminder(reminderId)
                Log.d(TAG, "Deleted local-only reminder: $reminderId")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting reminder", e)
            Result.failure(e)
        }
    }

    override suspend fun markReminderAsFired(reminderId: String): Result<Unit> {
        return try {
            reminderDao.updateReminderStatus(reminderId, "fired")

            // Try to sync status to backend
            try {
                val reminder = reminderDao.getReminderById(reminderId)
                if (reminder != null) {
                    val domainReminder = ReminderMapper.fromEntity(reminder)
                    syncReminderToNetwork(domainReminder.copy(status = ReminderStatus.FIRED))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync fired status to network", e)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncReminders(): Result<Unit> {
        return try {
            val currentUser = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not found"))

            syncFromNetwork(currentUser.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing reminders", e)
            Result.failure(e)
        }
    }

    override suspend fun rescheduleAllPendingReminders(): Result<Unit> {
        return try {
            val currentUser = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not found"))

            val entities = reminderDao.getRemindersByUser(currentUser.id)
            val pendingReminders = entities
                .map { ReminderMapper.fromEntity(it) }
                .filter { it.status == ReminderStatus.PENDING }

            pendingReminders.forEach { reminder ->
                reminderScheduler.scheduleNotification(reminder)
                reminderDao.updateScheduledStatus(reminder.id, true)
            }

            Log.d(TAG, "Rescheduled ${pendingReminders.size} pending reminders")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling reminders", e)
            Result.failure(e)
        }
    }

    override suspend fun clearCache() {
        // This would be called on logout
        val currentUser = authRepository.getCurrentUser().first()
        if (currentUser != null) {
            val reminders = reminderDao.getRemindersByUser(currentUser.id)
            reminders.forEach { reminder ->
                reminderScheduler.cancelNotification(reminder.id)
            }
        }
    }

    private suspend fun syncFromNetwork(userId: String): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken() ?: throw Exception("Not authenticated")

            val response = reminderApiService.getUserReminders("Bearer $token", userId)
            if (response.isSuccessful) {
                val remoteDtos = response.body() ?: emptyList()
                val remoteReminders = remoteDtos.map { ReminderMapper.fromDto(it) }

                // Store remote reminders locally
                val entities = remoteReminders.map { ReminderMapper.toEntity(it, isSynced = true) }
                reminderDao.insertReminders(entities)

                // Schedule notifications for new pending reminders
                remoteReminders
                    .filter { it.status == ReminderStatus.PENDING }
                    .forEach { reminder ->
                        reminderScheduler.scheduleNotification(reminder)
                        reminderDao.updateScheduledStatus(reminder.id, true)
                    }

                Log.d(TAG, "Synced ${remoteReminders.size} reminders from network")
                Result.success(Unit)
            } else {
                throw Exception("Network sync failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network sync failed", e)
            Result.failure(e)
        }
    }

    private suspend fun syncReminderToNetwork(reminder: Reminder) {
        val token = authRepository.getAuthToken() ?: return
        val dto = ReminderMapper.toDto(reminder)

        val response = reminderApiService.createReminder("Bearer $token", dto)
        if (response.isSuccessful) {
            val backendReminder = response.body()
            if (backendReminder?.id != null) {
                // Store the backend ID mapping
                reminderDao.updateBackendId(reminder.id, backendReminder.id)
                Log.d(TAG, "Mapped local ID ${reminder.id} to backend ID ${backendReminder.id}")
            }
        }
    }
}
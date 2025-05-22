package com.example.universe.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.universe.data.db.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE userId = :userId AND deleted = 0 ORDER BY remindAt ASC")
    suspend fun getRemindersByUser(userId: String): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND deleted = 0 ORDER BY remindAt ASC")
    fun getRemindersByUserFlow(userId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE status = 'pending' AND remindAt <= :currentTime AND deleted = 0")
    suspend fun getPendingReminders(currentTime: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE isSynced = 0 AND deleted = 0")
    suspend fun getUnsyncedReminders(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET status = :status WHERE id = :reminderId")
    suspend fun updateReminderStatus(reminderId: String, status: String)

    @Query("UPDATE reminders SET isLocallyScheduled = :scheduled WHERE id = :reminderId")
    suspend fun updateScheduledStatus(reminderId: String, scheduled: Boolean)

    @Query("UPDATE reminders SET isSynced = 1 WHERE id = :reminderId")
    suspend fun markAsSynced(reminderId: String)

    @Query("UPDATE reminders SET deleted = 1, isSynced = 0 WHERE id = :reminderId")
    suspend fun markAsDeleted(reminderId: String)

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminder(reminderId: String)

    @Query("DELETE FROM reminders WHERE deleted = 1 AND isSynced = 1")
    suspend fun cleanupDeletedSyncedReminders()
}
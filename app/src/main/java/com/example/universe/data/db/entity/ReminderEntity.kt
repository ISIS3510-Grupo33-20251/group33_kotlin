package com.example.universe.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
        @PrimaryKey val id: String,
        val userId: String,
        val entityType: String,
        val entityId: String?,
        val title: String,
        val message: String,
        val remindAt: Long, // Unix timestamp
        val status: String,
        val createdAt: Long,
        val isLocallyScheduled: Boolean = false,
        val isSynced: Boolean = false, // For eventual connectivity
        val deleted: Boolean = false
)
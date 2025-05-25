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
        val remindAt: Long,
        val status: String,
        val createdAt: Long,
        val isLocallyScheduled: Boolean = false,
        val isSynced: Boolean = false,
        val deleted: Boolean = false,
        val backendId: String? = null  // Add this field to track backend ID
)


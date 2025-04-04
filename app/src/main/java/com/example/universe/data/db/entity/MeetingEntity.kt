package com.example.universe.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meeting")
data class MeetingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val location: String?,
    val meetingLink: String?,
    val hostId: String,
    val participants: String, // JSON string of participant IDs
    val dateKey: String // Format: YYYY-MM-DD
)
package com.example.universe.domain.models

import java.time.LocalDateTime

enum class ReminderStatus {
    PENDING, FIRED, CANCELLED
}

enum class ReminderEntityType {
    TASK, MEETING, CUSTOM
}

data class Reminder(
    val id: String,
    val userId: String,
    val entityType: ReminderEntityType,
    val entityId: String?,
    val title: String,
    val message: String,
    val remindAt: LocalDateTime,
    val status: ReminderStatus,
    val createdAt: LocalDateTime,
    val isLocallyScheduled: Boolean = false
)
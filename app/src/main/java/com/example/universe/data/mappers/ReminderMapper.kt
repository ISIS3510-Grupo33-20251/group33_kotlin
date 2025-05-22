package com.example.universe.data.mappers

import com.example.universe.data.db.entity.ReminderEntity
import com.example.universe.data.models.ReminderDto
import com.example.universe.domain.models.Reminder
import com.example.universe.domain.models.ReminderEntityType
import com.example.universe.domain.models.ReminderStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderMapper {

    fun fromEntity(entity: ReminderEntity): Reminder = Reminder(
        id = entity.id,
        userId = entity.userId,
        entityType = when (entity.entityType.lowercase()) {
            "task" -> ReminderEntityType.TASK
            "meeting" -> ReminderEntityType.MEETING
            else -> ReminderEntityType.CUSTOM
        },
        entityId = entity.entityId,
        title = entity.title,
        message = entity.message,
        remindAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(entity.remindAt),
            ZoneId.systemDefault()
        ),
        status = when (entity.status.lowercase()) {
            "fired" -> ReminderStatus.FIRED
            "cancelled" -> ReminderStatus.CANCELLED
            else -> ReminderStatus.PENDING
        },
        createdAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(entity.createdAt),
            ZoneId.systemDefault()
        ),
        isLocallyScheduled = entity.isLocallyScheduled
    )

    fun toEntity(reminder: Reminder, isSynced: Boolean = false): ReminderEntity = ReminderEntity(
        id = reminder.id,
        userId = reminder.userId,
        entityType = reminder.entityType.name.lowercase(),
        entityId = reminder.entityId,
        title = reminder.title,
        message = reminder.message,
        remindAt = reminder.remindAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        status = reminder.status.name.lowercase(),
        createdAt = reminder.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        isLocallyScheduled = reminder.isLocallyScheduled,
        isSynced = isSynced,
        deleted = false
    )

    fun fromDto(dto: ReminderDto): Reminder = Reminder(
        id = dto.id ?: "",
        userId = dto.userId,
        entityType = when (dto.entityType.lowercase()) {
            "task" -> ReminderEntityType.TASK
            "meeting" -> ReminderEntityType.MEETING
            else -> ReminderEntityType.CUSTOM
        },
        entityId = dto.entityId,
        title = dto.title ?: "",
        message = dto.message ?: "",
        remindAt = LocalDateTime.parse(dto.remindAt),
        status = when (dto.status.lowercase()) {
            "fired" -> ReminderStatus.FIRED
            "cancelled" -> ReminderStatus.CANCELLED
            else -> ReminderStatus.PENDING
        },
        createdAt = LocalDateTime.now(),
        isLocallyScheduled = false
    )

    fun toDto(reminder: Reminder): ReminderDto = ReminderDto(
        id = reminder.id,
        userId = reminder.userId,
        entityType = reminder.entityType.name.lowercase(),
        entityId = reminder.entityId,
        title = reminder.title,
        message = reminder.message,
        remindAt = reminder.remindAt.toString(),
        status = reminder.status.name.lowercase()
    )
}
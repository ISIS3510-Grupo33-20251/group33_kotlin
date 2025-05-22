package com.example.universe.domain.usecases

import com.example.universe.domain.models.Reminder
import com.example.universe.domain.repositories.ReminderRepository
import java.time.LocalDateTime
import javax.inject.Inject

class CreateReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(
        title: String,
        message: String,
        remindAt: LocalDateTime,
        entityType: String = "custom",
        entityId: String? = null
    ): Result<Reminder> {
        return reminderRepository.createReminder(title, message, remindAt, entityType, entityId)
    }
}
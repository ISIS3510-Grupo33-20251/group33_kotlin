package com.example.universe.domain.usecases

import com.example.universe.domain.repositories.ReminderRepository
import javax.inject.Inject

class DeleteReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: String): Result<Unit> {
        return reminderRepository.deleteReminder(reminderId)
    }
}
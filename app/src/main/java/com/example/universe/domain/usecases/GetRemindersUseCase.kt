package com.example.universe.domain.usecases

import com.example.universe.domain.models.Reminder
import com.example.universe.domain.repositories.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    operator fun invoke(userId: String): Flow<List<Reminder>> {
        return reminderRepository.getRemindersStream(userId)
    }

    suspend fun getOnce(userId: String, localOnly: Boolean = false): Result<List<Reminder>> {
        return reminderRepository.getReminders(userId, localOnly)
    }
}
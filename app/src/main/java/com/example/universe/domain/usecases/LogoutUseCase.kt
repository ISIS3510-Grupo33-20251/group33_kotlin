package com.example.universe.domain.usecases

import com.example.universe.domain.repositories.AuthRepository
import com.example.universe.domain.repositories.FriendRepository
import com.example.universe.domain.repositories.ScheduleRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository,
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
        friendRepository.clearCache()
        scheduleRepository.clearCache()
    }
}
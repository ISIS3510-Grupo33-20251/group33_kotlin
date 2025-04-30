package com.example.universe.domain.usecases

import com.example.universe.domain.repositories.AuthRepository
import com.example.universe.domain.repositories.FriendLocationRepository
import com.example.universe.domain.repositories.FriendRepository
import com.example.universe.domain.repositories.LocationRepository
import com.example.universe.domain.repositories.ScheduleRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository,
    private val scheduleRepository: ScheduleRepository,
    private val friendLocationRepository: FriendLocationRepository,
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke() {
        friendRepository.clearCache()
        scheduleRepository.clearCache()
        friendLocationRepository.clearCache()
        locationRepository.clearCache()

        authRepository.logout()

    }
}
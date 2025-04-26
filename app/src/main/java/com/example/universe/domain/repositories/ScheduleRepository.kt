package com.example.universe.domain.repositories

import com.example.universe.domain.models.DaySchedule
import com.example.universe.domain.models.Meeting
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ScheduleRepository {

    suspend fun getScheduleForDay(date: LocalDate, forceNetworkRefresh: Boolean = false, localOnly: Boolean = false): Result<DaySchedule>

    suspend fun getScheduleForRange(startDate: LocalDate, endDate: LocalDate): Result<List<DaySchedule>>

    /**
     * Gets a reactive stream of schedules that updates when the database changes
     */
    fun getScheduleStream(date: LocalDate): Flow<DaySchedule>

    suspend fun removeMeeting(meetingId: String): Result<Unit>

    suspend fun refreshFromNetwork()
    suspend fun clearCache()
}
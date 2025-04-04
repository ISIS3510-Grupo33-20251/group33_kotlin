package com.example.universe.domain.repositories

import com.example.universe.domain.models.Meeting
import kotlinx.coroutines.flow.Flow

interface MeetingRepository {
    suspend fun createMeeting(
        title: String,
        day: String,
        startTime: String,
        endTime: String,
        description: String = "Created from mobile app",
        frequency: String = "Once"
    ): Result<Meeting>

    suspend fun getMeetings(): Result<List<Meeting>>

    suspend fun getMeetingById(meetingId: String): Result<Meeting>

    suspend fun deleteMeeting(meetingId: String): Result<Unit>

    fun getMeetingsStream(): Flow<List<Meeting>>
}
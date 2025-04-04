package com.example.universe.data.repositories

import android.util.Log
import com.example.universe.data.api.MeetingApiService
import com.example.universe.data.models.MeetingRequest
import com.example.universe.data.models.MeetingResponse
import com.example.universe.domain.models.Meeting
import com.example.universe.domain.repositories.AuthRepository
import com.example.universe.domain.repositories.MeetingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeetingRepositoryImpl @Inject constructor(
    private val meetingApiService: MeetingApiService,
    private val authRepository: AuthRepository
) : MeetingRepository {

    private val _meetingsFlow = MutableStateFlow<List<Meeting>>(emptyList())

    override suspend fun createMeeting(
        title: String,
        day: String,
        startTime: String,
        endTime: String,
        description: String,
        frequency: String
    ): Result<Meeting> {
        return try {
            // user ID
            val currentUser = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not found"))
            val userId = currentUser.id

            // auth token
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("Not authenticated"))

            // Convert day to a date (for simplicity, using the next occurrence of that day)
            val meetingDate = getNextDayDate(day)

            val startDateTime = "${meetingDate}T${parseTime(startTime)}"
            val endDateTime = "${meetingDate}T${parseTime(endTime)}"

            when (frequency) {
                "Once" -> {
                    // Create a single meeting
                    createSingleMeeting(token, title, description, startDateTime, endDateTime, userId)
                }
                "Daily" -> {
                    // Create daily meetings for the next 30 days
                    createRecurringMeetings(token, title, description, startDateTime, endDateTime, userId, 1, 30)
                }
                "Weekly" -> {
                    // Create weekly meetings for the next 12 weeks
                    createRecurringMeetings(token, title, description, startDateTime, endDateTime, userId, 7, 12)
                }
                "Monthly" -> {
                    // Create monthly meetings for the next 6 months
                    createRecurringMeetings(token, title, description, startDateTime, endDateTime, userId, 30, 6)
                }
                else -> {
                    createSingleMeeting(token, title, description, startDateTime, endDateTime, userId)
                }
            }

            Result.success(Meeting(
                id = "temp-id",
                title = title,
                description = description,
                startTime = startDateTime,
                endTime = endDateTime,
                location = null,
                meetingLink = null,
                hostId = userId,
                participants = listOf(userId)
            ))
        } catch (e: Exception) {
            Log.e("MeetingRepo", "Error creating meeting", e)
            Result.failure(e)
        }
    }

    override suspend fun getMeetings(): Result<List<Meeting>> {
        return try {
            // auth token
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("Not authenticated"))

            // Make API call
            val response = meetingApiService.getMeetings("Bearer $token")

            // Map response to domain models
            val meetings = response.map { mapResponseToDomain(it) }

            // Update the cached meetings
            _meetingsFlow.update { meetings }

            Result.success(meetings)
        } catch (e: Exception) {
            Log.e("MeetingRepo", "Error fetching meetings", e)
            Result.failure(e)
        }
    }

    override suspend fun getMeetingById(meetingId: String): Result<Meeting> {
        return try {
            // auth token
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("Not authenticated"))

            // Make API call
            val response = meetingApiService.getMeetingById("Bearer $token", meetingId)

            // Map response to domain model
            Result.success(mapResponseToDomain(response))
        } catch (e: Exception) {
            Log.e("MeetingRepo", "Error fetching meeting $meetingId", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteMeeting(meetingId: String): Result<Unit> {
        return try {
            // auth token
            val token = authRepository.getAuthToken()
                ?: return Result.failure(Exception("Not authenticated"))

            // Make API call
            meetingApiService.deleteMeeting("Bearer $token", meetingId)

            // Update the cached meetings
            _meetingsFlow.update { currentList ->
                currentList.filterNot { it.id == meetingId }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MeetingRepo", "Error deleting meeting $meetingId", e)
            Result.failure(e)
        }
    }

    override fun getMeetingsStream(): Flow<List<Meeting>> {
        return _meetingsFlow.asStateFlow()
    }

    // map API response to domain model
    private fun mapResponseToDomain(response: MeetingResponse): Meeting {
        return Meeting(
            id = response.id,
            title = response.title,
            description = response.description,
            startTime = response.startTime,
            endTime = response.endTime,
            location = response.location,
            meetingLink = response.meetingLink,
            hostId = response.hostId,
            participants = response.participants
        )
    }

    private suspend fun createSingleMeeting(
        token: String,
        title: String,
        description: String,
        startDateTime: String,
        endDateTime: String,
        userId: String
    ): Meeting {
        val meeting = MeetingRequest(
            title = title,
            description = description,
            startTime = startDateTime,
            endTime = endDateTime,
            hostId = userId,
            participants = listOf(userId)
        )

        // Make API call
        val response = meetingApiService.createMeeting("Bearer $token", meeting)

        // Map response to domain model
        val domainMeeting = mapResponseToDomain(response)

        // Update the cached meetings
        _meetingsFlow.update { currentList ->
            currentList + domainMeeting
        }

        return domainMeeting
    }

    private suspend fun createRecurringMeetings(
        token: String,
        title: String,
        description: String,
        startDateTime: String,
        endDateTime: String,
        userId: String,
        dayInterval: Int,
        occurrences: Int
    ): List<Meeting> {
        val createdMeetings = mutableListOf<Meeting>()

        // Parse the original date
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val originalStartDate = LocalDateTime.parse(startDateTime, formatter)
        val originalEndDate = LocalDateTime.parse(endDateTime, formatter)

        for (i in 0 until occurrences) {
            // Calculate the new dates
            val newStartDate = originalStartDate.plusDays((i * dayInterval).toLong())
            val newEndDate = originalEndDate.plusDays((i * dayInterval).toLong())

            val newStartDateStr = newStartDate.format(formatter)
            val newEndDateStr = newEndDate.format(formatter)

            val meeting = MeetingRequest(
                title = title,
                description = description,
                startTime = newStartDateStr,
                endTime = newEndDateStr,
                hostId = userId,
                participants = listOf(userId)
            )

            try {
                // Make API call
                val response = meetingApiService.createMeeting("Bearer $token", meeting)

                // Map response to domain model
                val domainMeeting = mapResponseToDomain(response)

                createdMeetings.add(domainMeeting)
            } catch (e: Exception) {
                Log.e("MeetingRepo", "Error creating recurring meeting: ${e.message}")
                // Continue with the next meeting even if this one fails
            }
        }

        // Update the cached meetings
        _meetingsFlow.update { currentList ->
            currentList + createdMeetings
        }

        return createdMeetings
    }

    private fun parseTime(timeStr: String): String {
        // Simple parsing for HH:MM format
        return try {
            val pattern = DateTimeFormatter.ofPattern("HH:mm")
            val time = LocalTime.parse(timeStr, pattern)
            // Format as HH:MM:SS
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            time.format(formatter)
        } catch (e: Exception) {
            // Default to current time if parsing fails
            val now = LocalTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            now.format(formatter)
        }
    }

    // next date for a given day of the week
    private fun getNextDayDate(dayName: String): String {
        val today = LocalDate.now()
        val currentDayOfWeek = today.dayOfWeek.value

        val dayOfWeek = when (dayName.lowercase()) {
            "monday" -> 1
            "tuesday" -> 2
            "wednesday" -> 3
            "thursday" -> 4
            "friday" -> 5
            "saturday" -> 6
            "sunday" -> 7
            else -> currentDayOfWeek
        }

        // Calculate days to add
        val daysToAdd = if (dayOfWeek >= currentDayOfWeek) {
            dayOfWeek - currentDayOfWeek
        } else {
            7 - (currentDayOfWeek - dayOfWeek)
        }

        // Get the date for the next occurrence of the day
        val meetingDate = today.plusDays(daysToAdd.toLong())
        return meetingDate.toString()
    }
}
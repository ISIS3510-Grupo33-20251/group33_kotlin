package com.example.universe.data.repositories

import android.util.Log
import com.example.universe.data.api.MeetingApiService
import com.example.universe.data.db.dao.MeetingDao
import com.example.universe.data.db.entity.MeetingEntity
import com.example.universe.data.models.MeetingResponse
import com.example.universe.domain.models.DaySchedule
import com.example.universe.domain.models.Meeting
import com.example.universe.domain.repositories.AuthRepository
import com.example.universe.domain.repositories.ScheduleRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import com.example.universe.di.IoDispatcher

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val meetingApiService: MeetingApiService,
    private val authRepository: AuthRepository,
    private val meetingDao: MeetingDao,
    private val gson: Gson,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ScheduleRepository {

    private val coroutineScope = CoroutineScope(ioDispatcher)

    private val TAG = "ScheduleRepository"

    // Cache of schedules
    private val cachedSchedules = MutableStateFlow<Map<LocalDate, DaySchedule>>(emptyMap())

    override suspend fun getScheduleForDay(date: LocalDate, forceNetworkRefresh: Boolean, localOnly: Boolean): Result<DaySchedule> {
        // First check if we have this date in our cache
        try {
            val dateKey = date.toString()
            Log.d(TAG, "Fetching schedule for day $dateKey from local database")
            val localMeetings = meetingDao.getMeetingsForDate(dateKey)

            if (localMeetings.isNotEmpty()) {
                Log.d(TAG, "Found ${localMeetings.size} meetings in Room for date $dateKey")
                val meetings = localMeetings.map { entity ->
                    Meeting(
                        id = entity.id,
                        title = entity.title,
                        description = entity.description,
                        startTime = entity.startTime,
                        endTime = entity.endTime,
                        location = entity.location,
                        meetingLink = entity.meetingLink,
                        hostId = entity.hostId,
                        participants = entity.participants.let {
                            try { gson.fromJson(it, Array<String>::class.java).toList() }
                            catch (e: Exception) { emptyList() }
                        }
                    )
                }

                // Update the cache
                val daySchedule = DaySchedule(date, meetings)
                cachedSchedules.value = cachedSchedules.value + (date to daySchedule)

                if (forceNetworkRefresh && !localOnly) {
                    coroutineScope.launch {
                        try { refreshFromNetwork() }
                        catch (e: Exception) { Log.w(TAG, "Background refresh failed", e) }
                    }
                }

                return Result.success(daySchedule)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching schedule from local database", e)
        }

        // If we're in local mode, return empty schedule
        if (localOnly) {
            Log.d(TAG, "Local-only mode, returning empty schedule for $date")
            return Result.success(DaySchedule(date))
        }

        // Fetch from network
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))

            // Fetch from API
            val allMeetings = meetingApiService.getMeetings("Bearer $token")
            Log.d(TAG, "Fetched ${allMeetings.size} meetings from API")

            // Process all meetings and update cache
            processMeetingsAndUpdateCache(allMeetings)

            val daySchedule = cachedSchedules.value[date] ?: DaySchedule(date)
            Result.success(daySchedule)
        } catch (e: Exception) {
            Log.e(TAG, "Network error fetching schedule for day $date", e)
            // Even if network fails, try to return a cached schedule if we have one
            val existingCache = cachedSchedules.value[date]
            if (existingCache != null) {
                return Result.success(existingCache)
            }

            // Return an empty schedule as a last option
            Result.success(DaySchedule(date))
        }
    }

    override suspend fun getScheduleForRange(startDate: LocalDate, endDate: LocalDate): Result<List<DaySchedule>> {
        val result = mutableListOf<DaySchedule>()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            getScheduleForDay(currentDate)
                .onSuccess { daySchedule ->
                    result.add(daySchedule)
                }
                .onFailure { exception ->
                    return Result.failure(exception)
                }
            currentDate = currentDate.plusDays(1)
        }

        return Result.success(result)
    }

    override fun getScheduleStream(date: LocalDate): Flow<DaySchedule> {
        return cachedSchedules.map { cache ->
            cache[date] ?: DaySchedule(date)
        }
    }

    override suspend fun removeMeeting(meetingId: String): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))

            // Delete from API
            meetingApiService.deleteMeeting("Bearer $token", meetingId)

            // Refresh the cache after removing a meeting
            refreshCache()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing meeting $meetingId", e)
            Result.failure(e)
        }
    }

    private suspend fun processMeetingsAndUpdateCache(meetings: List<MeetingResponse>) {
        try {
            val updatedCache = cachedSchedules.value.toMutableMap()
            val meetingEntities = mutableListOf<MeetingEntity>()

            for (meeting in meetings) {
                try {
                    val startDateTime = try {
                        // Try the full ISO format first
                        LocalDateTime.parse(meeting.startTime, DateTimeFormatter.ISO_DATE_TIME)
                    } catch (e: Exception) {
                        // Fall back to just date+time without timezone
                        LocalDateTime.parse(meeting.startTime.take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    }

                    val meetingDate = startDateTime.toLocalDate()
                    Log.d(TAG, "Processing meeting ${meeting.id} for date $meetingDate")

                    // Create entity for database
                    val participantsJson = gson.toJson(meeting.participants)
                    meetingEntities.add(
                        MeetingEntity(
                            id = meeting.id,
                            title = meeting.title,
                            description = meeting.description,
                            startTime = meeting.startTime,
                            endTime = meeting.endTime,
                            location = meeting.location,
                            meetingLink = meeting.meetingLink,
                            hostId = meeting.hostId,
                            participants = participantsJson,
                            dateKey = meetingDate.toString()
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing meeting ${meeting.id}: ${e.message}", e)
                    // Skip and continue with others
                }
            }

            // Store in database
            if (meetingEntities.isNotEmpty()) {
                meetingDao.insertAll(meetingEntities)
                Log.d(TAG, "Stored ${meetingEntities.size} meetings in database")
            }

            cachedSchedules.value = updatedCache
        } catch (e: Exception) {
            Log.e(TAG, "Error in processMeetingsAndUpdateCache: ${e.message}", e)
        }
    }

    private suspend fun refreshCache() {
        try {
            val token = authRepository.getAuthToken() ?: return

            // Fetch meetings from API
            val allMeetings = meetingApiService.getMeetings("Bearer $token")

            // Process all meetings and update cache
            processMeetingsAndUpdateCache(allMeetings)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing cache", e)
        }
    }

    override suspend fun refreshFromNetwork() {
        try {
            val token = authRepository.getAuthToken() ?: return
            val allMeetings = meetingApiService.getMeetings("Bearer $token")

            // Only process if we got data
            if (allMeetings.isNotEmpty()) {
                processMeetingsAndUpdateCache(allMeetings)
            } else {
                Log.d(TAG, "Received empty meetings list from network, preserving current data")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing cache", e)
            // Don't clear existing data on error
        }
    }

    suspend fun debugCheckLocalDatabase(): Int {
        return try {
            val allMeetings = meetingDao.getAllMeetings()
            Log.d(TAG, "DEBUG: Database contains ${allMeetings.size} meetings")
            allMeetings.forEach { meeting ->
                Log.d(TAG, "DEBUG: Meeting ${meeting.id} for date ${meeting.dateKey}")
            }
            allMeetings.size
        } catch (e: Exception) {
            Log.e(TAG, "Error checking database: ${e.message}", e)
            -1
        }
    }

    override suspend fun clearCache() {
        cachedSchedules.value = emptyMap()
        meetingDao.clearMeetings()
    }
}
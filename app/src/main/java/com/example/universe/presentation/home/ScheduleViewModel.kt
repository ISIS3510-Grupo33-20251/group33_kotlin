package com.example.universe.presentation.schedule

import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.data.repositories.ScheduleRepositoryImpl
import com.example.universe.domain.models.DaySchedule
import com.example.universe.domain.models.Meeting
import com.example.universe.domain.repositories.NetworkConnectivityObserver
import com.example.universe.domain.repositories.NetworkStatus
import com.example.universe.domain.repositories.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    // The currently selected date
    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // The formatted date string for display
    private val _formattedDate = MutableStateFlow<String>(formatDate(LocalDate.now()))
    val formattedDate: StateFlow<String> = _formattedDate.asStateFlow()

    // The current week's dates
    private val _weekDates = MutableStateFlow<List<LocalDate>>(getWeekDates(LocalDate.now()))
    val weekDates: StateFlow<List<LocalDate>> = _weekDates.asStateFlow()

    // The schedule for the selected date
    private val _currentSchedule = MutableStateFlow<DaySchedule?>(null)
    val currentSchedule: StateFlow<DaySchedule?> = _currentSchedule.asStateFlow()

    // Meetings for the selected date
    private val _timeSlots = MutableStateFlow<List<TimeSlot>>(emptyList())
    val timeSlots: StateFlow<List<TimeSlot>> = _timeSlots.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Network status
    private val _networkStatus = MutableStateFlow(NetworkStatus.Available)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    // Offline mode
    private val _offlineMode = MutableStateFlow(false)
    val offlineMode: StateFlow<Boolean> = _offlineMode.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Flag to track initial load completion
    private val _initialLoadComplete = MutableStateFlow(false)
    val initialLoadComplete: StateFlow<Boolean> = _initialLoadComplete.asStateFlow()

    init {

        viewModelScope.launch {
            _isLoading.value = true

            // Add a small delay to ensure database connection is ready
            delay(100)

            loadScheduleForSelectedDate(localOnly = false)
                .invokeOnCompletion {
                    _initialLoadComplete.value = true
                    _isLoading.value = false
                }
        }

        viewModelScope.launch {
            val meetingCount = (scheduleRepository as ScheduleRepositoryImpl).debugCheckLocalDatabase()
            Log.d("ScheduleViewModel", "Database contains $meetingCount meetings")
        }

        // Initial load
        loadScheduleForSelectedDate(localOnly = false)

        // Start observing schedule changes
        viewModelScope.launch {
            networkConnectivityObserver.observe().collect { status ->
                Log.d("ScheduleViewModel", "Network status changed to: $status")
                _networkStatus.value = status

                // Update the offline indicator
                _offlineMode.value = (status == NetworkStatus.Lost || status == NetworkStatus.Unavailable)

                // offline
                if (_offlineMode.value) {
                    loadScheduleForSelectedDate(localOnly = true, showOfflineMessage = true)
                } else {
                    // back online, refresh
                    loadScheduleForSelectedDate(forceNetworkRefresh = true)
                }
            }
        }

        // Keep observing the schedule stream for changes
        viewModelScope.launch {
            _selectedDate.collectLatest { date ->
                scheduleRepository.getScheduleStream(date).collect { daySchedule ->
                    _currentSchedule.value = daySchedule
                    processScheduleIntoTimeSlots(daySchedule)
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        if (_selectedDate.value != date) {
            _selectedDate.value = date
            _formattedDate.value = formatDate(date)
            _weekDates.value = getWeekDates(date)
            loadScheduleForSelectedDate()
        }
    }

    fun goToToday() {
        selectDate(LocalDate.now())
    }

    fun goToNextDay() {
        selectDate(_selectedDate.value.plusDays(1))
    }

    fun goToPreviousDay() {
        selectDate(_selectedDate.value.minusDays(1))
    }

    fun goToNextWeek() {
        selectDate(_selectedDate.value.plusWeeks(1))
    }

    fun goToPreviousWeek() {
        selectDate(_selectedDate.value.minusWeeks(1))
    }

    fun loadScheduleForSelectedDate(forceNetworkRefresh: Boolean = false, localOnly: Boolean = false, showOfflineMessage: Boolean = false): Job {
        return viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            scheduleRepository.getScheduleForDay(_selectedDate.value, forceNetworkRefresh, localOnly || _offlineMode.value)
                .onSuccess { daySchedule ->
                    _currentSchedule.value = daySchedule
                    processScheduleIntoTimeSlots(daySchedule)

                    // Only set error message if explicitly showing offline
                    if (showOfflineMessage || _offlineMode.value) {
                        _error.value = "Showing local data. You are offline."
                    } else {
                        _error.value = null
                    }
                }
                .onFailure { exception ->
                    if (localOnly || _offlineMode.value) {
                        _error.value = "Showing local data. You are offline."
                        // Try to show cached data
                        _currentSchedule.value?.let { processScheduleIntoTimeSlots(it) }
                    } else {
                        _error.value = exception.message ?: "Failed to load schedule"
                    }
                }

            _isLoading.value = false
        }
    }

    private fun processScheduleIntoTimeSlots(daySchedule: DaySchedule) {
        Log.d("ScheduleViewModel", "Processing schedule with ${daySchedule.meetings.size} meetings")
        val slots = mutableListOf<TimeSlot>()

        // Create time slots
        for (hour in 0..23) {
            val time = String.format("%02d:00", hour)
            val meetings = findMeetingsForTimeSlot(daySchedule.meetings, hour)
            slots.add(TimeSlot(time, meetings))

            if (meetings.isNotEmpty()) {
                Log.d("ScheduleViewModel", "Time slot $time has ${meetings.size} meetings")
            }
        }

        _timeSlots.value = slots
    }

    private fun findMeetingsForTimeSlot(meetings: List<Meeting>, hour: Int): List<Meeting> {
        return meetings.filter { meeting ->
            val startHour = extractHourFromIsoTime(meeting.startTime)
            startHour == hour
        }
    }

    private fun extractHourFromIsoTime(isoTime: String): Int {
        return try {
            // Parse the ISO time string and extract just the hour component
            val timeString = isoTime.substring(11, 19) // Extract just the time part (HH:MM:SS)
            val time = LocalTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_TIME)
            time.hour
        } catch (e: Exception) {
            // Default to -1 if parsing fails
            -1
        }
    }

    private fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
        return date.format(formatter)
    }

    private fun getWeekDates(forDate: LocalDate): List<LocalDate> {
        // Find the Monday of the current week
        val monday = forDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        // Create a list Monday to Friday
        return (0..6).map { monday.plusDays(it.toLong()) }
    }
}

data class TimeSlot(
    val time: String,
    val meetings: List<Meeting>
)
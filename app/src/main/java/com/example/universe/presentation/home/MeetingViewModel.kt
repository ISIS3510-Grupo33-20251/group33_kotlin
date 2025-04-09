package com.example.universe.presentation.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.domain.models.Meeting
import com.example.universe.domain.repositories.MeetingRepository
import com.example.universe.domain.repositories.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    private val meetingRepository: MeetingRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _meetingState = MutableStateFlow<MeetingState>(MeetingState.Initial)
    val meetingState: StateFlow<MeetingState> = _meetingState.asStateFlow()

    private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())
    val meetings: StateFlow<List<Meeting>> = _meetings.asStateFlow()

    init {
        loadMeetings()
    }

    fun createMeeting(title: String, day: String, startTime: String, endTime: String, frequency: String) {
        viewModelScope.launch {
            _meetingState.value = MeetingState.Loading

            meetingRepository.createMeeting(title, day, startTime, endTime, "Created from mobile app", frequency)
                .onSuccess {
                    _meetingState.value = MeetingState.Success
                    // Reload the meetings list
                    loadMeetings()
                    scheduleRepository.refreshFromNetwork()
                }
                .onFailure { e ->
                    _meetingState.value = MeetingState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun loadMeetings() {
        viewModelScope.launch {
            meetingRepository.getMeetings()
                .onSuccess { meetings ->
                    _meetings.value = meetings
                }
                .onFailure { e ->

                }
        }
    }

    fun deleteMeeting(meetingId: String) {
        viewModelScope.launch {
            _meetingState.value = MeetingState.Loading

            meetingRepository.deleteMeeting(meetingId)
                .onSuccess {
                    _meetingState.value = MeetingState.Success
                    // Update the meetings list
                    _meetings.value = _meetings.value.filter { it.id != meetingId }
                }
                .onFailure { e ->
                    _meetingState.value = MeetingState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun resetState() {
        _meetingState.value = MeetingState.Initial
    }
}

sealed class MeetingState {
    object Initial : MeetingState()
    object Loading : MeetingState()
    object Success : MeetingState()
    data class Error(val message: String) : MeetingState()
}
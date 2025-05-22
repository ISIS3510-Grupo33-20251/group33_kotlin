package com.example.universe.presentation.reminders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.domain.models.Reminder
import com.example.universe.domain.repositories.NetworkConnectivityObserver
import com.example.universe.domain.repositories.NetworkStatus
import com.example.universe.domain.usecases.CreateReminderUseCase
import com.example.universe.domain.usecases.DeleteReminderUseCase
import com.example.universe.domain.usecases.GetCurrentUserUseCase
import com.example.universe.domain.usecases.GetRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val createReminderUseCase: CreateReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _offlineMode = MutableStateFlow(false)
    val offlineMode: StateFlow<Boolean> = _offlineMode.asStateFlow()

    init {
        // Observe network status
        viewModelScope.launch {
            networkConnectivityObserver.observe().collect { status ->
                val wasOffline = _offlineMode.value
                _offlineMode.value = (status == NetworkStatus.Lost || status == NetworkStatus.Unavailable)

                if (_offlineMode.value) {
                    _error.value = "You are offline. Reminders will sync when you're back online."
                    loadReminders(localOnly = true)
                } else {
                    _error.value = null
                    // If we were offline and now we're back online, refresh
                    if (wasOffline) {
                        loadReminders(forceRefresh = true)
                    }
                }
            }
        }

        // Load reminders on init
        loadReminders()
    }

    private fun loadReminders(localOnly: Boolean = false, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase().first() ?: return@launch

                if (forceRefresh || _reminders.value.isEmpty()) {
                    _isLoading.value = true
                    getRemindersUseCase.getOnce(currentUser.id, localOnly)
                        .onSuccess { remindersList ->
                            _reminders.value = remindersList
                            _error.value = if (_offlineMode.value && remindersList.isNotEmpty()) {
                                "You are offline. Showing cached reminders."
                            } else null
                        }
                        .onFailure { exception ->
                            _error.value = "Failed to load reminders: ${exception.message}"
                        }
                    _isLoading.value = false
                }

                // Start observing real-time updates
                getRemindersUseCase(currentUser.id).collectLatest { remindersList ->
                    _reminders.value = remindersList
                    Log.d("ReminderViewModel", "Loaded ${remindersList.size} reminders")
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Error loading reminders: ${e.message}"
                Log.e("ReminderViewModel", "Error loading reminders", e)
            }
        }
    }

    fun createReminder(
        title: String,
        message: String,
        remindAt: LocalDateTime,
        entityType: String = "custom",
        entityId: String? = null
    ) {
        if (title.isBlank()) {
            _error.value = "Title cannot be empty"
            return
        }

        if (remindAt.isBefore(LocalDateTime.now())) {
            _error.value = "Reminder time must be in the future"
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true)

            createReminderUseCase(title, message, remindAt, entityType, entityId)
                .onSuccess { reminder ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createSuccess = true
                    )
                    _error.value = null
                    Log.d("ReminderViewModel", "Created reminder: ${reminder.title}")
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(isCreating = false)
                    _error.value = "Failed to create reminder: ${exception.message}"
                    Log.e("ReminderViewModel", "Error creating reminder", exception)
                }
        }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            deleteReminderUseCase(reminderId)
                .onSuccess {
                    _error.value = null
                    Log.d("ReminderViewModel", "Deleted reminder: $reminderId")
                }
                .onFailure { exception ->
                    _error.value = "Failed to delete reminder: ${exception.message}"
                    Log.e("ReminderViewModel", "Error deleting reminder", exception)
                }
        }
    }

    fun refreshReminders() {
        loadReminders(localOnly = _offlineMode.value, forceRefresh = true)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearCreateSuccess() {
        _uiState.value = _uiState.value.copy(createSuccess = false)
    }
}

data class ReminderUiState(
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false
)
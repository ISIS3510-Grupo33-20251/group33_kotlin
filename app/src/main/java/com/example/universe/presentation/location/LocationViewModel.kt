package com.example.universe.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.domain.models.Location
import com.example.universe.domain.repositories.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _friendLocations = MutableStateFlow<Map<String, Location>>(emptyMap())
    val friendLocations: StateFlow<Map<String, Location>> = _friendLocations.asStateFlow()

    private val _locationUpdatesEnabled = MutableStateFlow(false)
    val locationUpdatesEnabled: StateFlow<Boolean> = _locationUpdatesEnabled.asStateFlow()

    init {
        locationRepository.getCurrentLocation()
            .onEach { location ->
                _currentLocation.value = location
                // If we get a location and updates are enabled, send to server
                if (location != null && _locationUpdatesEnabled.value) {
                    updateLocationOnServer(location)
                }
            }
            .launchIn(viewModelScope)
    }

    fun startLocationUpdates() {
        _locationUpdatesEnabled.value = true
        locationRepository.startLocationUpdates()
    }

    fun stopLocationUpdates() {
        _locationUpdatesEnabled.value = false
        locationRepository.stopLocationUpdates()
    }

    fun loadFriendLocations() {
        viewModelScope.launch {
            locationRepository.getFriendLocations()
                .onSuccess { locations ->
                    _friendLocations.value = locations
                }
                .onFailure { error ->
                }
        }
    }

    private fun updateLocationOnServer(location: Location) {
        viewModelScope.launch {
            locationRepository.updateUserLocation(location)
                .onFailure { error ->
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationRepository.stopLocationUpdates()
    }
}
package com.example.universe.presentation.location

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.domain.models.FriendWithDistanceAndInfo
import com.example.universe.domain.models.Location
import com.example.universe.domain.repositories.FriendLocationRepository
import com.example.universe.domain.repositories.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val friendLocationRepository: FriendLocationRepository
) : ViewModel() {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _locationUpdatesEnabled = MutableStateFlow(false)
    val locationUpdatesEnabled: StateFlow<Boolean> = _locationUpdatesEnabled.asStateFlow()

    // Get the combined friend location info directly from the repository
    val friendsWithLocationAndInfo = friendLocationRepository.getFriendsWithLocationAndInfo()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        locationRepository.getCurrentLocation()
            .onEach { location ->
                _currentLocation.value = location
                Log.d("LocationViewModel", "Got location update: $location")
                // If we get a location and updates are enabled, send to server
                if (location != null && _locationUpdatesEnabled.value) {
                    Log.d("LocationViewModel", "Sending location to server: ${location.latitude}, ${location.longitude}")
                    updateLocationOnServer(location)
                } else {
                    Log.d("LocationViewModel", "Not sending location - enabled: ${_locationUpdatesEnabled.value}")
                }
            }
            .launchIn(viewModelScope)
    }

    fun startLocationUpdates() {
        Log.d("LocationViewModel", "startLocationUpdates called")

        if (_locationUpdatesEnabled.value) {
            Log.d("LocationViewModel", "Location updates already enabled")
            return
        }
        _locationUpdatesEnabled.value = true
        locationRepository.startLocationUpdates()
    }

    fun stopLocationUpdates() {
        _locationUpdatesEnabled.value = false
        locationRepository.stopLocationUpdates()
    }

    fun loadFriendLocations() {
        viewModelScope.launch {
            friendLocationRepository.loadFriendsWithLocation()
        }
    }

    private fun updateLocationOnServer(location: Location) {
        viewModelScope.launch {
            Log.d("LocationViewModel", "Calling updateUserLocation")
            locationRepository.updateUserLocation(location)
                .onFailure { error ->
                    Log.e("LocationViewModel", "Failed to update location: ${error.message}", error)
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationRepository.stopLocationUpdates()
    }
}
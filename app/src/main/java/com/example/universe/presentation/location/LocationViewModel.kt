package com.example.universe.presentation.location

import android.util.Log
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

    private val _friendsWithDistance = MutableStateFlow<List<FriendWithDistance>>(emptyList())
    val friendsWithDistance: StateFlow<List<FriendWithDistance>> = _friendsWithDistance.asStateFlow()

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

        combine(_currentLocation, _friendLocations) { myLocation, friendLocations ->
            Pair(myLocation, friendLocations)
        }.onEach { (myLocation, _) ->
            if (myLocation != null) {
                updateFriendsWithDistance()
            }
        }.launchIn(viewModelScope)

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
            Log.d("LocationViewModel", "Calling updateUserLocation")
            locationRepository.updateUserLocation(location)
                .onFailure { error ->
                    Log.e("LocationViewModel", "Failed to update location: ${error.message}", error)
                }
        }
    }

    fun updateFriendsWithDistance() {
        viewModelScope.launch {
            val currentLoc = _currentLocation.value ?: return@launch
            val friendLocs = _friendLocations.value

            val friendsWithDist = friendLocs.map { (friendId, location) ->
                val distance = LocationUtils.calculateDistance(
                    currentLoc.latitude, currentLoc.longitude,
                    location.latitude, location.longitude
                )
                FriendWithDistance(friendId, location, distance)
            }.sortedBy { it.distance }
            _friendsWithDistance.value = friendsWithDist
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationRepository.stopLocationUpdates()
    }
}

data class FriendWithDistance(
    val userId: String,
    val location: Location,
    val distance: Float
)
package com.example.universe.presentation.friends

import LocationUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.domain.models.FriendRequest
import com.example.universe.domain.models.Location
import com.example.universe.domain.models.User
import com.example.universe.domain.repositories.FriendRepository
import com.example.universe.domain.repositories.LocationRepository
import com.example.universe.presentation.location.FriendWithDistance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _friendsState = MutableStateFlow<FriendsState>(FriendsState.Loading)
    val friendsState: StateFlow<FriendsState> = _friendsState.asStateFlow()

    private val _friendRequestsState = MutableStateFlow<FriendRequestsState>(FriendRequestsState.Loading)
    val friendRequestsState: StateFlow<FriendRequestsState> = _friendRequestsState.asStateFlow()

    private val _pendingRequestsState = MutableStateFlow<PendingRequestsState>(PendingRequestsState.Loading)
    val pendingRequestsState: StateFlow<PendingRequestsState> = _pendingRequestsState.asStateFlow()

    private val _requestSenderInfo = MutableStateFlow<Map<String, User>>(emptyMap())
    val requestSenderInfo: StateFlow<Map<String, User>> = _requestSenderInfo.asStateFlow()

    private val _friendsWithLocation = MutableStateFlow<List<FriendWithDistance>>(emptyList())
    val friendsWithLocation: StateFlow<List<FriendWithDistance>> = _friendsWithLocation.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _friendInfoMap = MutableStateFlow<Map<String, User>>(emptyMap())
    val friendInfoMap: StateFlow<Map<String, User>> = _friendInfoMap.asStateFlow()

    init {
        loadFriends()
        loadPendingFriendRequests()
        locationRepository.getCurrentLocation()
            .onEach { currentLocation ->
                _currentLocation.value = currentLocation
                if (currentLocation != null) {
                    updateFriendsWithLocation(currentLocation)
                }
            }
            .launchIn(viewModelScope)
    }

    fun loadFriends() {
        viewModelScope.launch {
            _friendsState.value = FriendsState.Loading
            friendRepository.getFriends()
                .onSuccess { friends ->
                    _friendsState.value = FriendsState.Success(friends)
                }
                .onFailure { error ->
                    _friendsState.value = FriendsState.Error(error.message ?: "Unknown error")
                }
        }
    }

    private fun loadPendingFriendRequests() {
        viewModelScope.launch {
            _pendingRequestsState.value = PendingRequestsState.Loading
            friendRepository.getPendingFriendRequests()
                .onSuccess { requests ->
                    _pendingRequestsState.value = PendingRequestsState.Success(requests)

                    // Load user info for each request sender
                    val userMap = mutableMapOf<String, User>()
                    requests.forEach { request ->
                        launch {
                            Log.d("FriendsVM", "Loading user data for sender: ${request.senderId}")
                            friendRepository.getUserById(request.senderId)
                                .onSuccess { user ->
                                    Log.d("FriendsVM", "Successfully loaded user: ${user.name} for ID: ${request.senderId}")

                                    synchronized(userMap) {
                                        userMap[request.senderId] = user
                                        _requestSenderInfo.value = HashMap(userMap)
                                    }
                                }
                                .onFailure { error ->
                                    Log.e("FriendsVM", "Failed to load user data for sender: ${request.senderId}, Error: ${error.message}")
                                }
                        }
                    }
                }
                .onFailure { error ->
                    _pendingRequestsState.value = PendingRequestsState.Error(error.message ?: "Failed to load pending friend requests")
                }
        }
    }

    fun sendFriendRequest(email: String) {
        viewModelScope.launch {
            _friendRequestsState.value = FriendRequestsState.Loading
            friendRepository.sendFriendRequest(email)
                .onSuccess {
                    _friendRequestsState.value = FriendRequestsState.Success
                }
                .onFailure { error ->
                    _friendRequestsState.value = FriendRequestsState.Error("We could not find a User with that email")
                }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            friendRepository.acceptFriendRequest(requestId)
                .onSuccess {
                    loadPendingFriendRequests()
                    loadFriends()
                }
                .onFailure { error ->
                }
        }
    }

    fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch {
            friendRepository.rejectFriendRequest(requestId)
                .onSuccess {
                    loadPendingFriendRequests()
                }
                .onFailure { error ->
                }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            friendRepository.removeFriend(friendId)
                .onSuccess {
                    loadFriends()
                }
                .onFailure { error ->
                }
        }
    }

    private fun updateFriendsWithLocation(currentLocation: Location) {
        viewModelScope.launch {
            locationRepository.getFriendLocations()
                .onSuccess { friendLocations ->
                    val friendsWithDist = friendLocations.map { (friendId, location) ->
                        val distance = LocationUtils.calculateDistance(
                            currentLocation.latitude, currentLocation.longitude,
                            location.latitude, location.longitude
                        )
                        FriendWithDistance(friendId, location, distance)
                    }.sortedBy { it.distance }
                    _friendsWithLocation.value = friendsWithDist
                }
        }
    }

    fun loadFriendsWithLocation() {
        viewModelScope.launch {
            Log.d("FriendsViewModel", "Loading friends with location")
            locationRepository.getFriendLocations()
                .onSuccess { friendLocations ->
                    Log.d("FriendsViewModel", "Got ${friendLocations.size} friend locations")
                    val currentLoc = _currentLocation.value
                    if (currentLoc != null && friendLocations.isNotEmpty()) {
                        updateFriendsWithDistance(currentLoc, friendLocations)

                        // Fetch user info for each friend with location
                        for (friendId in friendLocations.keys) {
                            friendRepository.getUserById(friendId)
                                .onSuccess { user ->
                                    Log.d("FriendsViewModel", "Loaded user info for $friendId: ${user.name}")
                                    // Update the map with the new user info
                                    _friendInfoMap.update { currentMap ->
                                        currentMap.toMutableMap().apply {
                                            put(friendId, user)
                                        }
                                    }
                                }
                                .onFailure { error ->
                                    Log.e("FriendsViewModel", "Failed to load user for $friendId: ${error.message}")
                                }
                        }
                    }
                }
                .onFailure { error ->
                    Log.e("FriendsViewModel", "Failed to load friend locations", error)
                }
        }
    }

    private fun updateFriendsWithDistance(currentLocation: Location, friendLocations: Map<String, Location>) {
        val friendsWithDist = friendLocations.map { (friendId, location) ->
            val distance = LocationUtils.calculateDistance(
                currentLocation.latitude, currentLocation.longitude,
                location.latitude, location.longitude
            )
            FriendWithDistance(friendId, location, distance)
        }.sortedBy { it.distance }

        _friendsWithLocation.value = friendsWithDist
    }
}

sealed class FriendsState {
    object Loading : FriendsState()
    data class Success(val friends: List<User>) : FriendsState()
    data class Error(val message: String) : FriendsState()
}

sealed class FriendRequestsState {
    object Initial : FriendRequestsState()
    object Success : FriendRequestsState()
    object Loading : FriendRequestsState()
    data class Error(val message: String) : FriendRequestsState()
}

sealed class PendingRequestsState {
    object Loading : PendingRequestsState()
    data class Success(val requests: List<FriendRequest>) : PendingRequestsState()
    data class Error(val message: String) : PendingRequestsState()
}
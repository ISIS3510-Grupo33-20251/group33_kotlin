package com.example.universe.presentation.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.domain.models.FriendRequest
import com.example.universe.domain.models.User
import com.example.universe.domain.repositories.FriendLocationRepository
import com.example.universe.domain.repositories.FriendRepository
import com.example.universe.domain.repositories.NetworkConnectivityObserver
import com.example.universe.domain.repositories.NetworkStatus
import com.example.universe.presentation.location.LocationViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val networkConnectivityObserver: NetworkConnectivityObserver,
    private val friendLocationRepository: FriendLocationRepository
) : ViewModel() {

    private val _friendsState = friendRepository.getFriendsStream()
        .map { friends -> FriendsState.Success(friends) as FriendsState }
        .catch { error -> emit(FriendsState.Error(error.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.Lazily, FriendsState.Loading)
    val friendsState: StateFlow<FriendsState> = _friendsState

    private val _friendRequestsState = MutableStateFlow<FriendRequestsState>(FriendRequestsState.Loading)
    val friendRequestsState: StateFlow<FriendRequestsState> = _friendRequestsState.asStateFlow()

    private val _pendingRequestsState = MutableStateFlow<PendingRequestsState>(PendingRequestsState.Loading)
    val pendingRequestsState: StateFlow<PendingRequestsState> = _pendingRequestsState.asStateFlow()

    private val _requestSenderInfo = MutableStateFlow<Map<String, User>>(emptyMap())
    val requestSenderInfo: StateFlow<Map<String, User>> = _requestSenderInfo.asStateFlow()

    private val _friendInfoMap = MutableStateFlow<Map<String, User>>(emptyMap())
    val friendInfoMap: StateFlow<Map<String, User>> = _friendInfoMap.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Offline mode state
    private val _offlineMode = MutableStateFlow(false)
    val offlineMode: StateFlow<Boolean> = _offlineMode.asStateFlow()

    init {
        // Observe network status
        viewModelScope.launch {
            networkConnectivityObserver.observe().collect { status ->
                Log.d("FriendsViewModel", "Network status changed to: $status")
                val wasOffline = _offlineMode.value
                _offlineMode.value = (status == NetworkStatus.Lost || status == NetworkStatus.Unavailable)

                if (_offlineMode.value) {
                    _error.value = "You are offline. Showing cached friends."
                    // Load friends in offline mode
                    loadFriends(localOnly = true)
                } else {
                    _error.value = null
                    // If we were offline and now we're back online, refresh
                    if (wasOffline) {
                        loadFriends(forceRefresh = true)
                    }
                }
            }
        }

        loadFriends()
        loadPendingFriendRequests()
    }

    fun loadFriends(localOnly: Boolean = false, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            friendRepository.getFriends(localOnly)
                .onFailure { error ->
                    if (_offlineMode.value) {
                        _error.value = "You are offline. Some features may be limited."
                    } else {
                        _error.value = "Failed to load friends: ${error.message}"
                    }
                }
        }
    }

    private fun loadPendingFriendRequests() {
        viewModelScope.launch {
            _pendingRequestsState.value = PendingRequestsState.Loading

            // Don't try to load pending requests in offline mode
            if (_offlineMode.value) {
                _pendingRequestsState.value = PendingRequestsState.Success(emptyList())
                return@launch
            }

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
        // Don't try to send requests when offline
        if (_offlineMode.value) {
            _friendRequestsState.value = FriendRequestsState.Error("Cannot send friend requests while offline")
            return
        }

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
        // Don't try to accept requests when offline
        if (_offlineMode.value) {
            _error.value = "Cannot accept friend requests while offline"
            return
        }

        viewModelScope.launch {
            friendRepository.acceptFriendRequest(requestId)
                .onSuccess {
                    loadPendingFriendRequests()
                    loadFriends()
                }
                .onFailure { error ->
                    _error.value = "Failed to accept friend request: ${error.message}"
                }
        }
    }

    fun rejectFriendRequest(requestId: String) {
        // Don't try to reject requests when offline
        if (_offlineMode.value) {
            _error.value = "Cannot reject friend requests while offline"
            return
        }

        viewModelScope.launch {
            friendRepository.rejectFriendRequest(requestId)
                .onSuccess {
                    loadPendingFriendRequests()
                }
                .onFailure { error ->
                    _error.value = "Failed to reject friend request: ${error.message}"
                }
        }
    }

    fun removeFriend(friendId: String) {
        // Don't try to remove friends when offline
        if (_offlineMode.value) {
            _error.value = "Cannot remove friends while offline"
            return
        }

        viewModelScope.launch {
            friendRepository.removeFriend(friendId)
                .onSuccess {
                    loadFriends()
                }
                .onFailure { error ->
                    _error.value = "Failed to remove friend: ${error.message}"
                }
        }
    }

    fun refreshFriendLocations() {
        viewModelScope.launch {
            friendLocationRepository.loadFriendsWithLocation()
        }
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
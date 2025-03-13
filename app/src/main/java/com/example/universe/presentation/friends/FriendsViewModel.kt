package com.example.universe.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.domain.models.FriendRequest
import com.example.universe.domain.models.User
import com.example.universe.domain.repositories.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val _friendsState = MutableStateFlow<FriendsState>(FriendsState.Loading)
    val friendsState: StateFlow<FriendsState> = _friendsState.asStateFlow()

    private val _friendRequestsState = MutableStateFlow<FriendRequestsState>(FriendRequestsState.Loading)
    val friendRequestsState: StateFlow<FriendRequestsState> = _friendRequestsState.asStateFlow()

    private val _pendingRequestsState = MutableStateFlow<PendingRequestsState>(PendingRequestsState.Loading)
    val pendingRequestsState: StateFlow<PendingRequestsState> = _pendingRequestsState.asStateFlow()

    private val _requestSenderInfo = MutableStateFlow<Map<String, User>>(emptyMap())
    val requestSenderInfo: StateFlow<Map<String, User>> = _requestSenderInfo.asStateFlow()

    init {
        loadFriends()
        loadPendingFriendRequests()
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
                        friendRepository.getUserById(request.senderId)
                            .onSuccess { user ->
                                userMap[request.senderId] = user
                                _requestSenderInfo.value = userMap.toMap()
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
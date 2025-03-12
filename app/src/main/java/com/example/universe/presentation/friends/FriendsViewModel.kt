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

    init {
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

    private fun loadFriendRequests() {
        viewModelScope.launch {
            _friendRequestsState.value = FriendRequestsState.Loading
            friendRepository.getPendingFriendRequests()
                .onSuccess { requests ->
                    _friendRequestsState.value = FriendRequestsState.Success
                }
                .onFailure { error ->
                    _friendRequestsState.value = FriendRequestsState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun sendFriendRequest(email: String) {
        println("Attempting to send friend request to: $email")
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
                    loadFriendRequests()
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
                    loadFriendRequests()
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
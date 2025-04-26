package com.example.universe.domain.repositories

import com.example.universe.domain.models.FriendRequest
import com.example.universe.domain.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow

interface FriendRepository {
    suspend fun getFriends(localOnly: Boolean = false): Result<List<User>>
    suspend fun sendFriendRequest(email: String): Result<Unit>
    suspend fun getUserById(userId: String): Result<User>
    suspend fun acceptFriendRequest(requestId: String): Result<Unit>
    suspend fun rejectFriendRequest(requestId: String): Result<Unit>
    suspend fun getPendingFriendRequests(): Result<List<FriendRequest>>
    suspend fun removeFriend(friendId: String): Result<Unit>
    fun getFriendsStream(): Flow<List<User>>
    fun setOfflineMode(offline: Boolean)
    fun isOfflineMode(): Flow<Boolean>
    suspend fun clearCache()
}
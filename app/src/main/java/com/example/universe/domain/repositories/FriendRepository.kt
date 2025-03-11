package com.example.universe.domain.repositories

import com.example.universe.domain.models.FriendRequest
import com.example.universe.domain.models.User
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    suspend fun getFriends(): Result<List<User>>
    suspend fun sendFriendRequest(userId: String): Result<Unit>
    suspend fun acceptFriendRequest(requestId: String): Result<Unit>
    suspend fun rejectFriendRequest(requestId: String): Result<Unit>
    suspend fun getPendingFriendRequests(): Result<List<FriendRequest>>
    suspend fun removeFriend(friendId: String): Result<Unit>
}
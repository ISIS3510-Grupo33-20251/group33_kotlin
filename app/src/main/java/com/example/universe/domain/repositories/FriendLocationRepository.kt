package com.example.universe.domain.repositories

import com.example.universe.domain.models.FriendWithDistanceAndInfo
import kotlinx.coroutines.flow.Flow

interface FriendLocationRepository {
    fun getFriendsWithLocationAndInfo(): Flow<List<FriendWithDistanceAndInfo>>
    suspend fun loadFriendsWithLocation()
    suspend fun clearCache()
}
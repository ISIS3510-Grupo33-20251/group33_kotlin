package com.example.universe.domain.repositories

import com.example.universe.domain.models.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getCurrentLocation(): Flow<Location?>
    suspend fun updateUserLocation(location: Location): Result<Unit>
    suspend fun getFriendLocations(): Result<Map<String, Location>>
    fun startLocationUpdates()
    fun stopLocationUpdates()
}
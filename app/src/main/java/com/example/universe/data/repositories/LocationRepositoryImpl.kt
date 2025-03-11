package com.example.universe.data.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.example.universe.data.api.UserApiService
import com.example.universe.domain.models.Location as AppLocation
import com.example.universe.domain.repositories.AuthRepository
import com.example.universe.domain.repositories.LocationRepository
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val userApiService: UserApiService
) : LocationRepository {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(5000)
        .setMaxUpdateDelayMillis(15000)
        .build()

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission") // Permission should be checked before calling
    override fun getCurrentLocation(): Flow<AppLocation?> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val appLocation = AppLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        lastUpdated = System.currentTimeMillis(),
                        accuracy = location.accuracy
                    )
                    trySend(appLocation)
                }
            }
        }

        locationCallback = callback

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val appLocation = AppLocation(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    lastUpdated = System.currentTimeMillis(),
                    accuracy = it.accuracy
                )
                trySend(appLocation)
            }
        }

        awaitClose {
            locationCallback = null
        }
    }

    @SuppressLint("MissingPermission") // Permission should be checked before calling
    override fun startLocationUpdates() {
        locationCallback?.let { callback ->
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        }
    }

    override fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    override suspend fun updateUserLocation(location: AppLocation): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            userApiService.updateLocation("Bearer $token", location)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFriendLocations(): Result<Map<String, AppLocation>> {
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val friends = userApiService.getFriendsWithLocation("Bearer $token")

            val locationMap = mutableMapOf<String, AppLocation>()

            for (friend in friends) {
                val friendLocation = friend.location
                if (friendLocation != null) {
                    locationMap[friend.id] = AppLocation(
                        latitude = friendLocation.latitude,
                        longitude = friendLocation.longitude,
                        lastUpdated = friendLocation.lastUpdated ?: System.currentTimeMillis(),
                        accuracy = friendLocation.accuracy
                    )
                }
            }

            Result.success(locationMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
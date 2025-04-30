package com.example.universe.data.repositories

import android.content.SharedPreferences
import android.util.Log
import com.example.universe.domain.models.FriendWithDistanceAndInfo
import com.example.universe.domain.models.Location
import com.example.universe.domain.models.User
import com.example.universe.domain.repositories.FriendLocationRepository
import com.example.universe.domain.repositories.FriendRepository
import com.example.universe.domain.repositories.LocationRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class FriendLocationRepositoryImpl @Inject constructor(
    private val friendRepository: FriendRepository,
    private val locationRepository: LocationRepository,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : FriendLocationRepository {

    private val _friendInfoMap = MutableStateFlow<Map<String, User>>(emptyMap())
    private val _currentLocation = MutableStateFlow<Location?>(null)
    private val _friendsWithLocationAndInfo = MutableStateFlow<List<FriendWithDistanceAndInfo>>(emptyList())

    init {
        // Load cached data from preferences
        loadFriendInfoMapFromPrefs()

        // Observe location changes
        locationRepository.getCurrentLocation()
            .onEach { location ->
                _currentLocation.value = location
                updateFriendsWithDistance()
            }
            .launchIn(CoroutineScope(Dispatchers.IO))
    }

    override fun getFriendsWithLocationAndInfo(): Flow<List<FriendWithDistanceAndInfo>> {
        return _friendsWithLocationAndInfo.asStateFlow()
    }

    override suspend fun loadFriendsWithLocation() {
        locationRepository.getFriendLocations()
            .onSuccess { friendLocations ->
                for (friendId in friendLocations.keys) {
                    if (!_friendInfoMap.value.containsKey(friendId)) {
                        friendRepository.getUserById(friendId)
                            .onSuccess { user ->
                                _friendInfoMap.update { currentMap ->
                                    currentMap.toMutableMap().apply {
                                        put(friendId, user)
                                    }
                                }
                                saveFriendInfoMapToPrefs()
                                updateFriendsWithDistance()
                            }
                    }
                }
                updateFriendsWithDistance()
            }
    }

    private suspend fun updateFriendsWithDistance() {
        val currentLoc = _currentLocation.value ?: return
        val friendLocs = locationRepository.getFriendLocations().getOrNull() ?: return
        val infoMap = _friendInfoMap.value

        val friendsWithDist = friendLocs.mapNotNull { (friendId, location) ->
            val user = infoMap[friendId] ?: return@mapNotNull null

            val distance = LocationUtils.calculateDistance(
                currentLoc.latitude, currentLoc.longitude,
                location.latitude, location.longitude
            )
            FriendWithDistanceAndInfo(user, location, distance)
        }.sortedBy { it.distance }

        _friendsWithLocationAndInfo.value = friendsWithDist
    }

    private fun saveFriendInfoMapToPrefs() {
        val friendInfoJson = gson.toJson(_friendInfoMap.value)
        sharedPreferences.edit()
            .putString("friend_info_map", friendInfoJson)
            .apply()
    }

    private fun loadFriendInfoMapFromPrefs() {
        val friendInfoJson = sharedPreferences.getString("friend_info_map", null)
        if (friendInfoJson != null) {
            try {
                val typeToken = object : TypeToken<Map<String, User>>() {}.type
                val infoMap: Map<String, User> = gson.fromJson(friendInfoJson, typeToken)
                _friendInfoMap.value = infoMap
            } catch (e: Exception) {
                Log.e("FriendLocationRepo", "Error loading friend info", e)
            }
        }
    }

    override suspend fun clearCache() {
        _friendInfoMap.value = emptyMap()
        _friendsWithLocationAndInfo.value = emptyList()

        sharedPreferences.edit()
            .remove("friend_info_map")
            .apply()

        Log.d("FriendLocationRepo", "Friend location cache cleared")
    }
}
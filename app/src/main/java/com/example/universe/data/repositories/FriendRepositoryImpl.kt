package com.example.universe.data.repositories

import android.content.SharedPreferences
import android.util.Log
import com.example.universe.data.api.UserApiService
import com.example.universe.data.models.FriendRequestDto
import com.example.universe.data.models.SendFriendRequestDto
import com.example.universe.domain.models.FriendRequest
import com.example.universe.domain.models.FriendRequestStatus
import com.example.universe.domain.models.User
import com.example.universe.domain.repositories.AuthRepository
import com.example.universe.domain.repositories.FriendRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException
import javax.inject.Inject
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

class FriendRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val authRepository: AuthRepository,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : FriendRepository {

    override suspend fun getFriends(): Result<List<User>> {
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val currentUser = authRepository.getCurrentUser().first() ?: return Result.failure(Exception("User not found"))

            val response = userApiService.getFriends("Bearer $token", currentUser.id)

            val friends = response.map { userDto ->
                User(
                    id = userDto.id,
                    email = userDto.email,
                    name = userDto.name,
                    preferences = userDto.preferences?.let { gson.fromJson(it, Map::class.java) as? Map<String, Any> },
                    subscriptionStatus = userDto.subscriptionStatus ?: false,
                    location = userDto.location?.let {
                        com.example.universe.domain.models.Location(
                            latitude = it.latitude,
                            longitude = it.longitude,
                            lastUpdated = it.lastUpdated ?: System.currentTimeMillis(),
                            accuracy = it.accuracy
                        )
                    }
                )
            }

            Result.success(friends)
        } catch (e: Exception) {
            if (e is HttpException) {
                Result.failure(Exception("Failed to get friends: ${e.message()}"))
            } else {
                Result.failure(Exception("Failed to get friends: ${e.localizedMessage}"))
            }
        }
    }

    override suspend fun sendFriendRequest(email: String): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))

            val userJson = sharedPreferences.getString("user", null)
            val senderId = if (userJson != null) {
                try {
                    val user = gson.fromJson(userJson, User::class.java)
                    user?.id ?: return Result.failure(Exception("User ID not found"))
                } catch (e: Exception) {
                    return Result.failure(Exception("Failed to parse user data: ${e.message}"))
                }
            } else {
                return Result.failure(Exception("User not logged in"))
            }

            val request = SendFriendRequestDto(senderId, email)

            println("Sending friend request: $request") // Add logging

            userApiService.sendFriendRequest("Bearer $token", request)
            Result.success(Unit)
        } catch (e: Exception) {
            println("Friend request error: ${e.message}") // Add logging
            e.printStackTrace() // Print stack trace
            if (e is HttpException && e.code() == 404) {
                Result.failure(Exception("User not found"))
            } else {
                Result.failure(Exception("Failed to send friend request: ${e.localizedMessage}"))
            }
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            userApiService.acceptFriendRequest("Bearer $token", requestId)
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is HttpException) {
                Result.failure(Exception("Failed to accept friend request: ${e.message()}"))
            } else {
                Result.failure(Exception("Failed to accept friend request: ${e.localizedMessage}"))
            }
        }
    }

    override suspend fun rejectFriendRequest(requestId: String): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            userApiService.rejectFriendRequest("Bearer $token", requestId)
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is HttpException) {
                Result.failure(Exception("Failed to reject friend request: ${e.message()}"))
            } else {
                Result.failure(Exception("Failed to reject friend request: ${e.localizedMessage}"))
            }
        }
    }

    override suspend fun getPendingFriendRequests(): Result<List<FriendRequest>> {
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val currentUser = authRepository.getCurrentUser().first() ?: return Result.failure(Exception("User not found"))

            val response = userApiService.getPendingFriendRequests("Bearer $token", currentUser.id)

            val requests = response.map { dto ->
                FriendRequest(
                    id = dto.id,
                    senderId = dto.senderId,
                    receiverId = dto.receiverId,
                    status = dto.status,
                    createdAt = dto.createdAt
                )
            }

            Result.success(requests)
        } catch (e: Exception) {
            if (e is HttpException) {
                Result.failure(Exception("Failed to get friend requests: ${e.message()}"))
            } else {
                Result.failure(Exception("Failed to get friend requests: ${e.localizedMessage}"))
            }
        }
    }

    override suspend fun removeFriend(friendId: String): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            userApiService.removeFriend("Bearer $token", friendId)
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is HttpException) {
                Result.failure(Exception("Failed to remove friend: ${e.message()}"))
            } else {
                Result.failure(Exception("Failed to remove friend: ${e.localizedMessage}"))
            }
        }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            val token = authRepository.getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val response = userApiService.getUserById("Bearer $token", userId)
            Log.d("FriendRepo", "Received user data: $response")

            val preferencesMap: Map<String, Any>? = if (response.preferences != null) {
                try {
                    // Convert JsonObject to Map using a more explicit approach
                    val type = object : TypeToken<Map<String, Any>>() {}.type
                    gson.fromJson<Map<String, Any>>(response.preferences.toString(), type)
                } catch (e: Exception) {
                    Log.e("FriendRepo", "Error parsing preferences", e)
                    null
                }
            } else {
                null
            }

            val user = User(
                id = response.id,
                email = response.email,
                name = response.name,
                preferences = preferencesMap,
                subscriptionStatus = response.subscriptionStatus ?: false,
                location = response.location?.let {
                    com.example.universe.domain.models.Location(
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                }
            )
            Log.d("FriendRepo", "Mapped user: $user")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get user: ${e.message}"))
        }
    }
}
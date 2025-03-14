package com.example.universe.data.api

import com.example.universe.data.models.FriendRequestDto
import com.example.universe.data.models.UserDto
import com.example.universe.data.models.SendFriendRequestDto
import com.example.universe.domain.models.Location
import retrofit2.http.*

interface UserApiService {
    @GET("users/{userId}/friends")
    suspend fun getFriends(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): List<UserDto>

    @GET("friend_requests/pending/{userId}")
    suspend fun getPendingFriendRequests(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): List<FriendRequestDto>

    @GET("users/{userId}/friends/location")
    suspend fun getFriendsWithLocation(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): List<UserDto>

    @PUT("users/{user_id}/location")
    suspend fun updateLocation(
        @Header("Authorization") token: String,
        @Path("user_id") userId: String,
        @Body location: Location
    )

    @POST("friend_requests/by_email")
    suspend fun sendFriendRequest(
        @Header("Authorization") token: String,
        @Body request: SendFriendRequestDto
    ): okhttp3.ResponseBody

    @POST("friend_requests/{requestId}/accept")
    suspend fun acceptFriendRequest(
        @Header("Authorization") token: String,
        @Path("requestId") requestId: String
    )

    @POST("friend_requests/{requestId}/reject")
    suspend fun rejectFriendRequest(
        @Header("Authorization") token: String,
        @Path("requestId") requestId: String
    )

    @DELETE("users/{user_id}/friends/{friend_id}")
    suspend fun removeFriend(
        @Header("Authorization") token: String,
        @Path("friend_id") friendId: String
    )

    @GET("users/{userId}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): UserDto
}
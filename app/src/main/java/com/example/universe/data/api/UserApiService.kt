package com.example.universe.data.api

import com.example.universe.data.models.FriendRequestDto
import com.example.universe.data.models.UserDto
import com.example.universe.data.models.SendFriendRequestDto
import com.example.universe.domain.models.Location
import retrofit2.http.*

interface UserApiService {
    @GET("users/friends")
    suspend fun getFriends(@Header("Authorization") token: String): List<UserDto>

    @GET("users/friends/location")
    suspend fun getFriendsWithLocation(@Header("Authorization") token: String): List<UserDto>

    @PUT("users/location")
    suspend fun updateLocation(
        @Header("Authorization") token: String,
        @Body location: Location
    )

    @POST("friend_requests/by_email")
    suspend fun sendFriendRequest(
        @Header("Authorization") token: String,
        @Body request: SendFriendRequestDto
    ): okhttp3.ResponseBody

    @GET("friend_requests/pending")
    suspend fun getPendingFriendRequests(@Header("Authorization") token: String): List<FriendRequestDto>

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
}
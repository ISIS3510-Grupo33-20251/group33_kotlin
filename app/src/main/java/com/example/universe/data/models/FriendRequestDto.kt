package com.example.universe.data.models

import com.example.universe.domain.models.FriendRequestStatus
import com.google.gson.annotations.SerializedName

data class FriendRequestDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("sender_id")
    val senderId: String,
    @SerializedName("receiver_id")
    val receiverId: String,
    val status: FriendRequestStatus,
    @SerializedName("created_at")
    val createdAt: Double
    )

data class SendFriendRequestDto(
    val senderId: String,
    val email: String
)
package com.example.universe.data.models

import com.example.universe.domain.models.FriendRequestStatus

data class FriendRequestDto(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val status: FriendRequestStatus,
    val createdAt: Long
    )

data class SendFriendRequestDto(
    val senderId: String,
    val email: String
)
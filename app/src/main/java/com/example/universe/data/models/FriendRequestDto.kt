package com.example.universe.data.models

data class FriendRequestDto(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val status: String,
    val createdAt: Long
)
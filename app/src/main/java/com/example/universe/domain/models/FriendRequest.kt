package com.example.universe.domain.models

enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class FriendRequest(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val status: FriendRequestStatus,
    val createdAt: Long
)
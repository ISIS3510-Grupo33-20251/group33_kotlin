package com.example.universe.domain.models

data class FriendWithDistanceAndInfo(
    val user: User,
    val location: Location,
    val distance: Float
)
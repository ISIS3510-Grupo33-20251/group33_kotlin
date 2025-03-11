package com.example.universe.data.models

import com.google.gson.JsonObject

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val preferences: String? = null,  // JSON string of preferences
    val subscriptionStatus: Boolean? = false,
    val location: LocationDto? = null,
    val tasks: List<String> = emptyList(),
    val documents: List<String> = emptyList(),
    val teams: List<String> = emptyList(),
    val friends: List<String> = emptyList()
)

data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val lastUpdated: Long? = null,
    val accuracy: Float? = null
)
package com.example.universe.data.models

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val preferences: String? = null,  // JSON string of preferences
    @SerializedName("subscription_status")
    val subscriptionStatus: Boolean? = false,
    val location: LocationDto? = null,
    val tasks: List<String> = emptyList(),
    val documents: List<String> = emptyList(),
    val teams: List<String> = emptyList(),
    val friends: List<String> = emptyList(),
    @SerializedName("flashcard_decks")
    val flashcardDecks: List<String> = emptyList(),
    val courses: List<String> = emptyList(),
    val notes: List<String> = emptyList()
)

data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val lastUpdated: Long? = null,
    val accuracy: Float? = null
)
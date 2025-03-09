package com.example.universe.domain.models

data class User(
    val id: String,
    val email: String,
    val name: String,
    val preferences: Map<String, Any>? = null,
    val subscriptionStatus: Boolean = false,
    val location: Location? = null
)

data class Location(
    val latitude: Double,
    val longitude: Double
)
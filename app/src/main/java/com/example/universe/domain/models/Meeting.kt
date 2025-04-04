package com.example.universe.domain.models

data class Meeting(
    val id: String,
    val title: String,
    val description: String?,
    val startTime: String,  // ISO format time string
    val endTime: String,    // ISO format time string
    val location: String?,
    val meetingLink: String?,
    val hostId: String,
    val participants: List<String>
)
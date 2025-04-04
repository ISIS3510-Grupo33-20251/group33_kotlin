package com.example.universe.data.models

import com.google.gson.annotations.SerializedName

data class MeetingRequest(
    val title: String,
    val description: String?,
    @SerializedName("start_time")
    val startTime: String,  // ISO format: 2023-06-26T14:30:00
    @SerializedName("end_time")
    val endTime: String,    // ISO format: 2023-06-26T15:30:00
    val location: String? = null,
    @SerializedName("meeting_link")
    val meetingLink: String? = null,
    @SerializedName("host_id")
    val hostId: String,
    val participants: List<String> = emptyList()
)

data class MeetingResponse(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val description: String?,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    val location: String?,
    @SerializedName("meeting_link")
    val meetingLink: String?,
    @SerializedName("host_id")
    val hostId: String,
    val participants: List<String>
)
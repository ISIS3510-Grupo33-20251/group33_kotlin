package com.example.universe.data.models

import com.google.gson.annotations.SerializedName

data class ReminderDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("entity_type") val entityType: String,
    @SerializedName("entity_id") val entityId: String?,
    val title: String?,
    val message: String?,
    @SerializedName("remind_at") val remindAt: String, // ISO format
    val status: String
)
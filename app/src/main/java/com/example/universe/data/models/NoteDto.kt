package com.example.universe.data.models

import com.google.gson.annotations.SerializedName

data class NoteDto(
    @SerializedName("_id") val id: String? = null,  // ID opcional
    val title: String,
    val content: String,
    @SerializedName("created_date") val createdDate: String? = null,
    @SerializedName("last_modified") val lastModified: String? = null
)

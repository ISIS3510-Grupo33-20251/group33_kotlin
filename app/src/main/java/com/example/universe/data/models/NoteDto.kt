package com.example.universe.data.models

import com.google.gson.annotations.SerializedName

data class NoteDto(
    @SerializedName("_id") val id: String? = null,
    val title: String,
    val subject: String,
    val content: String,
    val tags: List<String> = emptyList<String>(),
    @SerializedName("created_date") val created_date: String? = null,
    @SerializedName("last_modified") val last_modified: String? = null,
    @SerializedName("owner_id") val owner_id: String? = null
    ) {
}


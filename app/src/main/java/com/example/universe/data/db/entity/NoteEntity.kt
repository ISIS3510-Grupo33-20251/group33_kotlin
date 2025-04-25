package com.example.universe.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val content: String,
    val tags: String, // Guardamos como JSON o separados por comas
    val created_date: String?,  // Usar snake_case
    val last_modified: String?, // Usar snake_case
    val owner_id: String?,
    val needsSync: Boolean = false
)
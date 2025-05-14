package com.example.universe.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val content: String,
    val tags: String, // Guarda como string JSON o CSV
    val created_date: String?,
    val last_modified: String?,
    val owner_id: String?,
    val isSynced: Boolean = false, // clave para conectividad eventual
    val deleted: Boolean = false
)

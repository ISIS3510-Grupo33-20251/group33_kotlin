package com.example.universe.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val subjectName: String,
    val ownerId: String,
    val entriesJson: String, // serializas las entradas como JSON
    val createdDate: String?,
    val lastModified: String?,
    
)

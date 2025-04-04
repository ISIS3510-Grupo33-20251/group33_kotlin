package com.example.universe.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend")
data class FriendEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val preferences: String?, // JSON string of preferences
    val subscriptionStatus: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)
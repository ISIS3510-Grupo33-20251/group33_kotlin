package com.example.universe.data.db.entity

import androidx.room.Entity
import com.example.universe.data.models.Flashcard

@Entity(tableName = "flashcards", primaryKeys = ["userId", "subject", "question"])
data class FlashcardEntity(
    val userId: String,
    val subject: String,
    val question: String,
    val answer: String
)

fun FlashcardEntity.toDomain(): Flashcard = Flashcard(question, answer)

fun Flashcard.toEntity(userId: String, subject: String): FlashcardEntity =
    FlashcardEntity(userId, subject, question, answer)

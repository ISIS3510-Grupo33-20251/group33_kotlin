package com.example.universe.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.example.universe.data.db.entity.FlashcardEntity
import com.example.universe.data.models.Flashcard

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE userId = :userId AND subject = :subject")
    suspend fun getFlashcards(userId: String, subject: String): List<FlashcardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flashcards: List<FlashcardEntity>)

    @Query("DELETE FROM flashcards WHERE userId = :userId AND subject = :subject")
    suspend fun deleteByUserAndSubject(userId: String, subject: String)

    @Query("DELETE FROM flashcards")
    suspend fun clearAll()
}


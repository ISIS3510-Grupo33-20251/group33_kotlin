package com.example.universe.data.repositories

import com.example.universe.data.api.FlashcardApiService
import com.example.universe.data.models.Flashcard
import javax.inject.Inject

class FlashcardRepository @Inject constructor(
    private val api: FlashcardApiService
) {
    suspend fun getFlashcards(userId: String, subject: String): List<Flashcard> {
        return api.getFlashcards(userId, subject)
    }
}

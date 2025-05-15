package com.example.universe.data.repositories

import com.example.universe.data.api.FlashcardApiService
import com.example.universe.data.db.dao.FlashcardDao
import com.example.universe.data.db.entity.toDomain
import com.example.universe.data.db.entity.toEntity
import com.example.universe.data.models.Flashcard
import javax.inject.Inject

class FlashcardRepository @Inject constructor(
    private val api: FlashcardApiService,
    private val dao: FlashcardDao
) {
    suspend fun getFlashcards(userId: String, subject: String): List<Flashcard> {
        return try {
            val flashcards = api.getFlashcards(userId, subject)
            val entities = flashcards.map { it.toEntity(userId, subject) }
            dao.deleteByUserAndSubject(userId, subject)
            dao.insertAll(entities)
            flashcards
        } catch (e: Exception) {
            val local = dao.getFlashcards(userId, subject)
            if (local.isNotEmpty()) {
                local.map { it.toDomain() }
            } else {
                throw e // O puedes devolver una lista vacía si prefieres
            }
        }
    }

    suspend fun clearAllCache() = dao.clearAll()
}

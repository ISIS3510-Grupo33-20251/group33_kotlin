package com.example.universe.data.repositories

import android.util.LruCache
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

    // Tamaño arbitrario para el cache en cantidad de flashcards
    private val cacheSize = 50

    // Clave será "userId-subject", valor es lista de flashcards
    private val flashcardCache = object : LruCache<String, List<Flashcard>>(cacheSize) {}

    suspend fun getFlashcards(userId: String, subject: String): List<Flashcard> {
        val key = "$userId-$subject"

        // 1. Intentar devolver del cache LRU si existe
        flashcardCache.get(key)?.let {
            return it
        }

        return try {
            // 2. Si no está en cache, ir a la red
            val flashcards = api.getFlashcards(userId, subject)

            // Guardar en DB
            val entities = flashcards.map { it.toEntity(userId, subject) }
            dao.deleteByUserAndSubject(userId, subject)
            dao.insertAll(entities)

            // Guardar en cache LRU
            flashcardCache.put(key, flashcards)

            flashcards
        } catch (e: Exception) {
            // 3. En caso de error, intentar desde la BD local
            val local = dao.getFlashcards(userId, subject)
            if (local.isNotEmpty()) {
                val localFlashcards = local.map { it.toDomain() }

                // Guardar en cache LRU para futuras consultas
                flashcardCache.put(key, localFlashcards)

                localFlashcards
            } else {
                // Si no hay nada local, propagar el error o retornar lista vacía
                throw e
            }
        }
    }

    suspend fun clearAllCache() {
        dao.clearAll()
        flashcardCache.evictAll()  // Limpiar cache LRU en memoria también
    }
}

package com.example.universe.data.repositories

import com.example.universe.data.mappers.toDto
import com.example.universe.data.mappers.toEntity
import com.example.universe.data.api.NoteApiService
import com.example.universe.data.api.UserApiService
import com.example.universe.data.db.dao.NoteDao
import com.example.universe.data.models.NoteDto
import retrofit2.Response
import javax.inject.Inject


class NoteRepository @Inject constructor(
    private val api: NoteApiService,
    private val apiUser: UserApiService,
    private val noteDao: NoteDao) {

    suspend fun getNotes(): Response<List<NoteDto>> {
        // 1. Obtener notas locales y devolverlas primero
        val localNotes = noteDao.getAllNotes().map { it.toDto() }

        // 2. Intentar sincronizar con la API remota
        return try {
            val response = api.getNotes()
            if (response.isSuccessful) {
                val remoteNotes = response.body() ?: emptyList()

                // Reemplazar localmente con las notas remotas
                noteDao.deleteAllNotes()
                noteDao.insertNotes(remoteNotes.map { it.toEntity() })

                Response.success(remoteNotes)
            } else {
                // Si falla la API, devolvemos las locales como fallback
                Response.success(localNotes)
            }
        } catch (e: Exception) {
            // En caso de error de red u otro, también devolvemos las locales
            Response.success(localNotes)
        }
    }

    suspend fun getNoteById(id: String): Response<NoteDto> {
        return api.getNoteById(id)
    }

    suspend fun createNote(note: NoteDto): Response<NoteDto> {
        return api.createNote(note)
    }

    suspend fun updateNote(id: String, note: NoteDto): Response<NoteDto> {
        return api.updateNote(id, note)
    }

    suspend fun deleteNote(id: String): Response<Unit> {
        return api.deleteNote(id)
    }

    suspend fun addNoteToUser(userId: String, noteId: String): Response<Unit> {
        return apiUser.addNoteToUser(userId, noteId)
    }

}

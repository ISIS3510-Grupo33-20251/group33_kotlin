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
    private val noteDao: NoteDao
) {

    suspend fun getNotes(): Response<List<NoteDto>> {
        // 1. Obtener notas locales
        val localNotes = noteDao.getAllNotes().map { it.toDto() }

        // 2. Intentar sincronizar con la API remota
        return try {
            val response = api.getNotes()
            if (response.isSuccessful) {
                val remoteNotes = response.body() ?: emptyList()

                // Insertar o actualizar notas remotas en la base de datos
                remoteNotes.forEach { remoteNote ->
                    val existingNote = noteDao.getNoteById(remoteNote.id ?: "")
                    if (existingNote == null) {
                        // Insertar si la nota no existe
                        noteDao.insertNote(remoteNote.toEntity(needsSync = false))
                    } else {
                        // Actualizar si ya existe
                        noteDao.insertNote(remoteNote.toEntity(needsSync = false))
                    }
                }

                Response.success(remoteNotes)
            } else {
                // Si la API falla, devolvemos las notas locales como fallback
                Response.success(localNotes)
            }
        } catch (e: Exception) {
            // En caso de error de red u otro, devolvemos las notas locales
            Response.success(localNotes)
        }
    }

    suspend fun getNoteById(id: String): Response<NoteDto> {
        return api.getNoteById(id)
    }

    suspend fun createNote(note: NoteDto): Response<NoteDto> {
        return try {
            // Intentar crear la nota remotamente
            val response = api.createNote(note)
            if (response.isSuccessful) {
                val createdNote = response.body()!!

                // Guardar en base local, marcada como sincronizada
                noteDao.insertNote(createdNote.toEntity(needsSync = false))
                Response.success(createdNote)
            } else {
                // Si falla el servidor, guardar localmente para sincronizar luego
                noteDao.insertNote(note.toEntity(needsSync = true))
                Response.success(note)
            }
        } catch (e: Exception) {
            // Error de red u otro, guardar localmente para sincronizar luego
            noteDao.insertNote(note.toEntity(needsSync = true))
            Response.success(note)
        }
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

    // Método para sincronizar notas que necesitan ser sincronizadas
    suspend fun syncPendingNotes() {
        // Obtener todas las notas locales que necesitan sincronización
        val pendingNotes = noteDao.getAllNotes().filter { it.needsSync }

        // Sincronizar notas pendientes con la API
        pendingNotes.forEach { pendingNote ->
            try {
                val response = api.createNote(pendingNote.toDto()) // Intenta crear la nota en el servidor
                if (response.isSuccessful) {
                    // Si la sincronización fue exitosa, marcar la nota como sincronizada
                    val updatedNote = pendingNote.copy(needsSync = false)
                    noteDao.insertNote(updatedNote) // Actualizar la nota en la base de datos
                }
            } catch (e: Exception) {
                // Si hay error en la sincronización, no hacer nada y continuar
                // La nota permanecerá marcada como needsSync = true
            }
        }
    }

    // Método para sincronizar las notas pendientes si hay conexión
    suspend fun syncNotesIfNetworkAvailable(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            syncPendingNotes() // Ejecuta la sincronización si hay conexión
        }
    }
}

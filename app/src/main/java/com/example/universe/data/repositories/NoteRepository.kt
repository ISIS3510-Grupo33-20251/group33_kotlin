package com.example.universe.data.repositories

import com.example.universe.data.api.NoteApiService
import com.example.universe.data.api.UserApiService
import com.example.universe.data.db.dao.NoteDao
import com.example.universe.data.mappers.NoteMapper
import com.example.universe.data.mappers.NoteMapper.toDto
import com.example.universe.data.models.NoteDto
import com.example.universe.work.NetworkUtils
import retrofit2.Response
import javax.inject.Inject


class NoteRepository @Inject constructor(
    private val api: NoteApiService,
    private val apiUser: UserApiService,
    private val noteDao: NoteDao
) {

    suspend fun getNotes(userId: String): Response<List<NoteDto>> {
        return api.getNotes(userId)
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
        try {
            // Eliminar la nota en el servidor
            val response = api.deleteNote(id)
            if (response.isSuccessful) {
                // Si la eliminación fue exitosa en el servidor, eliminar también la nota localmente
                noteDao.deleteNoteById(id)
            }
            return response
        } catch (e: Exception) {
            // En caso de error, simplemente retorna el error (puedes manejarlo más detalladamente si lo deseas)
            throw e
        }
    }


    suspend fun addNoteToUser(userId: String, noteId: String): Response<Unit> {
        return apiUser.addNoteToUser(userId, noteId)
    }

    suspend fun createNoteOffline(noteDto: NoteDto) {
        val entity = NoteMapper.fromDto(noteDto).copy(isSynced = false)
        noteDao.insertNote(entity)
    }

    suspend fun getNotesOffline(userId: String): List<NoteDto> {
        return noteDao.getNotesByUser(userId).map { NoteMapper.toDto(it) }
    }

    suspend fun updateNoteOffline(note: NoteDto) {
        val entity = NoteMapper.fromDto(note).copy(isSynced = false)
        noteDao.updateNote(entity)
    }

    suspend fun deleteNoteOffline(noteId: String) {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            // Marcar como eliminada sin borrarla aún
            val deletedNote = note.copy(deleted = true, isSynced = false)
            noteDao.updateNote(deletedNote)
        }
    }

    suspend fun syncPendingNotes() {
        val pendingNotes = noteDao.getPendingNotes()

        for (noteEntity in pendingNotes) {
            try {
                val noteDto = NoteMapper.toDto(noteEntity)

                if (noteEntity.deleted) {
                    // Si la nota está eliminada, intentamos eliminarla en el servidor
                    val response = api.deleteNote(noteEntity.id)
                    if (response.isSuccessful) {
                        // Eliminar la nota localmente si se elimina correctamente en el servidor
                        noteDao.deleteNoteById(noteEntity.id)
                    } else {
                        // Si no fue exitosa la eliminación, se puede manejar con alguna lógica adicional si es necesario
                    }
                } else {
                    // Si no está marcada como eliminada, procedemos a crearla en el servidor
                    val response = api.createNote(noteDto)
                    if (response.isSuccessful) {
                        val syncedNote = response.body()
                        if (syncedNote != null) {
                            // Insertar la nota sincronizada y marcarla como sincronizada
                            noteDao.insertNote(NoteMapper.fromDto(syncedNote).copy(isSynced = true))

                            // Agregar la nota al usuario en el servidor
                            apiUser.addNoteToUser(syncedNote.owner_id!!, syncedNote.id!!)

                            // Eliminar la nota local después de sincronizarla si no estaba marcada como eliminada
                            noteDao.deleteNoteById(noteEntity.id)
                        }
                    }
                }
            } catch (e: Exception) {
                // Si hay un error, no pasa nada, volverá a intentar en el próximo ciclo
            }
        }
    }

}


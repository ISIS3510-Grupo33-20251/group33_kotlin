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
            val response = api.deleteNote(id)
            if (response.isSuccessful) {
                noteDao.deleteNoteById(id)
            }
            return response
        } catch (e: Exception) {
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
                    val response = api.deleteNote(noteEntity.id)
                    if (response.isSuccessful) {
                        noteDao.deleteNoteById(noteEntity.id)
                    }
                } else {
                    val response = api.createNote(noteDto)
                    if (response.isSuccessful) {
                        val syncedNote = response.body()
                        if (syncedNote != null) {
                            noteDao.insertNote(NoteMapper.fromDto(syncedNote).copy(isSynced = true))
                            apiUser.addNoteToUser(syncedNote.owner_id!!, syncedNote.id!!)
                            noteDao.deleteNoteById(noteEntity.id)
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignorar errores para reintentar en otro ciclo
            }
        }
    }


    suspend fun getCachedNotes(userId: String): List<NoteDto> {
        return noteDao.getNotesByUser(userId).map { NoteMapper.toDto(it) }
    }

    suspend fun cacheNotes(notes: List<NoteDto>) {
        notes.forEach {
            val entity = NoteMapper.fromDto(it).copy(isSynced = true, deleted = false)
            noteDao.insertNote(entity)
        }
    }
}
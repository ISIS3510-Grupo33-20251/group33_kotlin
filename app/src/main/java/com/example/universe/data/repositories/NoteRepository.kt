package com.example.universe.data.repositories

import com.example.universe.data.api.NoteApiService
import com.example.universe.data.models.NoteDto
import retrofit2.Response
import javax.inject.Inject


class NoteRepository @Inject constructor(
    private val api: NoteApiService) {

    suspend fun getNotes(): Response<List<NoteDto>> {
        return api.getNotes()
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
}

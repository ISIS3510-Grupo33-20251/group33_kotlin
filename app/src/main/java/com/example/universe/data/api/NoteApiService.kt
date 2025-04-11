package com.example.universe.data.api

import com.example.universe.data.models.NoteDto
import retrofit2.Response
import retrofit2.http.*

interface NoteApiService {

    @GET("notes")
    suspend fun getNotes(): Response<List<NoteDto>>  // Obtener todas las notas

    @GET("notes/{id}")
    suspend fun getNoteById(@Path("id") id: String): Response<NoteDto>  // Obtener una nota por ID

    @POST("notes")
    suspend fun createNote(@Body note: NoteDto): Response<NoteDto>  // Crear una nota

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body note: NoteDto): Response<NoteDto>  // Actualizar nota

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<Unit>  // Eliminar nota

}

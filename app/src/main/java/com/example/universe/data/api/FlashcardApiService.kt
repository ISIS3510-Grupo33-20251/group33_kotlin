package com.example.universe.data.api

import com.example.universe.data.models.Flashcard
import retrofit2.http.GET
import retrofit2.http.Path

interface FlashcardApiService {
    @GET("users/{userId}/{subject}/flash")
    suspend fun getFlashcards(
        @Path("userId") userId: String,
        @Path("subject", encoded = true) subject: String
    ): List<Flashcard>
}
package com.example.universe.data.api
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface FlashcardApiService {
    @GET("flashcard_decks/{user_id}/{subject}/flash")
    fun getFlashcards(
        @Path("user_id") userId: String,
        @Path("subject") subject: String
    ): Call<List<Flashcard>>
}
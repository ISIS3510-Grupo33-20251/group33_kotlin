package com.example.universe.data.api

import com.example.universe.data.models.ReminderDto
import retrofit2.Response
import retrofit2.http.*

interface ReminderApiService {

    @GET("users/{user_id}/reminders")
    suspend fun getUserReminders(
        @Header("Authorization") token: String,
        @Path("user_id") userId: String
    ): Response<List<ReminderDto>>

    @GET("reminders/{id}")
    suspend fun getReminderById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ReminderDto>

    @POST("reminders")
    suspend fun createReminder(
        @Header("Authorization") token: String,
        @Body reminder: ReminderDto
    ): Response<ReminderDto>

    @PUT("reminders/{id}")
    suspend fun updateReminder(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body reminder: ReminderDto
    ): Response<ReminderDto>

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
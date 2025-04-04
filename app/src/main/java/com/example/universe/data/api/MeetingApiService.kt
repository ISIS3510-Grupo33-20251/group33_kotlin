package com.example.universe.data.api

import com.example.universe.data.models.MeetingRequest
import com.example.universe.data.models.MeetingResponse
import retrofit2.http.*

interface MeetingApiService {
    @POST("meetings")
    suspend fun createMeeting(
        @Header("Authorization") token: String,
        @Body meeting: MeetingRequest
    ): MeetingResponse

    @GET("meetings")
    suspend fun getMeetings(
        @Header("Authorization") token: String
    ): List<MeetingResponse>

    @GET("meetings/{id}")
    suspend fun getMeetingById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): MeetingResponse

    @DELETE("meetings/{id}")
    suspend fun deleteMeeting(
        @Header("Authorization") token: String,
        @Path("id") id: String
    )
}




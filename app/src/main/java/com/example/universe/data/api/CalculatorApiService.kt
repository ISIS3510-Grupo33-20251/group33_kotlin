package com.example.universe.data.api

import com.example.universe.data.models.CalculatorSubjectDto
import retrofit2.http.*

interface CalculatorApiService {

    @POST("calculator/")
    suspend fun createSubject(@Body subject: CalculatorSubjectDto): CalculatorSubjectDto

    @GET("calculator/user/{owner_id}")
    suspend fun getSubjectsByUser(@Path("owner_id") ownerId: String): List<CalculatorSubjectDto>

    @GET("calculator/{subject_id}")
    suspend fun getSubject(@Path("subject_id") subjectId: String): CalculatorSubjectDto

    @PUT("calculator/{subject_id}")
    suspend fun updateSubject(
        @Path("subject_id") subjectId: String,
        @Body updatedSubject: CalculatorSubjectDto
    ): Map<String, String> // Expects: { "message": "Subject updated successfully" }

    @DELETE("calculator/{subject_id}")
    suspend fun deleteSubject(@Path("subject_id") subjectId: String): Map<String, String>
}
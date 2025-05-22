package com.example.universe.data.repositories

import com.example.universe.data.api.CalculatorApiService
import com.example.universe.data.models.CalculatorSubjectDto
import javax.inject.Inject

class CalculatorRepository @Inject constructor(
    private val api: CalculatorApiService
) {

    suspend fun createSubject(subject: CalculatorSubjectDto): CalculatorSubjectDto {
        return api.createSubject(subject)
    }

    suspend fun getSubjectsByUser(ownerId: String): List<CalculatorSubjectDto> {
        return api.getSubjectsByUser(ownerId)
    }

    suspend fun getSubject(subjectId: String): CalculatorSubjectDto {
        return api.getSubject(subjectId)
    }

    suspend fun updateSubject(subjectId: String, subject: CalculatorSubjectDto): String {
        return api.updateSubject(subjectId, subject)["message"] ?: "No message"
    }

    suspend fun deleteSubject(subjectId: String): String {
        return api.deleteSubject(subjectId)["message"] ?: "No message"
    }
}
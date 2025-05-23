package com.example.universe.data.repositories

import com.example.universe.data.api.CalculatorApiService
import com.example.universe.data.db.dao.SubjectDao
import com.example.universe.data.db.entity.SubjectEntity
import com.example.universe.data.models.CalculatorSubjectDto
import com.example.universe.data.models.GradeEntryDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import javax.inject.Inject

class CalculatorRepository @Inject constructor(
    private val api: CalculatorApiService,
    private val subjectDao: SubjectDao,
    private val gson: Gson
) {

    suspend fun createSubject(subject: CalculatorSubjectDto): CalculatorSubjectDto {
        val subjectWithId = if (subject._id != null) subject else subject.copy(_id = UUID.randomUUID().toString())

        try {
            // Intentar enviar al backend
            val createdSubject = api.createSubject(subjectWithId)
            // Guardar en local si fue exitoso
            subjectDao.insertSubject(dtoToEntity(createdSubject))
            return createdSubject
        } catch (e: Exception) {
            // Fallback: guardar localmente para sincronizar luego
            subjectDao.insertSubject(dtoToEntity(subjectWithId))
            return subjectWithId
        }
    }


    suspend fun getSubjectsByUser(ownerId: String): List<CalculatorSubjectDto> {
        return try {
            val remoteSubjects = api.getSubjectsByUser(ownerId)
            remoteSubjects.forEach { subjectDao.insertSubject(dtoToEntity(it)) }
            remoteSubjects
        } catch (e: Exception) {
            // fallback local
            subjectDao.getSubjectsByUser(ownerId).map { entityToDto(it) }
        }
    }


    suspend fun getSubject(subjectId: String): CalculatorSubjectDto {
        return api.getSubject(subjectId)
    }

    suspend fun updateSubject(subjectId: String, updatedSubject: CalculatorSubjectDto): String {
        val subjectWithId = updatedSubject.copy(_id = subjectId)

        try {
            // Intentar actualizar remotamente
            val response = api.updateSubject(subjectId, subjectWithId)
            // Actualizar también en base local
            subjectDao.insertSubject(dtoToEntity(subjectWithId))
            return response["message"] ?: "Updated remotely"
        } catch (e: Exception) {
            // Fallback: actualizar solo en local
            subjectDao.insertSubject(dtoToEntity(subjectWithId))
            return "Updated locally (pending sync)"
        }
    }


    suspend fun deleteSubject(subjectId: String): String {
        val localSubject = subjectDao.getSubject(subjectId)

        try {
            // Intentar eliminar remotamente
            val response = api.deleteSubject(subjectId)
            // También eliminar localmente
            localSubject?.let { subjectDao.deleteSubject(it) }
            return response["message"] ?: "Deleted remotely"
        } catch (e: Exception) {
            // Fallback: eliminar solo local
            localSubject?.let { subjectDao.deleteSubject(it) }
            return "Deleted locally (pending sync)"
        }
    }


    private fun dtoToEntity(dto: CalculatorSubjectDto): SubjectEntity =
        SubjectEntity(
            id = dto._id ?: UUID.randomUUID().toString(),
            subjectName = dto.subject_name,
            ownerId = dto.owner_id,
            entriesJson = gson.toJson(dto.entries),
            createdDate = dto.created_date,
            lastModified = dto.last_modified
        )

    private fun entityToDto(entity: SubjectEntity): CalculatorSubjectDto =
        CalculatorSubjectDto(
            _id = entity.id,
            subject_name = entity.subjectName,
            owner_id = entity.ownerId,
            entries = gson.fromJson(entity.entriesJson, object : TypeToken<List<GradeEntryDto>>() {}.type),
            created_date = entity.createdDate,
            last_modified = entity.lastModified
        )

}
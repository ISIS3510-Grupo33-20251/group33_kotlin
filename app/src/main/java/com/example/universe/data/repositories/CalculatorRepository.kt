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
        val subjectWithoutId = updatedSubject.copy(_id = null) // 👈 eliminar ID antes de enviar

        try {
            val response = api.updateSubject(subjectId, subjectWithoutId)
            subjectDao.insertSubject(dtoToEntity(updatedSubject.copy(_id = subjectId)))
            return response["message"] ?: "Updated remotely"
        } catch (e: Exception) {
            subjectDao.insertSubject(
                dtoToEntity(
                    updatedSubject.copy(_id = subjectId),
                    isSynced = false
                )
            )
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


    private fun dtoToEntity(dto: CalculatorSubjectDto, isSynced: Boolean = false, deleted: Boolean = false): SubjectEntity =
        SubjectEntity(
            id = dto._id ?: UUID.randomUUID().toString(),
            subjectName = dto.subject_name,
            ownerId = dto.owner_id,
            entriesJson = gson.toJson(dto.entries),
            createdDate = dto.created_date,
            lastModified = dto.last_modified,
            isSynced = isSynced,
            deleted = deleted
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

    suspend fun syncPendingSubjects() {
        val pendingSubjects = subjectDao.getPendingSubjects()

        for (entity in pendingSubjects) {
            try {
                val dto = entityToDto(entity)

                if (entity.deleted) {
                    // Intentar eliminar remotamente
                    api.deleteSubject(entity.id)
                    // Eliminar localmente
                    subjectDao.deleteSubjectById(entity.id)
                } else {
                    // Verificar si el subject ya existe en el backend
                    val existsOnServer = try {
                        api.getSubject(entity.id)
                        true
                    } catch (e: Exception) {
                        false
                    }

                    if (existsOnServer) {
                        // Actualizar en el backend
                        api.updateSubject(entity.id, dto.copy(_id = null))

                        // Marcar como sincronizado localmente
                        val syncedEntity = entity.copy(isSynced = true, deleted = false)
                        subjectDao.insertSubject(syncedEntity)
                    } else {
                        // Crear en el backend
                        val createdDto = api.createSubject(dto)

                        if (createdDto._id != null && createdDto._id != entity.id) {
                            // Si el backend generó un ID nuevo, borrar el viejo local y guardar el nuevo
                            subjectDao.deleteSubjectById(entity.id)
                            subjectDao.insertSubject(dtoToEntity(createdDto, isSynced = true, deleted = false))
                        } else {
                            // Si el backend respetó el ID, solo marcar como sincronizado
                            val syncedEntity = entity.copy(isSynced = true, deleted = false)
                            subjectDao.insertSubject(syncedEntity)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignorar errores para reintentar luego
            }
        }
    }

}
package com.example.universe.data.mappers

import com.example.universe.data.db.entity.NoteEntity
import com.example.universe.data.models.NoteDto

object NoteMapper {
    fun fromDto(dto: NoteDto): NoteEntity = NoteEntity(
        id = dto.id ?: "", // fallback en caso de que no venga el ID
        title = dto.title,
        subject = dto.subject,
        content = dto.content,
        tags = dto.tags.joinToString(","),
        created_date = dto.created_date,
        last_modified = dto.last_modified,
        owner_id = dto.owner_id
    )

    fun toDto(entity: NoteEntity): NoteDto = NoteDto(
        id = entity.id,
        title = entity.title,
        subject = entity.subject,
        content = entity.content,
        tags = entity.tags.split(",").filter { it.isNotBlank() },
        created_date = entity.created_date,
        last_modified = entity.last_modified,
        owner_id = entity.owner_id
    )
}

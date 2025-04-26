package com.example.universe.data.mappers

import com.example.universe.data.db.entity.NoteEntity
import com.example.universe.data.models.NoteDto
import java.util.UUID

fun NoteEntity.toDto(): NoteDto {
    return NoteDto(
        id = this.id,
        title = this.title,
        subject = this.subject,
        content = this.content,
        tags = this.tags.split(","),
        created_date = this.created_date,
        last_modified = this.last_modified,
        owner_id = this.owner_id,
    )
}

fun NoteDto.toEntity(needsSync: Boolean = false): NoteEntity {
    return NoteEntity(
        id = this.id ?: UUID.randomUUID().toString(), // importa java.util.UUID
        title = this.title,
        subject = this.subject,
        content = this.content,
        tags = this.tags.joinToString(","),
        created_date = this.created_date,
        last_modified = this.last_modified,
        owner_id = this.owner_id,
        needsSync = needsSync
    )
}

package com.example.universe.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.universe.data.db.entity.NoteEntity

@Dao
interface NoteDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getPendingNotes(): List<NoteEntity>

    @Query("UPDATE notes SET isSynced = 1 WHERE id = :noteId")
    suspend fun markAsSynced(noteId: String)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE owner_id = :userId AND deleted = 0")
    suspend fun getNotesByUser(userId: String): List<NoteEntity>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: String)
}

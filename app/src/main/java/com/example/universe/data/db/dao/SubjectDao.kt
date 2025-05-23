package com.example.universe.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.universe.data.db.entity.SubjectEntity

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects WHERE ownerId = :ownerId")
    suspend fun getSubjectsByUser(ownerId: String): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    suspend fun getSubject(subjectId: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("SELECT * FROM subjects WHERE isSynced = 0")
    suspend fun getPendingSubjects(): List<SubjectEntity>

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: String)

}

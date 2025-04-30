package com.example.universe.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.universe.data.db.entity.MeetingEntity

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meeting WHERE dateKey = :dateKey AND participants LIKE '%' || :userId || '%'")
    suspend fun getMeetingsForDate(dateKey: String, userId: String): List<MeetingEntity>

    @Query("SELECT * FROM meeting")
    suspend fun getAllMeetings(): List<MeetingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meetings: List<MeetingEntity>)

    @Delete
    suspend fun delete(meeting: MeetingEntity)

    @Query("DELETE FROM meeting WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM meeting")
    suspend fun clearMeetings()
}
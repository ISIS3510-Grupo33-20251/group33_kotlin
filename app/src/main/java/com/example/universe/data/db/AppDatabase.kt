package com.example.universe.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.universe.data.db.dao.FriendDao
import com.example.universe.data.db.dao.MeetingDao
import com.example.universe.data.db.dao.NoteDao
import com.example.universe.data.db.entity.FriendEntity
import com.example.universe.data.db.entity.MeetingEntity
import com.example.universe.data.db.entity.NoteEntity

@Database(
    entities = [MeetingEntity::class, FriendEntity::class, NoteEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
    abstract fun friendDao(): FriendDao

    suspend fun clearAllTablesData() {
        meetingDao().clearMeetings()
        friendDao().deleteAll()
    }
    abstract fun noteDao(): NoteDao
}
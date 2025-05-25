package com.example.universe.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.universe.data.db.dao.FlashcardDao
import com.example.universe.data.db.dao.FriendDao
import com.example.universe.data.db.dao.MeetingDao
import com.example.universe.data.db.dao.NoteDao
import com.example.universe.data.db.dao.ReminderDao
import com.example.universe.data.db.dao.SubjectDao
import com.example.universe.data.db.entity.FlashcardEntity
import com.example.universe.data.db.entity.FriendEntity
import com.example.universe.data.db.entity.MeetingEntity
import com.example.universe.data.db.entity.NoteEntity
import com.example.universe.data.db.entity.ReminderEntity
import com.example.universe.data.db.entity.SubjectEntity

@Database(

    entities = [MeetingEntity::class, FriendEntity::class, NoteEntity::class, FlashcardEntity::class, ReminderEntity::class, SubjectEntity::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
    abstract fun friendDao(): FriendDao
    abstract fun noteDao(): NoteDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun reminderDao(): ReminderDao
    abstract fun subjectDao(): SubjectDao

    suspend fun clearAllTablesData() {
        meetingDao().clearMeetings()
        friendDao().deleteAll()
    }
}
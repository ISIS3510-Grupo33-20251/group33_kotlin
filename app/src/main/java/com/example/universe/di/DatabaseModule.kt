package com.example.universe.di

import android.content.Context
import androidx.room.Room
import com.example.universe.data.db.AppDatabase
import com.example.universe.data.db.dao.FlashcardDao
import com.example.universe.data.db.dao.FriendDao
import com.example.universe.data.db.dao.MeetingDao
import com.example.universe.data.db.dao.NoteDao
import com.example.universe.data.db.dao.ReminderDao
import com.example.universe.data.db.dao.SubjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "universe_database"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries() // Blocks the main thread, not for production
            .build()
    }

    @Provides
    fun provideMeetingDao(database: AppDatabase): MeetingDao {
        return database.meetingDao()
    }

    @Provides
    fun provideFriendDao(database: AppDatabase): FriendDao {
        return database.friendDao()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideFlashcardDao(database: AppDatabase): FlashcardDao {
        return database.flashcardDao()
    }

    @Provides
    fun provideReminderDao(database: AppDatabase): ReminderDao {
        return database.reminderDao()
    }

    @Provides
    fun provideSubjectDao(database: AppDatabase): SubjectDao {
        return database.subjectDao()
    }
}
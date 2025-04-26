package com.example.universe.di

import com.example.universe.data.repositories.AuthRepositoryImpl
import com.example.universe.data.repositories.FriendLocationRepositoryImpl
import com.example.universe.data.repositories.FriendRepositoryImpl
import com.example.universe.data.repositories.LocationRepositoryImpl
import com.example.universe.data.repositories.MeetingRepositoryImpl
import com.example.universe.data.repositories.ScheduleRepositoryImpl
import com.example.universe.domain.repositories.AuthRepository
import com.example.universe.domain.repositories.FriendLocationRepository
import com.example.universe.domain.repositories.FriendRepository
import com.example.universe.domain.repositories.LocationRepository
import com.example.universe.domain.repositories.MeetingRepository
import com.example.universe.domain.repositories.ScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindFriendRepository(
        friendRepositoryImpl: FriendRepositoryImpl
    ): FriendRepository

    @Binds
    @Singleton
    abstract fun bindMeetingRepository(
        meetingRepositoryImpl: MeetingRepositoryImpl
    ): MeetingRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        scheduleRepositoryImpl: ScheduleRepositoryImpl
    ): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindFriendLocationRepository(
        friendLocationRepositoryImpl: FriendLocationRepositoryImpl
    ): FriendLocationRepository

}
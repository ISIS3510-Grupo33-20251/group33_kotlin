package com.example.universe.di

import com.example.universe.data.repositories.AuthRepositoryImpl
import com.example.universe.data.repositories.FriendRepositoryImpl
import com.example.universe.data.repositories.LocationRepositoryImpl
import com.example.universe.domain.repositories.AuthRepository
import com.example.universe.domain.repositories.FriendRepository
import com.example.universe.domain.repositories.LocationRepository
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
}
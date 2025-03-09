package com.example.universe.domain.repositories

import com.example.universe.domain.models.LoginCredentials
import com.example.universe.domain.models.RegisterCredentials
import com.example.universe.domain.models.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): Result<User>
    suspend fun register(credentials: RegisterCredentials): Result<User>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
    fun getCurrentUser(): Flow<User?>
    fun getAuthToken(): String?
}
package com.example.universe.data.repositories

import android.content.SharedPreferences
import com.example.universe.data.api.AuthApiService
import com.example.universe.data.models.LoginRequestDto
import com.example.universe.data.models.RegisterRequestDto
import com.example.universe.domain.models.LoginCredentials
import com.example.universe.domain.models.RegisterCredentials
import com.example.universe.domain.models.User
import com.example.universe.domain.repositories.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : AuthRepository {

    companion object {
        private const val KEY_USER = "user"
        private const val KEY_TOKEN = "token"
    }

    override suspend fun login(credentials: LoginCredentials): Result<User> {
        return try {
            val request = LoginRequestDto(credentials.email, credentials.password)
            val response = authApiService.login(request)

            // Save token and user info
            sharedPreferences.edit()
                .putString(KEY_TOKEN, response.token)
                .putString(KEY_USER, gson.toJson(
                    User(response.userId, response.email, response.name)
                ))
                .apply()

            Result.success(User(response.userId, response.email, response.name))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(credentials: RegisterCredentials): Result<User> {
        return try {
            val request = RegisterRequestDto(
                credentials.email,
                credentials.name,
                credentials.password
            )
            val response = authApiService.register(request)

            // Save token and user info
            sharedPreferences.edit()
                .putString(KEY_TOKEN, response.token)
                .putString(KEY_USER, gson.toJson(
                    User(response.userId, response.email, response.name)
                ))
                .apply()

            Result.success(User(response.userId, response.email, response.name))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sharedPreferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER)
            .apply()
    }

    override fun isUserLoggedIn(): Boolean {
        return sharedPreferences.contains(KEY_TOKEN)
    }

    override fun getCurrentUser(): Flow<User?> = flow {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        if (userJson != null) {
            try {
                emit(gson.fromJson(userJson, User::class.java))
            } catch (e: Exception) {
                emit(null)
            }
        } else {
            emit(null)
        }
    }

    override fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }
}
package com.example.universe.data.api

import com.example.universe.data.models.AuthResponseDto
import com.example.universe.data.models.LoginRequestDto
import com.example.universe.data.models.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto
}
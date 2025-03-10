package com.example.universe.data.models

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val token: String,
    val userId: String,
    val email: String,
    val name: String
)
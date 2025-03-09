package com.example.universe.domain.models

data class LoginCredentials(
    val email: String,
    val password: String
)

data class RegisterCredentials(
    val email: String,
    val name: String,
    val password: String
)
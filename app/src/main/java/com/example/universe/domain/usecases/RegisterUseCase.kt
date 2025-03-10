package com.example.universe.domain.usecases

import com.example.universe.domain.models.RegisterCredentials
import com.example.universe.domain.models.User
import com.example.universe.domain.repositories.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        return authRepository.register(RegisterCredentials(name, email, password))
    }
}
package com.example.universe.domain.usecases

import com.example.universe.domain.models.LoginCredentials
import com.example.universe.domain.models.User
import com.example.universe.domain.repositories.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.login(LoginCredentials(email, password))
    }
}
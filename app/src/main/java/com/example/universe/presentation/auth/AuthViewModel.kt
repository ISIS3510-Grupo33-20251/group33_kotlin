package com.example.universe.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.domain.models.User
import com.example.universe.domain.repositories.NetworkConnectivityObserver
import com.example.universe.domain.repositories.NetworkStatus
import com.example.universe.domain.usecases.GetCurrentUserUseCase
import com.example.universe.domain.usecases.IsUserLoggedInUseCase
import com.example.universe.domain.usecases.LoginUseCase
import com.example.universe.domain.usecases.LogoutUseCase
import com.example.universe.domain.usecases.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val newtworkConnectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _networkState = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)
    val networkState: StateFlow<NetworkStatus> = _networkState.asStateFlow()

    private val _offlineMode = MutableStateFlow(false)
    val offlineMode: StateFlow<Boolean> = _offlineMode.asStateFlow()

    init {
        checkAuthStatus()

        viewModelScope.launch {
            newtworkConnectivityObserver.observe().collect { status ->
                _networkState.value = status
                _offlineMode.value = (status == NetworkStatus.Lost || status == NetworkStatus.Unavailable)

                if (_offlineMode.value) {
                    // Reset login/register states if offline
                    if (_loginState.value is LoginState.Loading) {
                        _loginState.value = LoginState.Error("Cannot log in while offline")
                    }

                    if (_registerState.value is RegisterState.Loading) {
                        _registerState.value = RegisterState.Error("Cannot register while offline")
                    }
                }
            }
        }
    }

    private fun checkAuthStatus() {
        if (isUserLoggedInUseCase()) {
            getCurrentUserUseCase().onEach { user ->
                if (user != null) {
                    _authState.value = AuthState.Authenticated(user)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }.launchIn(viewModelScope)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        if (_offlineMode.value) {
            _loginState.value = LoginState.Error("Cannot log in while offline. Please check your internet connection and restart the app.")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            loginUseCase(email, password)
                .onSuccess { user ->
                    _loginState.value = LoginState.Success
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (_offlineMode.value) {
            _registerState.value = RegisterState.Error("Cannot register while offline")
            return
        }

        _registerState.value = RegisterState.Loading
        Log.d("AuthViewModel", "Attempting to register with email: $email, name: $name")

        viewModelScope.launch {
            registerUseCase(name, email, password)
                .onSuccess { user ->
                    _registerState.value = RegisterState.Success
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    _registerState.value = RegisterState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Initial
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Initial
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
}

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Initial : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}
package com.example.universe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.universe.presentation.auth.AuthState
import com.example.universe.presentation.auth.AuthViewModel
import com.example.universe.presentation.auth.LoginScreen
import com.example.universe.presentation.auth.RegisterScreen
import com.example.universe.ui.theme.UniverseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniverseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val authState by authViewModel.authState.collectAsState()

                    AppNavHost(
                        navController = navController,
                        authState = authState,
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    authState: AuthState,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthState.Authenticated -> "home"
            else -> "login"
        }
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onRegisterSuccess = { navController.navigate("home") { popUpTo("register") { inclusive = true } } }
            )
        }
        composable("home") {
            // We'll implement this screen later
            // For now, we'll just show a placeholder
            HomeScreen(onLogout = onLogout)
        }
    }
}

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    // Simple placeholder for the home screen
    // We'll implement this in detail later
    androidx.compose.material.Button(onClick = onLogout) {
        androidx.compose.material.Text("Logout")
    }
}
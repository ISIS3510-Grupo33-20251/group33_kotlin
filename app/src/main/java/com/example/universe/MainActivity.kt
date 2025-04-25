package com.example.universe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.example.universe.presentation.WelcomeScreen
import com.example.universe.presentation.friends.FriendsScreen
import com.example.universe.presentation.home.HomeScreen
import com.example.universe.presentation.location.LocationViewModel
import com.example.universe.ui.theme.UniverseTheme
import com.example.universe.presentation.assignments.AssignmentsScreen
import com.example.universe.presentation.assignments.FlashcardDetailScreen
import com.example.universe.presentation.assignments.FlashcardsScreen
import com.example.universe.presentation.assignments.NoteViewModel
import com.example.universe.utils.NetworkUtils
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
                    val locationViewModel: LocationViewModel = hiltViewModel()
                    val authState by authViewModel.authState.collectAsState()
                    val noteViewModel: NoteViewModel = hiltViewModel()

                    LaunchedEffect(Unit) {
                        NetworkUtils.observeNetwork(applicationContext).collect { isConnected ->
                            noteViewModel.syncNotesIfConnected(isConnected)
                        }
                    }
                    // Start location updates if user is authenticated
                    LaunchedEffect(authState) {
                        if (authState is AuthState.Authenticated) {
                            locationViewModel.startLocationUpdates()

                        }
                    }

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
            else -> "welcome"
        }
    ) {
        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("home") { popUpTo("welcome") { inclusive = true } } }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onRegisterSuccess = { navController.navigate("home") { popUpTo("welcome") { inclusive = true } } }
            )
        }

        composable("home") {
            HomeScreen(
                onFriendsClick = { navController.navigate("friends") },
                onLogoutClick = {
                    onLogout()
                    navController.navigate("welcome") { popUpTo("home") { inclusive = true } }
                },
                onRemindersClick = { navController.navigate("reminders") },
                onScheduleClick = { /* Already on schedule */ },
                onAssignmentsClick = { navController.navigate("assignments") }
            )
        }

        composable("friends") {
            FriendsScreen(
                onBackClick = { navController.navigateUp() },
                onSearchClick = { /* Handle search action */ },
                onRemindersClick = { navController.navigate("reminders") },
                onScheduleClick = { navController.navigate("home") },
                onAssignmentsClick = { navController.navigate("assignments") }
            )
        }

        composable("reminders") {
            // Reminder screen will go here
            // For now, just show a text indicating this is the reminders screen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Reminders Screen")
            }
        }

        composable("assignments") {
            AssignmentsScreen(
                onRemindersClick = { navController.navigate("reminders") },
                onScheduleClick = { navController.navigate("home") },
                onAssignmentsClick = { navController.navigate("assignments") },
                onFlashcardsClick = { navController.navigate("flashcards") }
            )
        }

        composable("flashcards") {
            FlashcardsScreen(
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }

        composable("flashcard_detail") {
            FlashcardDetailScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }



    }
}
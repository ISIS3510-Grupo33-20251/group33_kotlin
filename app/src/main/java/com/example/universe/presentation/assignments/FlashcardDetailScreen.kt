package com.example.universe.presentation.assignments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay


@Composable
fun FlashcardDetailScreen(
    navController: NavHostController,
    flashcardViewModel: FlashcardViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val userId = savedStateHandle?.get<String>("userId")
    val subject = savedStateHandle?.get<String>("subject")

    val flashcards by flashcardViewModel.flashcards.collectAsState()
    val error by flashcardViewModel.error.collectAsState()

    var loading by remember { mutableStateOf(true) }
    var timeoutReached by remember { mutableStateOf(false) }

    // Inicia fetch y temporizador de timeout
    LaunchedEffect(userId, subject) {
        if (!userId.isNullOrBlank() && !subject.isNullOrBlank()) {
            flashcardViewModel.fetchFlashcards(userId, subject)

            delay(10000L)  // espera 15 segundos
            if (flashcards.isEmpty()) {
                timeoutReached = true
            }
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        when {
            error != null -> {
                Text("Error: $error", color = Color.Red)
            }
            loading && !timeoutReached -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Generating flashcards...", fontSize = 16.sp)
            }
            timeoutReached -> {
                Text(
                    text = "The AI could not find enough information in the note to generate a flashcard.",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
            flashcards.isEmpty() -> {
                Text(
                    text = "No flashcards generated.",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
            else -> {
                LazyColumn {
                    items(flashcards) { card ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Q: ${card.question}", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("A: ${card.answer}")
                            }
                        }
                    }
                }
            }
        }
    }
}

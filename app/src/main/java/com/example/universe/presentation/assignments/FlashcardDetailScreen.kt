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
import androidx.navigation.NavHostController


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

    LaunchedEffect(userId, subject) {
        if (!userId.isNullOrBlank() && !subject.isNullOrBlank()) {
            flashcardViewModel.fetchFlashcards(userId, subject)
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
            flashcards.isEmpty() -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading... Please don't leave yet.", fontSize = 16.sp)
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

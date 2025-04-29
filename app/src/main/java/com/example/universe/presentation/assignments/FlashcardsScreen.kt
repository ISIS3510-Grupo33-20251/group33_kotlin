package com.example.universe.presentation.assignments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.universe.R
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.universe.data.models.Flashcard
import com.example.universe.presentation.auth.AuthState
import com.example.universe.presentation.auth.AuthViewModel
import retrofit2.Response


@Composable
fun FlashcardsScreen(
    onBackClick: () -> Unit,
    navController: NavHostController,
    noteViewModel: NoteViewModel = hiltViewModel(),
) {
    val noteState by noteViewModel.noteState.collectAsState()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val flashcardViewModel: FlashcardViewModel = hiltViewModel()
    val currentUser = (authViewModel.authState.collectAsState().value as? AuthState.Authenticated)?.user
    val userId = currentUser?.id

    LaunchedEffect(Unit) {
        noteViewModel.getNotes(userId!!)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Botón para volver atrás
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Flashcards",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2A)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (noteState) {
                is NoteState.Loading -> {
                    CircularProgressIndicator()
                }
                is NoteState.Success -> {
                    val notes = (noteState as NoteState.Success).notes
                    LazyColumn {
                        items(notes) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        val currentUser = (authState as? AuthState.Authenticated)?.user
                                        val userId = currentUser?.id ?: return@clickable

                                        navController.currentBackStackEntry?.savedStateHandle?.set("userId", userId)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("subject", note.subject)
                                        // navegar
                                        navController.navigate("flashcard_detail")
                                    },
                                elevation = 4.dp,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = note.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.subject,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = note.content,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
                is NoteState.Error -> {
                    Text(
                        text = "Error: ${(noteState as NoteState.Error).message}",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
                else -> {}
            }
        }
    }
}


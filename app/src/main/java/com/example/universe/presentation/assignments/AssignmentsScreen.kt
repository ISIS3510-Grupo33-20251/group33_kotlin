package com.example.universe.presentation.assignments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.universe.R
import com.example.universe.presentation.home.Footer
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.universe.data.models.NoteDto
import java.time.LocalDate
import java.util.Calendar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Star



@Composable
fun AssignmentsScreen(
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    onFlashcardsClick: () -> Unit,
    noteViewModel: NoteViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var noteId by remember { mutableStateOf<String?>(null) }

    val noteState by noteViewModel.noteState.collectAsState()

    LaunchedEffect(Unit) {
        noteViewModel.getNotes()
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
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "My Notes",
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
                                        title = note.title
                                        subject = note.subject
                                        content = note.content
                                        noteId = note.id
                                        isEditing = true
                                        showDialog = true
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

        // Botones flotantes
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = {
                    title = ""
                    subject = ""
                    content = ""
                    isEditing = false
                    noteId = null
                    showDialog = true
                },
                backgroundColor = Color(0xFF1A2340),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 110.dp, end = 24.dp)
                    .size(56.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }

            FloatingActionButton(
                onClick = { onFlashcardsClick() },
                backgroundColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 110.dp, start = 24.dp)
                    .size(56.dp)
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = "Favorite")
            }
        }

        // Diálogo para crear/editar/eliminar nota
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    noteId = null
                },
                title = {
                    Text(
                        text = if (isEditing) "Edit Note" else "New Note",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Content") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            maxLines = 6
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            val day = calendar.get(Calendar.DAY_OF_MONTH)
                            val month = calendar.get(Calendar.MONTH) + 1
                            val year = calendar.get(Calendar.YEAR)

                            val formattedDay = String.format("%02d", day)
                            val formattedMonth = String.format("%02d", month)
                            val currentDate = "$year-$formattedMonth-$formattedDay"

                            val note = NoteDto(
                                title = title,
                                content = content,
                                subject = subject,
                                owner_id = "",
                                created_date = currentDate,
                                last_modified = currentDate
                            )

                            if (isEditing && noteId != null) {
                                noteViewModel.updateNote(noteId!!, note)
                            } else {
                                noteViewModel.createNote(note)
                            }

                            showDialog = false
                            noteId = null
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1A2340))
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    Row {
                        if (isEditing && noteId != null) {
                            OutlinedButton(
                                onClick = {
                                    noteViewModel.deleteNote(noteId!!)
                                    showDialog = false
                                    noteId = null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Text("Delete")
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        OutlinedButton(
                            onClick = {
                                showDialog = false
                                noteId = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }

        // Footer
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Footer(
                selectedScreen = "assignments",
                onRemindersClick = onRemindersClick,
                onScheduleClick = onScheduleClick,
                onAssignmentsClick = onAssignmentsClick
            )
        }
    }
}

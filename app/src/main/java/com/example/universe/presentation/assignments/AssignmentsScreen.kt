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

@Composable
fun AssignmentsScreen(
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }

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

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Título de la pantalla
            Text(
                text = "My Notes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2A)
            )

            // Espacio para contenido (futuras notas)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }

        // Botón flotante "+"
        FloatingActionButton(
            onClick = { showDialog = true },
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

        // Diálogo para agregar una nota
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Column {
                        Text(text = "New Note", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp)) // Padding fijo antes del campo Title
                    }
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
                            placeholder = { Text("Enter the title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject") },
                            placeholder = { Text("Enter the subject") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Content") },
                            placeholder = { Text("Write your note here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp), // Más grande para contenido largo
                            maxLines = 6
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1A2340))
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDialog = false }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Footer (Menú de navegación)
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Footer(
                selectedScreen = "assignments",
                onRemindersClick = onRemindersClick,
                onScheduleClick = onScheduleClick,
                onAssignmentsClick = onAssignmentsClick
            )
        }
    }
}

package com.example.universe.presentation.assignments

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color


import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.universe.data.models.CalculatorSubjectDto
import com.example.universe.data.models.GradeEntryDto
import com.example.universe.presentation.auth.AuthState
import com.example.universe.presentation.auth.AuthViewModel


@Composable
fun CalculatorScreen(navController: NavController, viewModel: CalculatorViewModel = hiltViewModel()) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser = (authViewModel.authState.collectAsState().value as? AuthState.Authenticated)?.user
    val ownerId = currentUser?.id
    val subjects by viewModel.subjects.collectAsState()
    var grades by remember { mutableStateOf(listOf(GradeInput("", "", ""))) }
    var isModo100 by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    var expanded by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }

    // 👇 Cargar materias al ingresar
    LaunchedEffect(Unit) {
        if (ownerId != null) {
            viewModel.loadSubjects(ownerId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            // 🔽 Dropdown menu con materias
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        showAddSubjectDialog = true
                        expanded = false
                    }) {
                        Text("Add Subject")
                    }

                    Divider()

                    // 🔁 Materias como botones
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            onClick = {
                                // En el futuro podrías seleccionar la materia actual
                                println("Seleccionaste: ${subject.subject_name}")
                                expanded = false
                            }
                        ) {
                            Text(subject.subject_name)
                        }
                    }
                }
            }
        }

        // ... 🔁 el resto de tu código continúa sin cambios ...


        Spacer(modifier = Modifier.height(8.dp))

        Text("Grade Calculator", style = MaterialTheme.typography.h6)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text("Mode 0–5")
            Switch(
                checked = isModo100,
                onCheckedChange = { isModo100 = it },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text("Mode 0–100")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            grades.forEachIndexed { index, grade ->
                GradeInputRow(
                    grade = grade,
                    onUpdate = { updatedGrade ->
                        grades = grades.toMutableList().also { it[index] = updatedGrade }
                    },
                    onDelete = {
                        grades = grades.toMutableList().also { it.removeAt(index) }
                    },
                    isModo100 = isModo100
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    grades = grades + GradeInput("", "", "")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1A2340))
            ) {
                Text("Add Grade", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val totalPercentage = grades.sumOf {
                it.percentage.toDoubleOrNull() ?: 0.0
            }

            val weightedTotal = grades.sumOf {
                val rawValue = it.value.replace(',', '.').toDoubleOrNull() ?: 0.0
                val percentage = it.percentage.toDoubleOrNull() ?: 0.0
                val convertedValue = if (isModo100) (rawValue / 100.0) * 5.0 else rawValue
                convertedValue * (percentage / 100.0)
            }

            Text("Total: %.2f%%".format(totalPercentage), fontSize = 18.sp)
            Text("Weighted Grade: %.2f / 5.0".format(weightedTotal), fontSize = 18.sp)

            if (weightedTotal >= 3.0) {
                Text(
                    text = "Congratulations! You're passing! 🎉",
                    color = Color(0xFF4CAF50),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Botón inferior izquierdo "Delete Subject" (no funcional aún)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = { /* lógica futura */ },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFB71C1C))
            ) {
                Text("Delete Subject", color = Color.White)
            }
        }
    }

    // Diálogo para agregar nueva materia
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddSubjectDialog = false
                newSubjectName = ""
            },
            title = { Text("Add New Subject") },
            text = {
                OutlinedTextField(
                    value = newSubjectName,
                    onValueChange = { newSubjectName = it },
                    label = { Text("Subject Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (ownerId != null && newSubjectName.isNotBlank()) {
                        val entries = grades.mapNotNull { grade ->
                            val percentage = grade.percentage.replace(',', '.').toDoubleOrNull()
                            val value = grade.value.replace(',', '.').toDoubleOrNull()

                            if (percentage != null && value != null) {
                                GradeEntryDto(
                                    name = grade.name,
                                    percentage = percentage,
                                    grade = if (isModo100) (value / 100.0) * 5.0 else value
                                )
                            } else null
                        }

                        val subject = CalculatorSubjectDto(
                            subject_name = newSubjectName,
                            owner_id = ownerId,
                            entries = entries
                        )

                        viewModel.createSubject(subject) {
                            // callback tras creación exitosa
                            viewModel.loadSubjects(ownerId)
                        }

                        // Reset UI
                        showAddSubjectDialog = false
                        newSubjectName = ""
                        grades = listOf(GradeInput("", "", ""))
                    }
                }) {
                    Text("Save")
                }
            }
            ,
            dismissButton = {
                TextButton(onClick = {
                    showAddSubjectDialog = false
                    newSubjectName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun GradeInputRow(
    grade: GradeInput,
    onUpdate: (GradeInput) -> Unit,
    onDelete: () -> Unit,
    isModo100: Boolean
) {
    val percentageValue = grade.percentage.toDoubleOrNull()
    val percentageError = grade.percentage.isNotEmpty() && percentageValue == null
    val percentageOutOfRange = percentageValue != null && (percentageValue < 0.0 || percentageValue > 100.0)

    val valueFormatted = grade.value.replace(',', '.')
    val valueParsed = valueFormatted.toDoubleOrNull()
    val valueError = grade.value.isNotEmpty() && valueParsed == null
    val valueOutOfRange = valueParsed != null && (
            valueParsed < 0.0 ||
                    (isModo100 && valueParsed > 100.0) ||
                    (!isModo100 && valueParsed > 5.0)
            )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = grade.name,
                onValueChange = { onUpdate(grade.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = grade.percentage,
                onValueChange = {
                    val number = it.toDoubleOrNull()
                    if (number == null || number >= 0.0) {
                        onUpdate(grade.copy(percentage = it))
                    }
                },
                label = { Text("%") },
                modifier = Modifier.width(70.dp),
                isError = percentageError || percentageOutOfRange
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = grade.value,
                onValueChange = {
                    val newVal = it.replace(',', '.')
                    val doubleVal = newVal.toDoubleOrNull()
                    val isValid = doubleVal != null &&
                            doubleVal >= 0.0 &&
                            ((isModo100 && doubleVal <= 100.0) || (!isModo100 && doubleVal <= 5.0))

                    if (isValid || it.isEmpty()) {
                        onUpdate(grade.copy(value = it))
                    }
                },
                label = { Text(if (isModo100) "Grade (0–100)" else "Grade (0–5)") },
                modifier = Modifier.width(100.dp),
                isError = valueError || valueOutOfRange
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { onDelete() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Grade",
                    tint = Color.Red
                )
            }
        }
        if (percentageError) {
            Text(
                text = "Only numeric values are allowed in %",
                color = MaterialTheme.colors.error,
                fontSize = 12.sp
            )
        } else if (percentageOutOfRange) {
            Text(
                text = "Percentage must be between 0 and 100",
                color = MaterialTheme.colors.error,
                fontSize = 12.sp
            )
        }
        if (valueError) {
            Text(
                text = "Only numeric values are allowed in Grade",
                color = MaterialTheme.colors.error,
                fontSize = 12.sp
            )
        } else if (valueOutOfRange) {
            Text(
                text = if (isModo100) "Grade must be between 0 and 100" else "Grade must be between 0 and 5",
                color = MaterialTheme.colors.error,
                fontSize = 12.sp
            )
        }
    }
}

data class GradeInput(
    val name: String,
    val percentage: String,
    val value: String
)

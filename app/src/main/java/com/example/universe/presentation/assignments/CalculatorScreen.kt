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

    var selectedSubject by remember { mutableStateOf<CalculatorSubjectDto?>(null) }
    var grades by remember { mutableStateOf(listOf(GradeInput("", "", ""))) }
    val scrollState = rememberScrollState()
    var expanded by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }

    // Cargar materias al ingresar
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

            // Dropdown con materias
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
                    // Opción para volver a la base
                    DropdownMenuItem(
                        onClick = {
                            selectedSubject = null
                            grades = listOf(GradeInput("", "", ""))
                            expanded = false
                        }
                    ) {
                        Text("➕ New / Blank")
                    }

                    Divider()

                    // Otras materias
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            onClick = {
                                selectedSubject = subject
                                grades = subject.entries.map {
                                    GradeInput(
                                        name = it.name,
                                        percentage = it.percentage.toString(),
                                        value = String.format("%.2f", it.grade)
                                    )
                                }
                                expanded = false
                            }
                        ) {
                            Text(subject.subject_name)
                        }
                    }
                }

            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Grade Calculator${selectedSubject?.let { ": ${it.subject_name}" } ?: ""}",
            style = MaterialTheme.typography.h6
        )

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
                    }
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
                rawValue * (percentage / 100.0)
            }

            Text("Total: %.2f%%".format(totalPercentage), fontSize = 18.sp)
            Text("Weighted Grade: %.2f".format(weightedTotal), fontSize = 18.sp)

            if (weightedTotal >= 3.0) {
                Text(
                    text = "Congratulations! You're passing! 🎉",
                    color = Color(0xFF4CAF50),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    showAddSubjectDialog = true
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1565C0))
            ) {
                Text("Add Subject", color = Color.White)
            }

            Button(
                onClick = {
                    selectedSubject?._id?.let { id ->
                        ownerId?.let { owner ->
                            viewModel.deleteSubject(id, owner)
                            selectedSubject = null
                            grades = listOf(GradeInput("", "", ""))
                        }
                    }
                },
                enabled = selectedSubject != null,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFB71C1C))
            ) {
                Text("Delete Subject", color = Color.White)
            }

            Button(
                onClick = {
                    selectedSubject?.let { subject ->
                        val updatedEntries = grades.mapNotNull { grade ->
                            val name = grade.name.trim()
                            val percentage = grade.percentage.replace(',', '.').toDoubleOrNull()
                            val value = grade.value.replace(',', '.').toDoubleOrNull()
                            if (name.isNotBlank() && percentage != null && value != null) {
                                GradeEntryDto(
                                    name = name,
                                    percentage = percentage,
                                    grade = value
                                )
                            } else null
                        }

                        if (ownerId != null && subject._id != null) {
                            val cleanedSubject = CalculatorSubjectDto(
                                subject_name = subject.subject_name,
                                owner_id = subject.owner_id,
                                entries = updatedEntries
                            )

                            viewModel.updateSubject(subject._id, cleanedSubject)
                        }
                    }
                },
                enabled = selectedSubject != null,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B5E20))
            ) {
                Text("Save Changes", color = Color.White)
            }
        }
    }

    // Diálogo para nueva materia
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
                                    grade = value
                                )
                            } else null
                        }

                        val subject = CalculatorSubjectDto(
                            subject_name = newSubjectName,
                            owner_id = ownerId,
                            entries = entries
                        )

                        viewModel.createSubject(subject) {
                            viewModel.loadSubjects(ownerId)
                        }

                        showAddSubjectDialog = false
                        newSubjectName = ""
                        grades = listOf(GradeInput("", "", ""))
                    }
                }) {
                    Text("Save")
                }
            },
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
    onDelete: () -> Unit
) {
    val percentageValue = grade.percentage.toDoubleOrNull()
    val percentageError = grade.percentage.isNotEmpty() && percentageValue == null
    val percentageOutOfRange = percentageValue != null && (percentageValue < 0.0 || percentageValue > 100.0)

    val valueFormatted = grade.value.replace(',', '.')
    val valueParsed = valueFormatted.toDoubleOrNull()
    val valueError = grade.value.isNotEmpty() && valueParsed == null
    val valueOutOfRange = valueParsed != null && (valueParsed < 0.0 || valueParsed > 5.0)  // CAMBIO aquí

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
                            doubleVal <= 5.0  // CAMBIO aquí

                    if (isValid || it.isEmpty()) {
                        onUpdate(grade.copy(value = it))
                    }
                },
                label = { Text("Grade (0–5)") }, // CAMBIO aquí
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
        }
        if (percentageOutOfRange) {
            Text(
                text = "Percentage must be between 0 and 100",
                color = MaterialTheme.colors.error,
                fontSize = 12.sp
            )
        }
        if (valueError) {
            Text(
                text = "Only numeric values are allowed in grade",
                color = MaterialTheme.colors.error,
                fontSize = 12.sp
            )
        }
        if (valueOutOfRange) {
            Text(
                text = "Grade must be between 0 and 5",  // CAMBIO aquí
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

package com.example.universe.presentation.reminders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun CreateReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, message: String, dateTime: LocalDateTime) -> Unit,
    isLoading: Boolean = false,
    isOffline: Boolean = false
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now().plusHours(1)) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var dateTimeError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Date picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            dateTimeError = null
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    // Time picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedTime = LocalTime.of(hour, minute)
            dateTimeError = null
        },
        selectedTime.hour,
        selectedTime.minute,
        false
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = 8.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Create Reminder",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1A1A2A)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Offline warning
                if (isOffline) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFFFF3CD),
                        elevation = 0.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFF856404),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "You're offline. Reminder will sync when you're back online.",
                                color = Color(0xFF856404),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = { Text("Title") },
                    singleLine = true,
                    isError = titleError != null,
                    modifier = Modifier.fillMaxWidth()
                )

                titleError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Message field
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Date and Time selection
                Text(
                    text = "When to remind you:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A2A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date selector
                    OutlinedTextField(
                        value = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        onValueChange = {},
                        label = { Text("Date") },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerDialog.show() },
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, "Select date")
                        },
                        isError = dateTimeError != null
                    )

                    // Time selector
                    OutlinedTextField(
                        value = selectedTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                        onValueChange = {},
                        label = { Text("Time") },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { timePickerDialog.show() },
                        trailingIcon = {
                            Icon(Icons.Default.Notifications, "Select time")
                        },
                        isError = dateTimeError != null
                    )
                }

                dateTimeError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            // Validate input
                            when {
                                title.isBlank() -> {
                                    titleError = "Title is required"
                                }
                                LocalDateTime.of(selectedDate, selectedTime).isBefore(LocalDateTime.now()) -> {
                                    dateTimeError = "Reminder time must be in the future"
                                }
                                else -> {
                                    val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                                    onConfirm(title.trim(), message.trim(), dateTime)
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF667EFF)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
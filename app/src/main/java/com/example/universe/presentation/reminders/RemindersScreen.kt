package com.example.universe.presentation.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.universe.domain.models.Reminder
import com.example.universe.domain.models.ReminderStatus
import com.example.universe.presentation.home.Footer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun RemindersScreen(
    onBackClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val offlineMode by viewModel.offlineMode.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    // Handle create success
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            showCreateDialog = false
            viewModel.clearCreateSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Reminders",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            backgroundColor = Color.White,
            elevation = 0.dp,
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    showCreateDialog = true
                    viewModel.clearError()
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Reminder",
                        tint = Color.Black
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Main content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group reminders by status
                val pendingReminders = reminders.filter { it.status == ReminderStatus.PENDING }
                val firedReminders = reminders.filter { it.status == ReminderStatus.FIRED }

                // Pending reminders section
                if (pendingReminders.isNotEmpty()) {
                    item {
                        Text(
                            text = "Upcoming (${pendingReminders.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2A),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(pendingReminders) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onDeleteClick = { showDeleteDialog = reminder.id }
                        )
                    }
                }

                // Fired reminders section
                if (firedReminders.isNotEmpty()) {
                    item {
                        Text(
                            text = "Completed (${firedReminders.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(firedReminders) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onDeleteClick = { showDeleteDialog = reminder.id },
                            isCompleted = true
                        )
                    }
                }

                // Empty state
                if (reminders.isEmpty() && !isLoading) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No reminders yet",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Tap + to create your first reminder",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Error message (like offline mode)
            if (error != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    color = Color(0xFFFFF3CD),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error ?: "",
                            color = Color(0xFF856404),
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }

        // Footer navigation
        Footer(
            selectedScreen = "reminders",
            onRemindersClick = onRemindersClick,
            onScheduleClick = onScheduleClick,
            onAssignmentsClick = onAssignmentsClick
        )
    }

    // Create reminder dialog
    if (showCreateDialog) {
        CreateReminderDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, message, dateTime ->
                viewModel.createReminder(title, message, dateTime)
            },
            isLoading = uiState.isCreating,
            isOffline = offlineMode
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { reminderId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete this reminder?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReminder(reminderId)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onDeleteClick: () -> Unit,
    isCompleted: Boolean = false
) {
    val backgroundColor = if (isCompleted) Color(0xFFF5F5F5) else Color.White
    val textColor = if (isCompleted) Color.Gray else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle item click if needed */ },
        elevation = if (isCompleted) 1.dp else 4.dp,
        backgroundColor = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                reminder.entityId?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (reminder.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.message,
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = textColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatReminderTime(reminder.remindAt),
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun formatReminderTime(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val reminderDate = dateTime.toLocalDate()
    val reminderTime = dateTime.toLocalTime()

    return when {
        reminderDate == now.toLocalDate() -> {
            "Today at ${reminderTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
        }
        reminderDate == now.toLocalDate().plusDays(1) -> {
            "Tomorrow at ${reminderTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
        }
        else -> {
            "${reminderDate.format(DateTimeFormatter.ofPattern("MMM dd"))} at ${reminderTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
        }
    }
}
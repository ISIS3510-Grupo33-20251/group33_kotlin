package com.example.universe.presentation.schedule

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.universe.domain.models.Meeting
import com.example.universe.presentation.home.Footer
import com.example.universe.presentation.home.AddMeetingDialog
import com.example.universe.presentation.meeting.MeetingViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun ScheduleScreen(
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    scheduleViewModel: ScheduleViewModel = hiltViewModel(),
    meetingViewModel: MeetingViewModel = hiltViewModel()
) {
    val selectedDate by scheduleViewModel.selectedDate.collectAsState()
    val formattedDate by scheduleViewModel.formattedDate.collectAsState()
    val weekDates by scheduleViewModel.weekDates.collectAsState()
    val timeSlots by scheduleViewModel.timeSlots.collectAsState()
    val isLoading by scheduleViewModel.isLoading.collectAsState()
    val error by scheduleViewModel.error.collectAsState()
    val offlineMode by scheduleViewModel.offlineMode.collectAsState()
    val initialLoadComplete by scheduleViewModel.initialLoadComplete.collectAsState()

    var showAddMeetingDialog by remember { mutableStateOf(false) }

    // Force a reload when initial load completes
    LaunchedEffect(initialLoadComplete) {
        if (initialLoadComplete) {
            Log.d("ScheduleScreen", "Initial load complete, triggering UI update")
            scheduleViewModel.loadScheduleForSelectedDate(localOnly = offlineMode)
        }
    }

    LaunchedEffect(timeSlots) {
        Log.d("ScheduleScreen", "Time slots updated: ${timeSlots.size} slots")
        timeSlots.forEach { slot ->
            if (slot.meetings.isNotEmpty()) {
                Log.d("ScheduleScreen", "Slot ${slot.time} has ${slot.meetings.size} meetings")
            }
        }
    }
    LaunchedEffect(selectedDate, offlineMode) {
        // Load data whenever the selected date changes or network status changes
        scheduleViewModel.loadScheduleForSelectedDate(
            localOnly = offlineMode,
            showOfflineMessage = offlineMode
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            // Current date display
            Text(
                text = formattedDate,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Week day selector
        WeekDaySelector(
            weekDates = weekDates,
            selectedDate = selectedDate,
            onDateSelected = { scheduleViewModel.selectDate(it) }
        )

        Divider(color = Color.LightGray, thickness = 1.dp)

        // Main content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // First, check if we have any time slots with meetings
            val hasMeetings = timeSlots.any { it.meetings.isNotEmpty() }

            // If we have data, display it even in offline mode
            if (hasMeetings || !isLoading) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(timeSlots) { slot ->
                        TimeSlotItem(
                            timeSlot = slot,
                            onMeetingClick = { /* Handle meeting click */ }
                        )
                    }
                }
            }

            // Show loading indicator when loading
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Show error message (like offline mode)
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

            // FAB for adding a new meeting
            FloatingActionButton(
                onClick = { if (!offlineMode) showAddMeetingDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp),
                backgroundColor = if (offlineMode) Color.Gray else Color.LightGray,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add meeting")
            }
        }

        // Footer navigation
        Footer(
            selectedScreen = "schedule",
            onRemindersClick = onRemindersClick,
            onScheduleClick = onScheduleClick,
            onAssignmentsClick = onAssignmentsClick
        )
    }

    // Meeting dialog
    if (showAddMeetingDialog) {
        AddMeetingDialog(
            onDismiss = { showAddMeetingDialog = false },
            onConfirm = { title, day, startTime, endTime, frequency ->
                meetingViewModel.createMeeting(title, day, startTime, endTime, frequency)
                showAddMeetingDialog = false
                // Refresh the schedule
                scheduleViewModel.loadScheduleForSelectedDate()
            }
        )
    }
}

@Composable
fun WeekDaySelector(
    weekDates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(weekDates) { _, date ->
            DayItem(
                date = date,
                isSelected = date == selectedDate,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun DayItem(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val dayOfMonth = date.dayOfMonth.toString()
    val isToday = date == LocalDate.now()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> Color(0xFF667EFF)
                    isToday -> Color(0xFFE0E0E0)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.dp else 0.dp,
                color = if (isToday && !isSelected) Color(0xFF667EFF) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = dayOfWeek,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dayOfMonth,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TimeSlotItem(
    timeSlot: TimeSlot,
    onMeetingClick: (Meeting) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Time label
        Text(
            text = timeSlot.time,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Meetings at this time slot
        if (timeSlot.meetings.isEmpty()) {
            // Empty slot indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.LightGray)
            )
        } else {
            // Meeting items
            timeSlot.meetings.forEach { meeting ->
                MeetingItem(meeting = meeting, onClick = { onMeetingClick(meeting) })
            }
        }
    }
}

@Composable
fun MeetingItem(
    meeting: Meeting,
    onClick: () -> Unit
) {
    val startTime = formatTime(meeting.startTime)
    val endTime = formatTime(meeting.endTime)
    val duration = "$startTime - $endTime"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = 2.dp,
        backgroundColor = getMeetingColor(meeting.title)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = meeting.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = duration,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// format time from ISO string
private fun formatTime(isoTime: String): String {
    return try {
        val timeString = isoTime.substring(11, 19) // Extract just the time part (HH:MM:SS)
        val time = LocalTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_TIME)
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        time.format(formatter)
    } catch (e: Exception) {
        "N/A"
    }
}

// generate consistent colors based on meeting title
private fun getMeetingColor(title: String): Color {
    // Simple hash function to generate consistent colors
    val hash = title.hashCode()
    val index = Math.abs(hash) % meetingColors.size
    return meetingColors[index]
}

// Predefined colors for meetings
private val meetingColors = listOf(
    Color(0xFFF44336),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF673AB7),
    Color(0xFF3F51B5),
    Color(0xFF2196F3),
    Color(0xFF03A9F4),
    Color(0xFF00BCD4),
    Color(0xFF009688),
    Color(0xFF4CAF50),
    Color(0xFF8BC34A),
    Color(0xFFCDDC39),
    Color(0xFFFFEB3B),
    Color(0xFFFFC107),
    Color(0xFFFF9800),
    Color(0xFFFF5722)
)
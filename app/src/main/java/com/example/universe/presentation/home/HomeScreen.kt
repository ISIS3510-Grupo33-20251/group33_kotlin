package com.example.universe.presentation.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.universe.presentation.location.LocationViewModel
import com.example.universe.presentation.location.rememberLocationPermissionState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.example.universe.domain.models.Meeting
import com.example.universe.presentation.meeting.MeetingState
import com.example.universe.presentation.meeting.MeetingViewModel
import com.example.universe.presentation.schedule.ScheduleViewModel
import com.example.universe.presentation.schedule.ScheduleScreen
import com.example.universe.presentation.schedule.TimeSlot
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun HomeScreen(
    onFriendsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit
) {
    LocationPermissionHandler ({
        var showMenu by remember { mutableStateOf(false) }

        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .alpha(if (showMenu) 0.7f else 1f)
            ) {
                Header(onMenuClick = { showMenu = !showMenu })

                ScheduleScreen(
                    onRemindersClick = onRemindersClick,
                    onScheduleClick = onScheduleClick,
                    onAssignmentsClick = onAssignmentsClick
                )

                Footer(
                    selectedScreen = "home",
                    onRemindersClick = onRemindersClick,
                    onScheduleClick = onScheduleClick,
                    onAssignmentsClick = onAssignmentsClick
                )
            }

            if (showMenu) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .clickable { showMenu = false }
                ) {
                    SideMenu(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 72.dp)
                            .width(200.dp),
                        onFriendsClick = {
                            onFriendsClick()
                            showMenu = false
                        },
                        onShareClick = {
                            // Handle share action
                            showMenu = false
                        },
                        onSettingsClick = {
                            // Navigate to settings
                            showMenu = false
                        },
                        onGuideClick = {
                            // Navigate to guide
                            showMenu = false
                        },
                        onLogoutClick = {
                            onLogoutClick()
                            showMenu = false
                        }
                    )
                }
            }
        }
    })
}

@Composable
fun SideMenu(
    modifier: Modifier = Modifier,
    onFriendsClick: () -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onGuideClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = modifier,
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            MenuItem("Friends", onFriendsClick)
            Divider()
            MenuItem("Share", onShareClick)
            Divider()
            MenuItem("Settings", onSettingsClick)
            Divider()
            MenuItem("Guide", onGuideClick)
            Divider()
            MenuItem("Logout", onLogoutClick)
        }
    }
}

@Composable
fun MenuItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            color = Color.Black
        )
    }
}

@Composable
fun Header(onMenuClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(
            text = "SEMESTER 1",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Schedule",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2A)
            )

            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun Footer(
    selectedScreen: String,
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit
) {
    val navBarColor = Color(0xFF1A2340)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(navBarColor)
    ) {
        // Navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Reminders
            NavigationItem(
                icon = Icons.Outlined.Notifications,
                label = "Reminders",
                onClick = onRemindersClick,
                isSelected = false
            )

            // Schedule
            NavigationItem(
                icon = Icons.Filled.DateRange,
                label = "Schedule",
                onClick = onScheduleClick,
                isSelected = selectedScreen == "home"
            )

            // Assignments
            NavigationItem(
                icon = Icons.Outlined.List,
                label = "Assignments",
                onClick = onAssignmentsClick,
                isSelected = selectedScreen == "assignments"
            )
        }
    }
}

@Composable
fun NavigationItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    val itemColor = if (isSelected) Color.White else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = itemColor
            )
        }

        Text(
            text = label,
            color = itemColor,
            fontSize = 12.sp
        )

        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(24.dp)
                    .height(2.dp)
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun LocationPermissionHandler(
    content: @Composable () -> Unit,
    locationViewModel: LocationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val permissionState = rememberLocationPermissionState()

    LaunchedEffect(permissionState.hasPermission) {
        if (permissionState.hasPermission) {
            Log.d("LocationPermission", "Permission granted, starting location updates")
            locationViewModel.startLocationUpdates()
        }
    }

    if (permissionState.hasPermission) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Location permission is required for this feature")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { permissionState.requestPermission() }) {
                Text("Request permission")
            }
        }
    }
}

@Composable
fun AddMeetingDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, day: String, startTime: String, endTime: String, frequency: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Once") }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val frequencies = listOf("Once", "Daily", "Weekly", "Monthly")
    val timeSlots = (0..23).map { hour ->
        String.format("%02d:00", hour)
    }

    var showDayDropdown by remember { mutableStateOf(false) }
    var showFrequencyDropdown by remember { mutableStateOf(false) }
    var showTimeDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Meeting",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Day selection
                Box {
                    OutlinedTextField(
                        value = selectedDay,
                        onValueChange = {},
                        label = { Text("Day") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDayDropdown = true },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, "Show days")
                        }
                    )

                    DropdownMenu(
                        expanded = showDayDropdown,
                        onDismissRequest = { showDayDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedDay = day
                                    showDayDropdown = false
                                }
                            ) {
                                Text(day)
                            }
                        }
                    }
                }

                // Time selection row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Start time
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("HH:MM") }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // End time
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("HH:MM") }
                    )
                }

                // Frequency selection
                Box {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = {},
                        label = { Text("Frequency") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFrequencyDropdown = true },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, "Show frequencies")
                        }
                    )

                    DropdownMenu(
                        expanded = showFrequencyDropdown,
                        onDismissRequest = { showFrequencyDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        frequencies.forEach { freq ->
                            DropdownMenuItem(
                                onClick = {
                                    frequency = freq
                                    showFrequencyDropdown = false
                                }
                            ) {
                                Text(freq)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedDay.isNotBlank() &&
                        startTime.isNotBlank() && endTime.isNotBlank()) {
                        onConfirm(title, selectedDay, startTime, endTime, frequency)
                    }
                },
                enabled = title.isNotBlank() && selectedDay.isNotBlank() &&
                        startTime.isNotBlank() && endTime.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
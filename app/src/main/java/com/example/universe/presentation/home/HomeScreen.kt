package com.example.universe.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun HomeScreen(
    onFriendsClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .alpha(if (showMenu) 0.7f else 1f)
        ) {
            Header(onMenuClick = { showMenu = !showMenu })

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Footer(
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
                    }
                )
            }
        }
    }
}

@Composable
fun SideMenu(
    modifier: Modifier = Modifier,
    onFriendsClick: () -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onGuideClick: () -> Unit
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
                isSelected = true
            )

            // Assignments
            NavigationItem(
                icon = Icons.Outlined.List,
                label = "Assignments",
                onClick = onAssignmentsClick,
                isSelected = false
            )
        }
    }
}

@Composable
fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
// app/src/main/java/com/example/universe/presentation/home/HomeScreen.kt
package com.example.universe.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Header(onMenuClick)

        // Content area - Empty for now as requested
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

        // Title row with menu button
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
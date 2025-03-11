package com.example.universe.presentation.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.universe.presentation.home.Footer

@Composable
fun FriendsScreen(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit
) {
    val friends = listOf(
        "Alejandro",
        "Felipe",
        "Alejandra",
        "Pablo",
        "Nicolas",
        "Juana"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Friends",
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
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Black
                    )
                }
            }
        )

        LazyColumn {
            items(friends) { friend ->
                FriendItem(name = friend)
                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(start = 72.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Footer(
            onRemindersClick = onRemindersClick,
            onScheduleClick = onScheduleClick,
            onAssignmentsClick = onAssignmentsClick
        )
    }
}

@Composable
fun FriendItem(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE6E6FA)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.first().toString(),
                color = Color(0xFF673AB7),
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
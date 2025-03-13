package com.example.universe.presentation.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.universe.domain.models.FriendRequest
import com.example.universe.domain.models.User
import com.example.universe.presentation.home.Footer

@Composable
fun FriendsScreen(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val friendsState by viewModel.friendsState.collectAsState()
    val pendingRequestsState by viewModel.pendingRequestsState.collectAsState()
    val friendRequestsState by viewModel.friendRequestsState.collectAsState()
    val senderInfo by viewModel.requestSenderInfo.collectAsState()

    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(friendRequestsState) {
        when (friendRequestsState) {
            is FriendRequestsState.Success -> {
                showAddFriendDialog = false
                showSuccessDialog = true
            }
            is FriendRequestsState.Error -> {
                emailError = (friendRequestsState as FriendRequestsState.Error).message
            }
            else -> {
            }
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
                IconButton(onClick = {
                    showAddFriendDialog = true
                    email = ""
                    emailError = null
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Friend",
                        tint = Color.Black
                    )
                }

                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Black
                    )
                }
            }
        )

        when (pendingRequestsState) {
            is PendingRequestsState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
            is PendingRequestsState.Success -> {
                val requests = (pendingRequestsState as PendingRequestsState.Success).requests
                if (requests.isNotEmpty()) {
                    PendingFriendRequestsSection(
                        pendingRequests = requests,
                        senderInfo = senderInfo,
                        onAccept = { requestId -> viewModel.acceptFriendRequest(requestId) },
                        onReject = { requestId -> viewModel.rejectFriendRequest(requestId) }
                    )
                }
            }
            is PendingRequestsState.Error -> {
                Text(
                    text = (pendingRequestsState as PendingRequestsState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        when (friendsState) {
            is FriendsState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
            is FriendsState.Success -> {
                val friends = (friendsState as FriendsState.Success).friends
                LazyColumn {
                    items(friends) { friend ->
                        FriendItem(name = friend.name)
                        Divider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(start = 72.dp)
                        )
                    }
                }
            }
            is FriendsState.Error -> {
                Text(
                    text = (friendsState as FriendsState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
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

    if (showAddFriendDialog) {
        Dialog(onDismissRequest = { showAddFriendDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Add Friend",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = { Text("Friend's Email") },
                        singleLine = true,
                        isError = emailError != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    emailError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddFriendDialog = false }) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = {
                            if (email.isBlank()) {
                                emailError = "Email cannot be empty"
                                return@Button
                            }
                            viewModel.sendFriendRequest(email)
                        }) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.Green,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your friend request has been sent!",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { showSuccessDialog = false }) {
                        Text("Ok")
                    }
                }
            }
        }
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

@Composable
fun PendingFriendRequestsSection(
    pendingRequests: List<FriendRequest>,
    senderInfo: Map<String, User>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    if (pendingRequests.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Pending Friend Requests",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Divider()

        pendingRequests.forEach { request ->
            PendingFriendRequestItem(
                requestId = request.id,
                senderId = request.senderId,
                senderName = senderInfo[request.senderId]?.name,
                onAccept = onAccept,
                onReject = onReject
            )
            Divider()
        }
    }
}

@Composable
fun PendingFriendRequestItem(
    requestId: String,
    senderId: String,
    senderName: String?,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE6E6FA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = senderName?.firstOrNull()?.toString() ?: "?",
                    color = Color(0xFF673AB7),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Friend Request from User ${senderName ?: senderId}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f, false)
            )
        }

        // Action buttons
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            IconButton(onClick = { onReject(requestId) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Reject",
                    tint = Color.Red
                )
            }

            IconButton(onClick = { onAccept(requestId) }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accept",
                    tint = Color.Green
                )
            }
        }
    }
}
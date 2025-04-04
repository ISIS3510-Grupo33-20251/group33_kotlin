package com.example.universe.presentation.friends

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import com.example.universe.presentation.location.LocationViewModel

@Composable
fun FriendsScreen(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    viewModel: FriendsViewModel = hiltViewModel(),
    locationViewModel: LocationViewModel = hiltViewModel()
) {
    val friendsState by viewModel.friendsState.collectAsState()
    val pendingRequestsState by viewModel.pendingRequestsState.collectAsState()
    val friendRequestsState by viewModel.friendRequestsState.collectAsState()
    val senderInfo by viewModel.requestSenderInfo.collectAsState()

    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val error by viewModel.error.collectAsState()

    val friendInfoMap by viewModel.friendInfoMap.collectAsState()

    val friendsWithLocationAndInfo by locationViewModel.friendsWithLocationAndInfo.collectAsState()

    LaunchedEffect(Unit) {
        locationViewModel.loadFriendLocations()
    }

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

    LaunchedEffect(friendsWithLocationAndInfo) {
        Log.d("FriendsScreen", "Friends with location size: ${friendsWithLocationAndInfo.size}")
        friendsWithLocationAndInfo.forEach { friend ->
            Log.d("FriendsScreen", "Friend with distance: ${friend.user.name}, distance: ${friend.distance}")
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

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Column for all the possible scrollables that can go here
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Pending requests section
                if (pendingRequestsState is PendingRequestsState.Success) {
                    val requests = (pendingRequestsState as PendingRequestsState.Success).requests
                    if (requests.isNotEmpty()) {
                        item {
                            PendingFriendRequestsSection(
                                pendingRequests = requests,
                                senderInfo = senderInfo,
                                onAccept = { requestId -> viewModel.acceptFriendRequest(requestId) },
                                onReject = { requestId -> viewModel.rejectFriendRequest(requestId) }
                            )
                        }
                    }
                }

                // Nearby Friends section header
                item {
                    Text(
                        text = "Nearby Friends",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Conditional content for nearby friends
                if (friendsWithLocationAndInfo.isEmpty()) {
                    item {
                        Text(
                            text = "No friends with active location",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                } else {
                    // Nearby Friends section
                    items(friendsWithLocationAndInfo) { friendInfo ->
                        FriendDistanceItem(
                            name = friendInfo.user.name,
                            distance = LocationUtils.formatDistance(friendInfo.distance),
                            onClick = { /* Handle friend click */ }
                        )
                        Divider()
                    }


                }

                // All friends section
                if (friendsState is FriendsState.Success) {
                    item {
                        Text(
                            text = "All Friends",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    val friends = (friendsState as FriendsState.Success).friends
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

            // Show loading indicators if needed
            if (pendingRequestsState is PendingRequestsState.Loading ||
                friendsState is FriendsState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
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
        }

        Footer(
            selectedScreen = "schedule",
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

    Log.d("FriendUI", "Rendering section with ${pendingRequests.size} requests")
    Log.d("FriendUI", "Sender info map contains ${senderInfo.size} entries: ${senderInfo.keys}")

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

    Log.d("FriendUI", "Rendering request item - ID: $requestId, Sender: $senderId, Name: $senderName")
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
                text = "Request from ${senderName ?: "user $senderId"}",
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

@Composable
fun FriendDistanceItem(
    name: String,
    distance: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = distance,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
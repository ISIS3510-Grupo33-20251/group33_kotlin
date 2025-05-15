package com.example.universe.presentation.assignments

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.universe.data.models.Flashcard
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment



@Composable
fun FlashcardDetailScreen(
    navController: NavHostController,
    flashcardViewModel: FlashcardViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val userId = savedStateHandle?.get<String>("userId")
    val subject = savedStateHandle?.get<String>("subject")
    val flashcards by flashcardViewModel.flashcards.collectAsState()
    val error by flashcardViewModel.error.collectAsState()
    var loading by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(false) }

    val context = LocalContext.current

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    LaunchedEffect(userId, subject) {
        if (!userId.isNullOrBlank() && !subject.isNullOrBlank()) {
            val networkAvailable = isNetworkAvailable()
            isOffline = !networkAvailable

            try {
                flashcardViewModel.fetchFlashcards(userId, subject)
                delay(4000L)
            } catch (e: Exception) {
                isOffline = true
            }

            if (!networkAvailable && flashcardViewModel.flashcards.value.isEmpty()) {
                isOffline = true
            }

            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        when {
            loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading flashcards...", fontSize = 16.sp)
            }

            isOffline && flashcards.isNotEmpty() -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    backgroundColor = Color(0xFFFFF3CD),
                    elevation = 0.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline mode — showing cached flashcards",
                            color = Color(0xFF856404),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                FlashcardList(flashcards)
            }

            isOffline -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    backgroundColor = Color(0xFFFFF3CD),
                    elevation = 0.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No internet connection and no cached flashcards available.",
                            color = Color(0xFF856404),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            error != null -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    backgroundColor = Color(0xFFFFF3CD),
                    elevation = 0.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Error: $error",
                            color = Color(0xFF856404),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            flashcards.isEmpty() -> {
                Text(
                    text = "The AI could not generate a flashcard because there is not enough information for this subject.",
                    color = Color(0xFF444444),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            else -> {
                FlashcardList(flashcards)
            }
        }
    }
}

@Composable
fun FlashcardList(flashcards: List<Flashcard>) {
    LazyColumn {
        items(flashcards) { card ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Q: ${card.question}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("A: ${card.answer}")
                }
            }
        }
    }
}

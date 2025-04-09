package com.example.universe.presentation.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.universe.ui.theme.LogoFirstTypography
import com.example.universe.ui.theme.UniverseTheme

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val offlineMode by viewModel.offlineMode.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(offlineMode) {
        if (offlineMode) {
            errorMessage = "You are offline. Login requires an internet connection. Please check your internet connection and restart the app."
        } else if (errorMessage == "You are offline. Login requires an internet connection.") {
            // Clear the offline error message when back online
            errorMessage = ""
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Error -> {
                errorMessage = (loginState as LoginState.Error).message
            }
            is LoginState.Success -> {
                viewModel.resetLoginState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Bottom left circle
            drawCircle(
                color = Color(0x80667EFF),
                radius = 120.dp.toPx(),
                center = Offset(-20.dp.toPx(), size.height - 350.dp.toPx())
            )

            // Top right shape
            drawCircle(
                color = Color(0x80667EFF),
                radius = 150.dp.toPx(),
                center = Offset(380.dp.toPx(), size.height - 550.dp.toPx())
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // UniVerse
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        append("Uni")
                    }
                    append("\n") // Add a newline
                    withStyle(style = SpanStyle(color = Color(0xFF4285F4))) {
                        append("Verse")
                    }
                },
                fontSize = 96.sp,
                style = LogoFirstTypography.copy(
                    lineHeight = 80.sp  // Reduce the line height (adjust as needed)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(120.dp))

            // Email field
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Email",
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Password field
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Password",
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Login button
            Button(
                onClick = {
                    if (offlineMode) {
                        errorMessage = "You are offline. Login requires an internet connection. Please check your internet connection and restart the app."
                    } else if (email.isNotEmpty() && password.isNotEmpty()) {
                        viewModel.login(email, password)
                    } else {
                        errorMessage = "Please fill in all fields"
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF667EFF)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !offlineMode && loginState !is LoginState.Loading
            ) {
                if (loginState is LoginState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Log-in",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Don't have an account link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        "Don't have an account? Sign up",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}



package com.example.universe.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.universe.R
import com.example.universe.ui.theme.LogoFirstTypography
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle


@Preview
@Composable
fun LoginScreenPreview() {
    Surface {
    WelcomeScreen(
        onNavigateToRegister = {},
        onNavigateToLogin = {})
}}
@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
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
                .fillMaxWidth()
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

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

            Spacer(modifier = Modifier.height(300.dp))

            // Buttons
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Log-in",
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }

            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Create a new account",
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Social login buttons
            SocialLoginButton(
                text = "Sign in with Google",
                iconResId = R.drawable.icon_google,
                onClick = { /* Not implemented yet */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            )

            SocialLoginButton(
                text = "Sign in with Facebook",
                iconResId = R.drawable.icon_facebook,
                onClick = { /* Not implemented yet */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}
package com.example.universe.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.universe.R

val SmoochSans = FontFamily(
    Font(R.font.smooch_sans_regular, FontWeight.Normal),
    Font(R.font.smooch_sans_bold, FontWeight.Bold)
)

val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

val LogoFirstTypography = TextStyle(
    fontFamily = SmoochSans,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp
)
package com.erendogan6.seyahatasistanim.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.erendogan6.seyahatasistanim.R

val LatoFontFamily =
    FontFamily(
        Font(R.font.lato, FontWeight.Normal),
        Font(R.font.lato_bold, FontWeight.Bold),
    )

val Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 57.sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 45.sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 28.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = LatoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            ),
    )

package com.erendogan6.seyahatasistanim.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme =
    lightColorScheme(
        primary = Blue80,
        secondary = BlueGrey80,
        tertiary = Green80,
        background = LightBlueBackground,
        surface = surfaceColor,
    )

@Composable
fun seyahatAsistanimTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}

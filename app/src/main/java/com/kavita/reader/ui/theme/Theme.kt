package com.kavita.reader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = Color(0xFF2F5D50),
    secondary = Color(0xFF6E5849),
    tertiary = Color(0xFF425E7C),
    surface = Color(0xFFFBFCF8),
    background = Color(0xFFF6F7F1)
)

@Composable
fun KavitaReaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

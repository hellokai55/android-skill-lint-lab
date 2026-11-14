package com.example.movielab.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * ComponentActivity.enableEdgeToEdge already sets system-bar icon contrast.
 * The frozen edge-to-edge skill says not to also force icon appearance in that case.
 */
@Composable
fun MovieLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}

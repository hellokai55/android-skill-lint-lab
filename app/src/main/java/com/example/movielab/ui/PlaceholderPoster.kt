package com.example.movielab.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderPoster(
    title: String,
    hue: Int,
    modifier: Modifier = Modifier,
    heightDp: Int = 180,
) {
    val top = Color.hsl(hue.toFloat(), 0.45f, 0.62f)
    val bottom = Color.hsl((hue + 28).toFloat() % 360f, 0.50f, 0.32f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .semantics { contentDescription = "占位海报，非真实电影海报：$title" },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(brush = Brush.linearGradient(listOf(top, bottom), start = Offset.Zero, end = Offset(size.width, size.height)))
        }
        Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Text("占位海报", color = Color.White, style = MaterialTheme.typography.labelLarge)
            Text("PLACEHOLDER", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

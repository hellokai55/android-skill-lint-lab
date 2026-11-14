package com.example.movielab.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movielab.R

@Composable
fun PosterPlaceholder(
    seed: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val description = stringResource(R.string.poster_description)
    val label = stringResource(R.string.poster_label)
    val background = remember(seed) { Color.hsl(hue(seed, 1), 0.42f, 0.38f) }
    val accent = remember(seed) { Color.hsl(hue(seed, 2), 0.55f, 0.62f) }
    val ink = remember(seed) { Color.hsl(hue(seed, 3), 0.35f, 0.22f) }

    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = description
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(background)
            val stripeCount = 5
            val stripeWidth = size.width / (stripeCount * 2f)
            repeat(stripeCount) { index ->
                drawRect(
                    color = accent.copy(alpha = 0.85f),
                    topLeft = Offset(x = stripeWidth * (index * 2f + 0.4f), y = 0f),
                    size = Size(width = stripeWidth * 0.7f, height = size.height),
                )
            }
            drawCircle(
                color = ink.copy(alpha = 0.9f),
                radius = size.minDimension * 0.18f,
                center = Offset(size.width * 0.72f, size.height * 0.34f),
            )
            drawRect(
                color = Color.White.copy(alpha = 0.18f),
                topLeft = Offset(size.width * 0.12f, size.height * 0.18f),
                size = Size(size.width * 0.34f, size.height * 0.22f),
            )
        }
        Text(
            text = label,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.62f))
                .padding(horizontal = 4.dp, vertical = if (compact) 2.dp else 8.dp),
            color = Color.White,
            fontSize = if (compact) 8.sp else 14.sp,
            lineHeight = if (compact) 10.sp else 18.sp,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}

private fun hue(seed: String, salt: Int): Float {
    val mixed = seed.hashCode() xor (salt * 0x9E3779B9.toInt())
    return (mixed.toUInt() % 360u).toFloat()
}

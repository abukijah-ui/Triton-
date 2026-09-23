package com.example.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun goldShimmerBrush(
    colors: List<Color> = listOf(
        GoldDeep,
        GoldAccent,
        GoldHighlight,
        GoldAccent,
        GoldDeep
    ),
    durationMillis: Int = 2600
): Brush {
    val transition = rememberInfiniteTransition(label = "gold_shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gold_shimmer_anim"
    )

    return Brush.linearGradient(
        colors = colors,
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim + 500f, translateAnim + 500f)
    )
}

fun Modifier.shimmerGoldBackground(
    brush: Brush,
    shape: Shape = RectangleShape
): Modifier = this.background(brush = brush, shape = shape)

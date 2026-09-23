package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldPrimary

/**
 * Geometric SVG Vector Glyph for the Triton Trident Emblem.
 * Renders a crisp, precision-crafted nautical trident with center prong and curved outer barbs.
 */
@Composable
fun TritonTridentGlyph(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = GoldPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = (w * 0.11f).coerceAtLeast(1.5f)

        // Main vertical center shaft
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.12f),
            end = Offset(w * 0.5f, h * 0.92f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Center spear tip
        val centerTip = Path().apply {
            moveTo(w * 0.5f, h * 0.06f)
            lineTo(w * 0.40f, h * 0.22f)
            lineTo(w * 0.60f, h * 0.22f)
            close()
        }
        drawPath(path = centerTip, color = tint, style = Fill)

        // Left curved trident barb
        val leftBarb = Path().apply {
            moveTo(w * 0.20f, h * 0.18f)
            cubicTo(
                w * 0.18f, h * 0.45f,
                w * 0.26f, h * 0.58f,
                w * 0.50f, h * 0.58f
            )
        }
        drawPath(
            path = leftBarb,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Left spear tip
        val leftTip = Path().apply {
            moveTo(w * 0.20f, h * 0.14f)
            lineTo(w * 0.12f, h * 0.26f)
            lineTo(w * 0.28f, h * 0.26f)
            close()
        }
        drawPath(path = leftTip, color = tint, style = Fill)

        // Right curved trident barb
        val rightBarb = Path().apply {
            moveTo(w * 0.80f, h * 0.18f)
            cubicTo(
                w * 0.82f, h * 0.45f,
                w * 0.74f, h * 0.58f,
                w * 0.50f, h * 0.58f
            )
        }
        drawPath(
            path = rightBarb,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Right spear tip
        val rightTip = Path().apply {
            moveTo(w * 0.80f, h * 0.14f)
            lineTo(w * 0.72f, h * 0.26f)
            lineTo(w * 0.88f, h * 0.26f)
            close()
        }
        drawPath(path = rightTip, color = tint, style = Fill)

        // Crossbar collar
        drawLine(
            color = tint,
            start = Offset(w * 0.36f, h * 0.65f),
            end = Offset(w * 0.64f, h * 0.65f),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Geometric SVG Vector Glyph for Aureate 4-point Sparkle / Star.
 */
@Composable
fun AureateSparkleGlyph(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = GoldPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        val path = Path().apply {
            moveTo(cx, h * 0.05f)
            quadraticTo(cx, cy, w * 0.95f, cy)
            quadraticTo(cx, cy, cx, h * 0.95f)
            quadraticTo(cx, cy, w * 0.05f, cy)
            quadraticTo(cx, cy, cx, h * 0.05f)
            close()
        }
        drawPath(path = path, color = tint, style = Fill)
    }
}

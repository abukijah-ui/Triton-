package com.example.ui.components.artifact

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Live Preview Canvas for GLSL/Shader & Simulation Artifacts.
 */
@Composable
fun ArtifactInteractiveCanvas(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    var waveSpeed by remember { mutableFloatStateOf(1.0f) }
    var particleDensity by remember { mutableFloatStateOf(48f) }
    var waveAmplitude by remember { mutableFloatStateOf(35f) }

    val infiniteTransition = rememberInfiniteTransition(label = "canvas_anim")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (4000 / waveSpeed).toInt().coerceAtLeast(500), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_uniform"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("artifact_interactive_canvas")
    ) {
        // Visual Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color(0xFF0D0E11) else Color(0xFF16161A))
                .border(
                    1.dp,
                    GoldPrimary.copy(alpha = 0.35f),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val count = particleDensity.toInt()

                // Draw central harmonic orbital rings
                for (ring in 1..4) {
                    val r = (size.minDimension * 0.11f * ring)
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.12f / ring),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Draw golden particle wave matrix
                for (i in 0 until count) {
                    val progress = i.toFloat() / count
                    val angle = progress * 6.28318f * 3f + time
                    val radiusOffset = sin(angle + time) * waveAmplitude
                    val baseRadius = size.minDimension * 0.32f + radiusOffset

                    val x = cx + cos(angle) * baseRadius
                    val y = cy + sin(angle) * baseRadius * 0.7f

                    val particleColor = when {
                        i % 3 == 0 -> GoldHighlight
                        i % 3 == 1 -> GoldPrimary
                        else -> GoldAmber
                    }

                    // Particle core
                    drawCircle(
                        color = particleColor.copy(alpha = 0.85f),
                        radius = 3.5f + sin(time * 2f + i) * 1.5f,
                        center = Offset(x, y)
                    )

                    // Connecting auric filament to adjacent particle
                    if (i > 0) {
                        val prevAngle = ((i - 1).toFloat() / count) * 6.28318f * 3f + time
                        val prevRadOffset = sin(prevAngle + time) * waveAmplitude
                        val prevBaseRad = size.minDimension * 0.32f + prevRadOffset
                        val px = cx + cos(prevAngle) * prevBaseRad
                        val py = cy + sin(prevAngle) * prevBaseRad * 0.7f

                        drawLine(
                            color = GoldPrimary.copy(alpha = 0.25f),
                            start = Offset(px, py),
                            end = Offset(x, y),
                            strokeWidth = 1f
                        )
                    }
                }
            }

            // Real-time Telemetry Tag
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, GoldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "60 FPS · ${particleDensity.toInt()} NODES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = GoldAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Shader Parameter Sliders
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.3f else 0.5f),
            border = androidx.compose.foundation.BorderStroke(
                0.8.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "INTERACTIVE CONTROLS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = CinzelFontFamily,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    ),
                    color = GoldPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Slider 1: Kinetic Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Kinetic Speed",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JakartaFontFamily),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", waveSpeed)}x",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GoldPrimary
                    )
                }
                Slider(
                    value = waveSpeed,
                    onValueChange = { waveSpeed = it },
                    valueRange = 0.2f..3.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldPrimary,
                        activeTrackColor = GoldPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                )

                // Slider 2: Node Density
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Particle Matrix Density",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JakartaFontFamily),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${particleDensity.toInt()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GoldPrimary
                    )
                }
                Slider(
                    value = particleDensity,
                    onValueChange = { particleDensity = it },
                    valueRange = 16f..96f,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldPrimary,
                        activeTrackColor = GoldPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}

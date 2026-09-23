package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TritonArtifact
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TritonArtifactViewer(
    artifact: TritonArtifact,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Preview, 1: Code
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("artifact_viewer_modal"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = artifact.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = CinzelFontFamily,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TRITON ARTIFACT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  ${artifact.language.uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("artifact_code", artifact.content)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                        },
                        modifier = Modifier.testTag("artifact_copy_button")
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (isCopied) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("artifact_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close artifact",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Tabs: Preview & Code
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GoldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldPrimary,
                        height = 2.5.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Preview",
                                fontFamily = JakartaFontFamily,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Code",
                                fontFamily = JakartaFontFamily,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    ArtifactInteractivePreview(artifact = artifact, isDark = isDark)
                } else {
                    Box(modifier = Modifier.padding(16.dp)) {
                        TritonCodeBlock(
                            code = artifact.content,
                            language = artifact.language
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtifactInteractivePreview(artifact: TritonArtifact, isDark: Boolean) {
    var particleIntensity by remember { mutableFloatStateOf(0.7f) }
    var shimmerFrequency by remember { mutableFloatStateOf(1.2f) }

    val transition = rememberInfiniteTransition(label = "particle_transition")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_anim"
    )

    val canvasSurfaceColor = if (isDark) Color(0xFF141417) else Color(0xFFF7F5EE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Visualizer Canvas representing Triton's real-time rendering
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = canvasSurfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldPrimary.copy(alpha = if (isDark) 0.4f else 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val baseRadius = size.minDimension * 0.28f

                    // Draw golden caustics and particle orbits
                    val particleCount = (40 * particleIntensity).toInt()
                    for (i in 0 until particleCount) {
                        val angle = (i.toFloat() / particleCount) * 6.28318f + (time * shimmerFrequency)
                        val radiusMod = baseRadius + sin(angle * 3f + time) * 35f
                        val x = center.x + cos(angle) * radiusMod
                        val y = center.y + sin(angle) * radiusMod

                        val alpha = (sin(angle + time) * 0.5f + 0.5f).coerceIn(0.2f, 1f)
                        drawCircle(
                            color = if (i % 2 == 0) GoldHighlight.copy(alpha = alpha) else GoldPrimary.copy(alpha = alpha),
                            radius = (4f + sin(time + i) * 2f) * particleIntensity,
                            center = Offset(x, y)
                        )
                    }

                    // Shimmering Golden Core
                    drawCircle(
                        color = GoldPrimary.copy(alpha = if (isDark) 0.15f else 0.25f),
                        radius = baseRadius * 0.7f,
                        center = center
                    )
                    drawCircle(
                        color = if (isDark) GoldAccent.copy(alpha = 0.8f) else GoldAmber.copy(alpha = 0.9f),
                        radius = baseRadius * 0.5f,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "AURIC ENGINE LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = CinzelFontFamily,
                            letterSpacing = 1.5.sp,
                            fontSize = 11.sp
                        ),
                        color = if (isDark) GoldHighlight else GoldAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "60 FPS • Vulkan GLSL Simulation",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive control dashboard
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Interactive Parameters",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Particle Intensity Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Photon Density",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${(particleIntensity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp),
                        color = GoldPrimary
                    )
                }
                Slider(
                    value = particleIntensity,
                    onValueChange = { particleIntensity = it },
                    valueRange = 0.2f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldPrimary,
                        activeTrackColor = GoldAccent
                    )
                )

                // Frequency Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Shimmer Wave Frequency",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${(shimmerFrequency * 10).toInt() / 10.0}x",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp),
                        color = GoldPrimary
                    )
                }
                Slider(
                    value = shimmerFrequency,
                    onValueChange = { shimmerFrequency = it },
                    valueRange = 0.5f..3.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldPrimary,
                        activeTrackColor = GoldAccent
                    )
                )
            }
        }
    }
}

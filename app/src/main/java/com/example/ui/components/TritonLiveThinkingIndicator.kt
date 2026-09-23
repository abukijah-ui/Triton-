package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveThinkingState
import com.example.model.ThinkingPhase
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.goldShimmerBrush
import kotlinx.coroutines.delay

@Composable
fun TritonLiveThinkingIndicator(
    thinkingState: LiveThinkingState?,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_pulse")

    // Pulsing halo animation for the central icon
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Shimmer sweep across the progress beam
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beam_shimmer"
    )

    // Blinking cursor for live thoughts
    var showCursor by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            showCursor = !showCursor
        }
    }

    if (thinkingState == null) {
        // Standard generation indicator when Thinking Mode is not enabled (e.g. Haiku)
        StandardGeneratingIndicator(isDark = isDark, pulseScale = pulseScale)
        return
    }

    val isExpanded = thinkingState.isExpanded
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrow_rot")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_thinking_mode_indicator")
    ) {
        // Top Thinking Card / Header
        Surface(
            onClick = onToggleExpanded,
            shape = RoundedCornerShape(14.dp),
            color = if (isDark) Color(0xFF1B1B20) else Color(0xFFF9F6EE),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                GoldPrimary.copy(alpha = if (isDark) 0.45f else 0.55f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("thinking_header_pill")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Left: Pulsing Golden Icon + Thinking Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Pulsing Aureate Brain / Trident Emblem
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(32.dp)
                        ) {
                            // Expanding pulse ring
                            Box(
                                modifier = Modifier
                                    .size((28 * pulseScale).dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = pulseAlpha * 0.4f))
                            )
                            // Inner solid core
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(GoldPrimary, GoldAmber, GoldHighlight)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Thinking",
                                    tint = Color.Black,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Thinking...",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    ),
                                    color = if (isDark) GoldHighlight else GoldAmber
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Live elapsed timer
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GoldPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", thinkingState.elapsedSeconds)}s",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = GoldPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Active Phase badge
                            Text(
                                text = "${thinkingState.phase.badge}: ${thinkingState.phase.label}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    // Right: Expand/Collapse affordance
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            0.8.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isExpanded) "Hide thoughts" else "View thoughts",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(14.dp)
                                    .rotate(rotation)
                            )
                        }
                    }
                }

                // Shimmering Golden Progress Activity Beam
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(GoldPrimary.copy(alpha = 0.15f))
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(2.5.dp)) {
                        val barWidth = size.width * 0.35f
                        val startX = shimmerOffset.coerceIn(-barWidth, size.width + barWidth)
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    GoldPrimary.copy(alpha = 0.7f),
                                    GoldHighlight,
                                    GoldPrimary.copy(alpha = 0.7f),
                                    Color.Transparent
                                ),
                                startX = startX,
                                endX = startX + barWidth
                            ),
                            size = size
                        )
                    }
                }
            }
        }

        // Live Thought Stream Expanded Drawer (Claude Extended Thinking Experience)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF141418) else Color(0xFFFBF8F2))
                    .border(
                        0.9.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.45f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
                    .testTag("live_thinking_stream_container")
            ) {
                // Cognitive Pipeline Stepper (Phases 1 to 4)
                ThinkingPhasesPipeline(currentPhase = thinkingState.phase, isDark = isDark)

                Spacer(modifier = Modifier.height(12.dp))

                // Streaming Thought Monologue Container
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Left vertical golden thread / thinking rail
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(120.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(GoldPrimary, GoldAmber, GoldPrimary.copy(alpha = 0.2f))
                                )
                            )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .verticalScroll(rememberScrollState(Int.MAX_VALUE))
                    ) {
                        Text(
                            text = "LIVE REASONING MONOLOGUE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = CinzelFontFamily,
                                letterSpacing = 1.sp,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GoldPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Render previously completed thoughts
                        thinkingState.completedSteps.forEach { step ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.5.sp,
                                        lineHeight = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Render currently active streaming thought
                        if (thinkingState.thoughtsStream.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GoldPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = thinkingState.thoughtsStream + if (showCursor) " ▋" else "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.5.sp,
                                        lineHeight = 17.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (isDark) Color(0xFFE2E0D8) else Color(0xFF2D2B26)
                                )
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "• ${thinkingState.activeThoughtSummary}" + if (showCursor) " ▋" else "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.5.sp,
                                        lineHeight = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom transparency footnote
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Extended Thinking enabled • Triton Aureate Engine",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JakartaFontFamily,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Text(
                        text = "Transparent Reasoning",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JakartaFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = GoldPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ThinkingPhasesPipeline(
    currentPhase: ThinkingPhase,
    isDark: Boolean
) {
    val phases = ThinkingPhase.values()
    val currentIndex = currentPhase.ordinal

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        phases.forEachIndexed { index, phase ->
            val isDone = index < currentIndex
            val isCurrent = index == currentIndex
            val isFuture = index > currentIndex

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Node circle
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone -> Color(0xFF22C55E).copy(alpha = 0.2f)
                                isCurrent -> GoldPrimary.copy(alpha = 0.25f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isDone -> Color(0xFF22C55E)
                                isCurrent -> GoldPrimary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isDone -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        isCurrent -> {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary)
                            )
                        }
                        else -> {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Label
                Text(
                    text = when (phase) {
                        ThinkingPhase.DECONSTRUCTING -> "Deconstruct"
                        ThinkingPhase.EXPLORING -> "Explore"
                        ThinkingPhase.SYNTHESIZING -> "Synthesize"
                        ThinkingPhase.VERIFYING -> "Verify"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = when {
                        isDone -> Color(0xFF22C55E)
                        isCurrent -> if (isDark) GoldHighlight else GoldAmber
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                    maxLines = 1
                )

                // Connector line between nodes
                if (index < phases.size - 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.5.dp)
                            .background(
                                if (isDone) Color(0xFF22C55E).copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Composable
private fun StandardGeneratingIndicator(
    isDark: Boolean,
    pulseScale: Float
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color(0xFF1B1B20) else Color(0xFFF9F6EE),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            GoldPrimary.copy(alpha = if (isDark) 0.35f else 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("standard_generating_indicator")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((24 * pulseScale).dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.15f))
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.2f))
                        .border(1.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    TritonTridentGlyph(size = 12.dp, tint = GoldPrimary)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Triton is generating response...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (isDark) GoldHighlight else GoldAmber
                )
                Text(
                    text = "Aureate language synthesis in progress",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 10.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

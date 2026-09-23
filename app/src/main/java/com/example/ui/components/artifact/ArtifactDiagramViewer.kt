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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TritonArtifact
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily

data class DiagramNode(
    val id: String,
    val title: String,
    val subtitle: String,
    val role: String,
    val details: String,
    val isPrimary: Boolean = false
)

/**
 * Modular Architecture & Diagram Canvas Viewer for structured outputs.
 */
@Composable
fun ArtifactDiagramViewer(
    artifact: TritonArtifact,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val nodes = remember {
        listOf(
            DiagramNode(
                id = "n1",
                title = "Prompt Dispatcher",
                subtitle = "Token Stream & Input Normalizer",
                role = "Ingestion Layer",
                details = "Parses multimodal input, manages context windows, and routes intent."
            ),
            DiagramNode(
                id = "n2",
                title = "Cognitive Core (3.7)",
                subtitle = "Hybrid Extended Thinking Engine",
                role = "Inference & Reasoning",
                details = "Performs test-time compute scaling across 4-phase cognitive pipeline.",
                isPrimary = true
            ),
            DiagramNode(
                id = "n3",
                title = "Artifact Synthesizer",
                subtitle = "Code & Document Transpiler",
                role = "Generation Unit",
                details = "Extracts structured codeblocks, markdown tables, and live GLSL shaders."
            ),
            DiagramNode(
                id = "n4",
                title = "Artifact Display Panel",
                subtitle = "Dedicated Modular Renderer",
                role = "Presentation Plane",
                details = "Renders real-time interactive canvas, formatted documents, and code editor."
            )
        )
    }

    var selectedNode by remember { mutableStateOf(nodes[1]) }

    val infiniteTransition = rememberInfiniteTransition(label = "flow_anim")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("artifact_diagram_viewer")
    ) {
        // Diagram Header
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.45f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.25f else 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SYSTEM TOPOLOGY MAP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = GoldPrimary
                    )
                }
                Text(
                    text = "Tap node to inspect",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagram Nodes Visual Stack with animated connecting data stream
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color(0xFF0F1013) else Color(0xFFF3F4F6))
                .border(1.dp, GoldPrimary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            nodes.forEachIndexed { index, node ->
                val isSelected = selectedNode.id == node.id

                Surface(
                    onClick = { selectedNode = node },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) {
                        GoldPrimary.copy(alpha = if (isDark) 0.2f else 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 1.5.dp else 0.8.dp,
                        if (isSelected) GoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diagram_node_${node.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (node.isPrimary) GoldPrimary else GoldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (node.isPrimary) Color.Black else GoldPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = node.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoldPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = node.role,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GoldPrimary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = node.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Connecting Animated Pulse Line
                if (index < nodes.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val midX = size.width / 2f
                            drawLine(
                                color = GoldPrimary,
                                start = Offset(midX, 0f),
                                end = Offset(midX, size.height),
                                strokeWidth = 2.5f,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(8f, 6f),
                                    phase = flowPhase
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Node Inspection Drawer
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.45f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                GoldPrimary.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NODE SPECIFICATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        ),
                        color = GoldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = selectedNode.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = selectedNode.details,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

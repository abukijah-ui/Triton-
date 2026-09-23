package com.example.ui.components.artifact

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArtifactType
import com.example.model.TritonArtifact
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.delay

/**
 * Dedicated, modular Artifact Display Panel component with multi-tab rendering
 * (Preview, Code, Document, Diagram, and Inspector) for structured AI outputs.
 */
@Composable
fun TritonArtifactPanel(
    artifact: TritonArtifact,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isCopied by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    val iconVector = when (artifact.type) {
        ArtifactType.CODE -> Icons.Default.Code
        ArtifactType.MARKDOWN -> Icons.Default.Description
        ArtifactType.DIAGRAM -> Icons.Default.AccountTree
        ArtifactType.SVG, ArtifactType.HTML -> Icons.Default.Palette
    }

    // Determine available tabs based on artifact type
    val tabs = remember(artifact.type) {
        when (artifact.type) {
            ArtifactType.CODE -> listOf(
                ArtifactTabItem("Preview", Icons.Default.Visibility),
                ArtifactTabItem("Code", Icons.Default.Code),
                ArtifactTabItem("Inspector", Icons.Default.Analytics)
            )
            ArtifactType.MARKDOWN -> listOf(
                ArtifactTabItem("Document", Icons.Default.Description),
                ArtifactTabItem("Source", Icons.Default.Code),
                ArtifactTabItem("Inspector", Icons.Default.Analytics)
            )
            ArtifactType.DIAGRAM -> listOf(
                ArtifactTabItem("Diagram", Icons.Default.AccountTree),
                ArtifactTabItem("Source", Icons.Default.Code),
                ArtifactTabItem("Inspector", Icons.Default.Analytics)
            )
            ArtifactType.SVG, ArtifactType.HTML -> listOf(
                ArtifactTabItem("Interactive", Icons.Default.Palette),
                ArtifactTabItem("Source", Icons.Default.Code),
                ArtifactTabItem("Inspector", Icons.Default.Analytics)
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("triton_artifact_panel"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Dedicated Artifact Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Left: Icon + Title + Metadata
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldPrimary.copy(alpha = 0.15f))
                                .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = artifact.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = CinzelFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoldPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "v1.0",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GoldPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TRITON ARTIFACT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = GoldPrimary
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "•  ${artifact.language.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Right: Actions (Copy, Share, Fullscreen, Close)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Copy Button
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("artifact_content", artifact.content)
                                clipboard.setPrimaryClip(clip)
                                isCopied = true
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("artifact_panel_copy_button")
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy artifact",
                                tint = if (isCopied) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TITLE, artifact.title)
                                    putExtra(Intent.EXTRA_TEXT, artifact.content)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Artifact: ${artifact.title}")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("artifact_panel_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share artifact",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Close Button
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("artifact_panel_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close artifact panel",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Tab Switcher Row
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
                tabs.forEachIndexed { index, tabItem ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = tabItem.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tabItem.title,
                                    fontFamily = JakartaFontFamily,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            // Tab Content Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Primary View (Preview / Document / Interactive / Diagram)
                        when (artifact.type) {
                            ArtifactType.MARKDOWN -> {
                                ArtifactDocumentViewer(
                                    artifact = artifact,
                                    isDark = isDark
                                )
                            }
                            ArtifactType.DIAGRAM -> {
                                ArtifactDiagramViewer(
                                    artifact = artifact,
                                    isDark = isDark
                                )
                            }
                            ArtifactType.SVG, ArtifactType.HTML -> {
                                ArtifactInteractiveCanvas(isDark = isDark)
                            }
                            ArtifactType.CODE -> {
                                if (artifact.language.lowercase() in listOf("glsl", "shader", "canvas", "opengl")) {
                                    ArtifactInteractiveCanvas(isDark = isDark)
                                } else {
                                    ArtifactDocumentViewer(
                                        artifact = artifact,
                                        isDark = isDark
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Source Code View
                        ArtifactCodeViewer(
                            code = artifact.content,
                            language = artifact.language,
                            isDark = isDark
                        )
                    }
                    2 -> {
                        // Inspector & Telemetry View
                        ArtifactInspectorView(
                            artifact = artifact,
                            isDark = isDark,
                            onShare = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TITLE, artifact.title)
                                    putExtra(Intent.EXTRA_TEXT, artifact.content)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Artifact: ${artifact.title}")
                                context.startActivity(shareIntent)
                            }
                        )
                    }
                }
            }
        }
    }
}

private data class ArtifactTabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

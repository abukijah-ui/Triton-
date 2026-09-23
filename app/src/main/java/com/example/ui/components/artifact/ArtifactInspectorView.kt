package com.example.ui.components.artifact

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TritonArtifact
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlinx.coroutines.delay

/**
 * Modular Inspector view providing metadata, token metrics, and export utilities for artifacts.
 */
@Composable
fun ArtifactInspectorView(
    artifact: TritonArtifact,
    isDark: Boolean,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    val lineCount = remember(artifact.content) { artifact.content.lines().size }
    val charCount = remember(artifact.content) { artifact.content.length }
    val estTokens = remember(charCount) { (charCount / 3.8).toInt() }
    val bytesFormatted = remember(artifact.content) {
        val bytes = artifact.content.toByteArray().size
        if (bytes < 1024) "$bytes B" else "${String.format("%.1f", bytes / 1024.0)} KB"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
            .testTag("artifact_inspector_view")
    ) {
        // Section: Metrics
        Text(
            text = "ARTIFACT METRICS & TELEMETRY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = CinzelFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            ),
            color = GoldPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2x2 Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                label = "Lines of Code",
                value = "$lineCount",
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Byte Size",
                value = bytesFormatted,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                label = "Estimated Tokens",
                value = "~$estTokens",
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Runtime Target",
                value = artifact.language.uppercase(),
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        Spacer(modifier = Modifier.height(20.dp))

        // Section: Artifact Identity Details
        Text(
            text = "STRUCTURAL METADATA",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = CinzelFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            ),
            color = GoldPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.45f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                MetadataRow(label = "Identifier", value = artifact.id)
                Spacer(modifier = Modifier.height(8.dp))
                MetadataRow(label = "Artifact Type", value = artifact.type.name)
                Spacer(modifier = Modifier.height(8.dp))
                MetadataRow(label = "Language Spec", value = artifact.language)
                Spacer(modifier = Modifier.height(8.dp))
                MetadataRow(label = "Architecture Version", value = "v1.0 (Live)")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Action buttons
        Text(
            text = "EXPORT & ACTIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = CinzelFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            ),
            color = GoldPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("artifact_content", artifact.content)
                    clipboard.setPrimaryClip(clip)
                    isCopied = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("inspector_copy_btn")
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCopied) "Copied!" else "Copy Raw",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = onShare,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("inspector_share_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Share",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.3f else 0.45f)
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = GoldPrimary
            )
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

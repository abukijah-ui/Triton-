package com.example.ui.components.artifact

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TritonArtifact
import com.example.ui.components.TritonMarkdownText
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily

/**
 * Modular Document Viewer for Artifacts, featuring reading statistics and rich Markdown rendering.
 */
@Composable
fun ArtifactDocumentViewer(
    artifact: TritonArtifact,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val wordCount by remember(artifact.content) {
        derivedStateOf {
            artifact.content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        }
    }
    val readingTimeMin by remember(wordCount) {
        derivedStateOf {
            maxOf(1, (wordCount / 200.0).toInt())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("artifact_document_viewer")
    ) {
        // Document Meta Header Banner
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
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DOCUMENT PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = GoldPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$wordCount words",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "~$readingTimeMin min read",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title and summary
        Text(
            text = artifact.title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = CinzelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (artifact.summary.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = artifact.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JakartaFontFamily,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        Spacer(modifier = Modifier.height(16.dp))

        // Rich Markdown Output
        TritonMarkdownText(
            text = artifact.content,
            onOpenArtifact = {}
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

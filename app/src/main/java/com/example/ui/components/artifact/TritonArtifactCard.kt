package com.example.ui.components.artifact

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Modular in-chat Artifact preview card that opens the dedicated Artifact panel when clicked.
 */
@Composable
fun TritonArtifactCard(
    artifact: TritonArtifact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val lineCount = remember(artifact.content) { artifact.content.lines().size }

    val iconVector = when (artifact.type) {
        ArtifactType.CODE -> Icons.Default.Code
        ArtifactType.MARKDOWN -> Icons.Default.Description
        ArtifactType.DIAGRAM -> Icons.Default.AccountTree
        ArtifactType.SVG, ArtifactType.HTML -> Icons.Default.Palette
    }

    val typeLabel = when (artifact.type) {
        ArtifactType.CODE -> "CODE ARTIFACT"
        ArtifactType.MARKDOWN -> "DOCUMENT ARTIFACT"
        ArtifactType.DIAGRAM -> "DIAGRAM ARTIFACT"
        ArtifactType.SVG, ArtifactType.HTML -> "INTERACTIVE ARTIFACT"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.65f),
        border = BorderStroke(
            1.2.dp,
            GoldPrimary.copy(alpha = if (isDark) 0.45f else 0.55f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("chat_artifact_card_${artifact.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary.copy(alpha = 0.15f))
                    .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata & Title
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JakartaFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        ),
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•  ${artifact.language.uppercase()}  •  $lineCount lines",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = artifact.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = CinzelFontFamily,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (artifact.summary.isNotBlank()) {
                    Text(
                        text = artifact.summary,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // View Action Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldPrimary.copy(alpha = 0.15f),
                border = BorderStroke(0.8.dp, GoldPrimary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        ),
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

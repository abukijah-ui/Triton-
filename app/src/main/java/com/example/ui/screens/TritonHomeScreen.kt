package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatSession
import com.example.model.PromptSuggestion
import com.example.model.TritonModel
import com.example.ui.components.TritonComposer
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.LocalIsDarkTheme
import java.util.Calendar

/**
 * Claude's signature Welcome / New Chat Screen, rendered with Triton's shimmering gold palette.
 */
@Composable
fun TritonHomeScreen(
    selectedModel: TritonModel,
    isThinkingEnabled: Boolean,
    onToggleThinking: () -> Unit,
    promptSuggestions: List<PromptSuggestion>,
    recentSessions: List<ChatSession>,
    onSelectSession: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    isGenerating: Boolean,
    onSelectModel: ((TritonModel) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val greeting = remember { getGreeting() }
    val isDark = LocalIsDarkTheme.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .testTag("triton_home_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Claude Hallmark: Radiant Asterisk Logo + Editorial Greeting
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 26.dp)
        ) {
            ClaudeAsterisk(
                size = 54.dp,
                modifier = Modifier.testTag("claude_asterisk_logo")
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "$greeting, Voyager",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = CinzelFontFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "How can Triton assist you today?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = JakartaFontFamily,
                    fontSize = 15.sp,
                    letterSpacing = 0.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Claude's Landmark Central Elevated Composer Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
        ) {
            TritonComposer(
                text = inputText,
                onTextChanged = { inputText = it },
                onSendMessage = {
                    onSendMessage(it)
                    inputText = ""
                },
                isGenerating = isGenerating,
                isThinkingEnabled = isThinkingEnabled,
                onToggleThinking = onToggleThinking,
                selectedModel = selectedModel,
                onSelectModel = onSelectModel,
                placeholder = "Reply to Triton or start a new task..."
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Claude's Action / Prompt Starters (2x2 Grid)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "EXPLORE CAPABILITIES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = CinzelFontFamily,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = GoldPrimary
                )
            }

            // High-fidelity Claude Starter Cards
            val starterPrompts = listOf(
                ClaudeStarter(
                    icon = Icons.Default.Code,
                    title = "Code GLSL Shader",
                    description = "Synthesize an auric particle wave artifact",
                    prompt = "Write a high-performance GLSL shader for an interactive golden shimmering particle wave, and compile it as an artifact."
                ),
                ClaudeStarter(
                    icon = Icons.Default.DataObject,
                    title = "Synthesize Comparative Table",
                    description = "Evaluate Triton vs Claude compute metrics",
                    prompt = "Generate a comprehensive comparative markdown table analyzing Triton 3.7 Sonnet versus Claude 3.7 Sonnet, including reasoning modes, artifact support, and benchmark metrics."
                ),
                ClaudeStarter(
                    icon = Icons.Default.Psychology,
                    title = "Deep Reasoning Pipeline",
                    description = "Deconstruct test-time compute & thinking phases",
                    prompt = "Explain how test-time compute scaling and extended thinking pipelines work in modern AI architectures like Claude and Triton."
                ),
                ClaudeStarter(
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    title = "Draft Executive Strategy",
                    description = "Structured roadmap with milestones & checklists",
                    prompt = "Draft an executive strategic roadmap for deploying a premier AI intelligence platform, formatted with milestones, checklists, and key invariants."
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                starterPrompts.chunked(2).forEach { rowStarters ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowStarters.forEach { starter ->
                            ClaudeStarterCard(
                                starter = starter,
                                onClick = { onSendMessage(starter.prompt) },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Claude's Recent Chats Drawer / Quick Access
        if (recentSessions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECENT CONVERSATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = CinzelFontFamily,
                            letterSpacing = 1.1.sp,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                recentSessions.take(3).forEach { session ->
                    Surface(
                        onClick = { onSelectSession(session.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.3f else 0.45f),
                        border = BorderStroke(
                            0.8.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.25f else 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("recent_session_${session.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔱",
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = session.previewSnippet.ifBlank { "Conversation with ${session.model.displayName}" },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = session.model.shortName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = GoldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Claude Hallmark Footer Disclaimer
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "Triton 3.7 Sonnet · Aureate Intelligence · AI responses may require verification.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = JakartaFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Claude's signature radiant 8-petal Asterisk emblem, rendered in shimmering gold.
 */
@Composable
fun ClaudeAsterisk(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension / 2f
        val petalWidth = radius * 0.30f
        val petalLength = radius * 0.85f

        // Draw 8 radial petals with rounded caps
        for (i in 0 until 8) {
            val angleDeg = i * 45f
            rotate(angleDeg, center) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(GoldHighlight, GoldPrimary, GoldAmber),
                        startY = center.y - petalLength,
                        endY = center.y
                    ),
                    topLeft = Offset(center.x - petalWidth / 2f, center.y - petalLength),
                    size = Size(petalWidth, petalLength),
                    cornerRadius = CornerRadius(petalWidth / 2f, petalWidth / 2f)
                )
            }
        }

        // Central gold core with glowing highlight
        drawCircle(
            color = GoldPrimary,
            radius = petalWidth * 0.8f,
            center = center
        )
        drawCircle(
            color = GoldHighlight,
            radius = petalWidth * 0.42f,
            center = center
        )
    }
}

data class ClaudeStarter(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val prompt: String
)

@Composable
private fun ClaudeStarterCard(
    starter: ClaudeStarter,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.3f else 0.45f)
        ),
        modifier = modifier.testTag("claude_starter_${starter.title.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldPrimary.copy(alpha = if (isDark) 0.15f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = starter.icon,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = starter.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = starter.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JakartaFontFamily,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}

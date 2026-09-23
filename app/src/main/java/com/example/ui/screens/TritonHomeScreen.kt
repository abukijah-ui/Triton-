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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InternetTimeService
import com.example.model.ChatSession
import com.example.model.PromptSuggestion
import com.example.model.TritonModel
import com.example.model.UserProfile
import com.example.ui.components.TritonComposer
import com.example.ui.components.TritonTridentGlyph
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.LocalIsDarkTheme

/**
 * Claude-inspired Welcome & New Chat Screen.
 *
 * Clean, focused layout:
 * - Real-time internet-synced time-of-day greeting (background verification without visual badge).
 * - Focused 4-card starter grid (Write an essay, Do some research, Create some code, Brainstorm ideas).
 * - Bottom-docked prompt input box.
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
    currentUser: UserProfile? = null,
    onSelectModel: ((TritonModel) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val isDark = LocalIsDarkTheme.current
    val scrollState = rememberScrollState()

    // Internet-derived time state (defaults to local device time immediately, then silently updates from internet)
    var timeInfo by remember { mutableStateOf(InternetTimeService.getLocalTimeInfo()) }

    LaunchedEffect(Unit) {
        try {
            val internetInfo = InternetTimeService.fetchInternetTimeInfo()
            timeInfo = internetInfo
        } catch (_: Exception) {
            // Graceful silent fallback to local device time
        }
    }

    // Fixed 4 signature Claude starter templates
    val starterTemplates = remember { getClaudeTemplates() }

    val greetingName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Voyager"

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("triton_home_screen")
    ) {
        // Scrollable Top & Center Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Claude Asterisk Radiant Starburst Emblem
            ClaudeAsterisk(
                size = 52.dp,
                modifier = Modifier.testTag("claude_asterisk_logo")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Time-of-Day Greeting (background-verified)
            Text(
                text = "${timeInfo.greeting}, $greetingName",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = CinzelFontFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "How can Triton help you today?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = JakartaFontFamily,
                    fontSize = 15.sp,
                    letterSpacing = 0.15.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 4 Starter Cards (2x2 Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                starterTemplates.chunked(2).forEach { rowTemplates ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowTemplates.forEach { template ->
                            ClaudeTemplateCard(
                                template = template,
                                onSelect = {
                                    // Populate input box so user can customize or send
                                    inputText = template.prompt
                                },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Recent Conversations Section (if any)
            if (recentSessions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(26.dp))
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
                                .padding(vertical = 3.dp)
                                .testTag("recent_session_${session.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TritonTridentGlyph(
                                        size = 14.dp,
                                        tint = GoldPrimary
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

            // Footer disclaimer
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Triton 3.7 Sonnet · Aureate Intelligence · AI responses may require verification.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JakartaFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom-Anchored Prompt Input Box (Claude-style docked bottom composer)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("home_bottom_composer_container"),
            contentAlignment = Alignment.Center
        ) {
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
                    placeholder = "How can Triton help you today?"
                )
            }
        }
    }
}

data class ClaudeTemplate(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val prompt: String,
    val tag: String
)

@Composable
private fun ClaudeTemplateCard(
    template: ClaudeTemplate,
    onSelect: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.3f else 0.45f)
        ),
        modifier = modifier.testTag("claude_template_${template.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldPrimary.copy(alpha = if (isDark) 0.16f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = template.icon,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = template.tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = template.title,
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
                text = template.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JakartaFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action row with tap-to-use prompt cue
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Use template",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = GoldPrimary
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Use template",
                    tint = GoldPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * The 4 signature starter templates.
 */
private fun getClaudeTemplates(): List<ClaudeTemplate> {
    return listOf(
        ClaudeTemplate(
            id = "write_essay",
            icon = Icons.Default.EditNote,
            title = "Write an essay",
            description = "Draft a structured, persuasive essay with thesis and arguments",
            prompt = "Write a comprehensive, compelling essay on [topic], structured with an engaging introduction, clear thesis, well-substantiated arguments with counterpoints, and an insightful conclusion.",
            tag = "Essay"
        ),
        ClaudeTemplate(
            id = "do_research",
            icon = Icons.Default.Search,
            title = "Do some research",
            description = "Conduct deep synthesis and literature review on cutting-edge domains",
            prompt = "Conduct a detailed research breakdown on [subject], highlighting foundational principles, recent breakthroughs, technological trade-offs, and future trajectories.",
            tag = "Research"
        ),
        ClaudeTemplate(
            id = "create_code",
            icon = Icons.Default.Code,
            title = "Create some code",
            description = "Generate clean, production-grade code with error handling & tests",
            prompt = "Write clean, idiomatic, production-ready code in Kotlin Jetpack Compose for [feature], including proper state hoisting, M3 styling, and clean architecture.",
            tag = "Code"
        ),
        ClaudeTemplate(
            id = "brainstorm_ideas",
            icon = Icons.Default.Lightbulb,
            title = "Brainstorm ideas",
            description = "Synthesize innovative concepts, strategic roadmaps, and solutions",
            prompt = "Brainstorm 5 creative, high-impact approaches to [problem or objective], categorized by technical feasibility, unique value, and strategic advantage.",
            tag = "Ideas"
        )
    )
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

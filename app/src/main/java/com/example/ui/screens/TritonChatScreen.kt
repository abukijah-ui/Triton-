package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.MessageFeedback
import com.example.model.Role
import com.example.model.TritonArtifact
import com.example.model.TritonModel
import com.example.ui.components.TritonComposer
import com.example.ui.components.TritonMarkdownText
import com.example.ui.components.TritonThinkingBlock
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldBorderLight
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.goldShimmerBrush

@Composable
fun TritonChatScreen(
    messages: List<ChatMessage>,
    selectedModel: TritonModel,
    isThinkingEnabled: Boolean,
    onToggleThinking: () -> Unit,
    onSendMessage: (String) -> Unit,
    isGenerating: Boolean,
    onToggleMessageThinking: (String) -> Unit,
    onSetMessageFeedback: (String, MessageFeedback) -> Unit,
    onOpenArtifact: (TritonArtifact) -> Unit,
    modifier: Modifier = Modifier
) {
    var composerText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when messages update
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("triton_chat_screen")
    ) {
        // Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            items(messages, key = { it.id }) { message ->
                if (message.role == Role.USER) {
                    UserMessageBubble(message = message)
                } else {
                    AssistantMessageBubble(
                        message = message,
                        onToggleThinking = { onToggleMessageThinking(message.id) },
                        onSetFeedback = { fb -> onSetMessageFeedback(message.id, fb) },
                        onOpenArtifact = onOpenArtifact
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (isGenerating) {
                item {
                    GeneratingIndicator()
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Bottom Elevated Composer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding()
        ) {
            TritonComposer(
                text = composerText,
                onTextChanged = { composerText = it },
                onSendMessage = {
                    onSendMessage(it)
                    composerText = ""
                },
                isGenerating = isGenerating,
                isThinkingEnabled = isThinkingEnabled,
                onToggleThinking = onToggleThinking,
                selectedModel = selectedModel,
                placeholder = "Reply to Triton..."
            )
        }
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessage) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = androidx.compose.foundation.BorderStroke(
                0.8.dp,
                GoldPrimary.copy(alpha = 0.25f)
            ),
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .testTag("user_message_${message.id}")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 15.sp,
                        lineHeight = 23.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun AssistantMessageBubble(
    message: ChatMessage,
    onToggleThinking: () -> Unit,
    onSetFeedback: (MessageFeedback) -> Unit,
    onOpenArtifact: (TritonArtifact) -> Unit
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("assistant_message_${message.id}")
    ) {
        // Model & Brand Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            // Golden trident avatar
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.15f))
                    .border(1.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔱", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message.modelName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = CinzelFontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = GoldPrimary
            )
        }

        // Thinking foldout if available (Claude 3.7 signature)
        if (!message.thoughtProcess.isNullOrBlank()) {
            TritonThinkingBlock(
                thoughtProcess = message.thoughtProcess,
                thoughtDurationSec = message.thoughtDurationSec,
                isExpanded = message.isThinkingExpanded,
                onToggle = onToggleThinking,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        // Markdown Formatted Message Body
        TritonMarkdownText(
            text = message.content,
            onOpenArtifact = {
                if (message.artifact != null) {
                    onOpenArtifact(message.artifact)
                }
            }
        )

        // Triton Artifact Card if present
        if (message.artifact != null) {
            Spacer(modifier = Modifier.height(10.dp))
            TritonArtifactCard(
                artifact = message.artifact,
                onClick = { onOpenArtifact(message.artifact) }
            )
        }

        // Action Toolbar: Copy, Feedback
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("triton_response", message.content)
                    clipboard.setPrimaryClip(clip)
                    isCopied = true
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy message",
                    tint = if (isCopied) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = {
                    onSetFeedback(if (message.feedback == MessageFeedback.THUMBS_UP) MessageFeedback.NONE else MessageFeedback.THUMBS_UP)
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = if (message.feedback == MessageFeedback.THUMBS_UP) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = "Thumbs up",
                    tint = if (message.feedback == MessageFeedback.THUMBS_UP) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = {
                    onSetFeedback(if (message.feedback == MessageFeedback.THUMBS_DOWN) MessageFeedback.NONE else MessageFeedback.THUMBS_DOWN)
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = if (message.feedback == MessageFeedback.THUMBS_DOWN) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                    contentDescription = "Thumbs down",
                    tint = if (message.feedback == MessageFeedback.THUMBS_DOWN) GoldAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun TritonArtifactCard(
    artifact: TritonArtifact,
    onClick: () -> Unit
) {
    val shimmerBrush = goldShimmerBrush()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, shimmerBrush),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("artifact_card_${artifact.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary.copy(alpha = 0.15f))
                    .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TRITON ARTIFACT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JakartaFontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp
                        ),
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•  ${artifact.language.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = artifact.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = CinzelFontFamily,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = artifact.summary,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "View",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 11.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    color = GoldAccent
                )
            }
        }
    }
}

@Composable
private fun GeneratingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.15f))
                .border(1.dp, GoldPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🔱", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = GoldPrimary,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Triton is thinking...",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = JakartaFontFamily,
                fontSize = 12.sp
            ),
            color = GoldPrimary
        )
    }
}

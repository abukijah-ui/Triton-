package com.example.model

enum class Role {
    USER, ASSISTANT, SYSTEM
}

enum class ArtifactType {
    CODE, MARKDOWN, HTML, SVG, DIAGRAM
}

data class TritonArtifact(
    val id: String,
    val title: String,
    val type: ArtifactType,
    val language: String,
    val content: String,
    val summary: String
)

data class ChatAttachment(
    val id: String,
    val name: String,
    val sizeFormatted: String,
    val mimeType: String
)

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val thoughtProcess: String? = null,
    val thoughtDurationSec: Double? = null,
    val isThinkingExpanded: Boolean = false,
    val artifact: TritonArtifact? = null,
    val modelName: String = "Triton 3.7 Sonnet",
    val isStreaming: Boolean = false,
    val attachments: List<ChatAttachment> = emptyList(),
    val feedback: MessageFeedback = MessageFeedback.NONE
)

enum class MessageFeedback {
    NONE, THUMBS_UP, THUMBS_DOWN
}

enum class TritonModel(
    val id: String,
    val displayName: String,
    val shortName: String,
    val description: String,
    val supportsThinking: Boolean,
    val tag: String
) {
    TRITON_3_7_SONNET(
        id = "triton-3-7-sonnet",
        displayName = "Triton 3.7 Sonnet",
        shortName = "3.7 Sonnet",
        description = "Hybrid reasoning, advanced coding & deep analysis",
        supportsThinking = true,
        tag = "Latest"
    ),
    TRITON_3_5_SONNET(
        id = "triton-3-5-sonnet",
        displayName = "Triton 3.5 Sonnet",
        shortName = "3.5 Sonnet",
        description = "Most versatile model for general intellect",
        supportsThinking = true,
        tag = "Recommended"
    ),
    TRITON_3_5_HAIKU(
        id = "triton-3-5-haiku",
        displayName = "Triton 3.5 Haiku",
        shortName = "3.5 Haiku",
        description = "Instant responses for lightweight chats & triage",
        supportsThinking = false,
        tag = "Fast"
    ),
    TRITON_3_5_OPUS(
        id = "triton-3-5-opus",
        displayName = "Triton 3.5 Opus",
        shortName = "3.5 Opus",
        description = "Profound depth, philosophy & creative literature",
        supportsThinking = false,
        tag = "Deep"
    )
}

data class ChatSession(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val previewSnippet: String = "",
    val isPinned: Boolean = false,
    val model: TritonModel = TritonModel.TRITON_3_7_SONNET,
    val projectId: String? = null
)

data class TritonProject(
    val id: String,
    val name: String,
    val description: String,
    val instructions: String,
    val fileCount: Int,
    val iconEmoji: String = "⚡"
)

data class PromptSuggestion(
    val id: String,
    val category: String,
    val title: String,
    val promptText: String
)

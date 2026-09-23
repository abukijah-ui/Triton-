package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_sessions",
    indices = [Index(value = ["updatedAt"]), Index(value = ["userId"])]
)
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val previewSnippet: String,
    val modelId: String,
    val isPinned: Boolean = false,
    val userId: String? = null
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"]), Index(value = ["timestamp"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val role: String, // "USER" or "ASSISTANT"
    val content: String,
    val timestamp: Long,
    val modelName: String,
    val thoughtProcess: String? = null,
    val thoughtDurationSec: Double? = null,
    val isThinkingExpanded: Boolean = false,
    val hasArtifact: Boolean = false,
    val artifactId: String? = null,
    val artifactTitle: String? = null,
    val artifactLanguage: String? = null,
    val artifactContent: String? = null,
    val artifactType: String? = null,
    val artifactSummary: String? = null,
    val feedback: String = "NONE" // "NONE", "THUMBS_UP", "THUMBS_DOWN"
)

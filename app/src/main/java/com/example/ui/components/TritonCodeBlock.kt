package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInFull
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBgDark
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxFunction
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxString
import kotlinx.coroutines.delay

@Composable
fun TritonCodeBlock(
    code: String,
    language: String = "kotlin",
    onOpenArtifact: (() -> Unit)? = null,
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                GoldPrimary.copy(alpha = 0.25f),
                RoundedCornerShape(12.dp)
            )
            .testTag("code_block"),
        color = CodeBgDark,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar: Language, Open in Artifact, Copy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E24))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(GoldAccent)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = language.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = GoldAccent
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onOpenArtifact != null) {
                        Surface(
                            onClick = onOpenArtifact,
                            shape = RoundedCornerShape(6.dp),
                            color = GoldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.testTag("open_artifact_code_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "Artifact",
                                    tint = GoldHighlight,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Artifact",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 11.sp
                                    ),
                                    color = GoldHighlight
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("code", code)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent,
                        modifier = Modifier.testTag("copy_code_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = if (isCopied) "Copied" else "Copy code",
                                tint = if (isCopied) Color(0xFF4CAF50) else Color(0xFFA6A29A),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCopied) "Copied" else "Copy",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 11.sp
                                ),
                                color = if (isCopied) Color(0xFF4CAF50) else Color(0xFFA6A29A)
                            )
                        }
                    }
                }
            }

            // Code Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = buildSyntaxHighlightedCode(code, language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )
                )
            }
        }
    }
}

private fun buildSyntaxHighlightedCode(code: String, language: String) = buildAnnotatedString {
    val lines = code.lines()
    val keywords = setOf(
        "fun", "val", "var", "class", "data", "object", "interface", "import", "package",
        "return", "if", "else", "when", "for", "while", "true", "false", "null", "suspend",
        "override", "public", "private", "protected", "uniform", "precision", "highp", "float",
        "vec2", "vec3", "vec4", "void", "main", "const", "def", "async", "await", "from"
    )

    lines.forEachIndexed { index, line ->
        val trimmed = line.trimStart()
        if (trimmed.startsWith("//") || trimmed.startsWith("#")) {
            withStyle(SpanStyle(color = SyntaxComment)) {
                append(line)
            }
        } else {
            val tokens = line.split(Regex("(?<=[\\s(),;=+\\-*/\\[\\]{}])|(?=[\\s(),;=+\\-*/\\[\\]{}])"))
            tokens.forEach { token ->
                when {
                    keywords.contains(token) -> {
                        withStyle(SpanStyle(color = SyntaxKeyword)) {
                            append(token)
                        }
                    }
                    token.startsWith("\"") && token.endsWith("\"") -> {
                        withStyle(SpanStyle(color = SyntaxString)) {
                            append(token)
                        }
                    }
                    token.matches(Regex("\\b[0-9]+(\\.[0-9]+)?[fF]?\\b")) -> {
                        withStyle(SpanStyle(color = SyntaxNumber)) {
                            append(token)
                        }
                    }
                    token.matches(Regex("[A-Z][a-zA-Z0-9_]+")) -> {
                        withStyle(SpanStyle(color = GoldHighlight)) {
                            append(token)
                        }
                    }
                    token.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*(?=\\()")) -> {
                        withStyle(SpanStyle(color = SyntaxFunction)) {
                            append(token)
                        }
                    }
                    else -> {
                        withStyle(SpanStyle(color = Color(0xFFD6D4CE))) {
                            append(token)
                        }
                    }
                }
            }
        }
        if (index < lines.size - 1) {
            append("\n")
        }
    }
}

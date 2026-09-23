package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun TritonMarkdownText(
    text: String,
    onOpenArtifact: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val blocks = parseMarkdownBlocks(text)

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineLarge.copy(fontSize = 20.sp)
                        2 -> MaterialTheme.typography.headlineMedium.copy(fontSize = 17.sp)
                        else -> MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = block.text,
                        style = style.copy(
                            fontFamily = CinzelFontFamily,
                            color = GoldAccent
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                is MarkdownBlock.CodeBlock -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    TritonCodeBlock(
                        code = block.code,
                        language = block.language,
                        onOpenArtifact = onOpenArtifact
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JakartaFontFamily,
                                fontSize = 14.5.sp,
                                lineHeight = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            ),
                            color = GoldPrimary,
                            modifier = Modifier.width(22.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JakartaFontFamily,
                                fontSize = 14.5.sp,
                                lineHeight = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    if (block.text.isNotBlank()) {
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JakartaFontFamily,
                                fontSize = 14.5.sp,
                                lineHeight = 23.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

private fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Check for code block ```
        if (line.trim().startsWith("```")) {
            val language = line.trim().removePrefix("```").trim().ifBlank { "kotlin" }
            val codeBuilder = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeBuilder.append(lines[i]).append("\n")
                i++
            }
            // Skip the closing ```
            i++
            blocks.add(MarkdownBlock.CodeBlock(language, codeBuilder.toString().trimEnd()))
            continue
        }

        // Headers
        if (line.startsWith("### ")) {
            blocks.add(MarkdownBlock.Header(3, line.removePrefix("### ")))
            i++
            continue
        } else if (line.startsWith("## ")) {
            blocks.add(MarkdownBlock.Header(2, line.removePrefix("## ")))
            i++
            continue
        } else if (line.startsWith("# ")) {
            blocks.add(MarkdownBlock.Header(1, line.removePrefix("# ")))
            i++
            continue
        }

        // Bullets (* or -)
        val trimmed = line.trimStart()
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ")) {
            blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2)))
            i++
            continue
        }

        // Numbered list
        val numMatch = Regex("^(\\d+)\\.\\s+(.*)").find(trimmed)
        if (numMatch != null) {
            val num = numMatch.groupValues[1].toIntOrNull() ?: 1
            val content = numMatch.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(num, content))
            i++
            continue
        }

        // Normal paragraph
        blocks.add(MarkdownBlock.Paragraph(line))
        i++
    }

    return blocks
}

private fun parseInlineMarkdown(text: String) = buildAnnotatedString {
    var cursor = 0
    while (cursor < text.length) {
        // Bold: **text**
        val boldStart = text.indexOf("**", cursor)
        val codeStart = text.indexOf("`", cursor)

        val nextSpecial = listOfNotNull(
            if (boldStart != -1) boldStart else null,
            if (codeStart != -1) codeStart else null
        ).minOrNull()

        if (nextSpecial == null) {
            append(text.substring(cursor))
            break
        }

        if (nextSpecial > cursor) {
            append(text.substring(cursor, nextSpecial))
            cursor = nextSpecial
        }

        if (boldStart == nextSpecial) {
            val boldEnd = text.indexOf("**", boldStart + 2)
            if (boldEnd != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = GoldPrimary)) {
                    append(text.substring(boldStart + 2, boldEnd))
                }
                cursor = boldEnd + 2
            } else {
                append("**")
                cursor = boldStart + 2
            }
        } else if (codeStart == nextSpecial) {
            val codeEnd = text.indexOf("`", codeStart + 1)
            if (codeEnd != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.5.sp,
                        color = GoldAccent
                    )
                ) {
                    append(" ${text.substring(codeStart + 1, codeEnd)} ")
                }
                cursor = codeEnd + 1
            } else {
                append("`")
                cursor = codeStart + 1
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainerLight
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JakartaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalIsDarkTheme

@Composable
fun TritonMarkdownText(
    text: String,
    onOpenArtifact: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val blocks = parseMarkdownBlocks(text)

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val (style, topSpace, color) = when (block.level) {
                        1 -> Triple(
                            MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 21.sp,
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            14.dp,
                            GoldPrimary
                        )
                        2 -> Triple(
                            MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 18.sp,
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            12.dp,
                            GoldAccent
                        )
                        3 -> Triple(
                            MaterialTheme.typography.titleLarge.copy(
                                fontSize = 16.sp,
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            10.dp,
                            MaterialTheme.colorScheme.onSurface
                        )
                        else -> Triple(
                            MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.5.sp,
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            8.dp,
                            GoldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(topSpace))
                    Text(
                        text = block.text,
                        style = style,
                        color = color
                    )
                    Spacer(modifier = Modifier.height(5.dp))
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

                is MarkdownBlock.Table -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    MarkdownTableView(
                        headers = block.headers,
                        alignments = block.alignments,
                        rows = block.rows,
                        isDark = isDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                is MarkdownBlock.TaskItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.level * 16).dp, top = 3.dp, bottom = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (block.isChecked) GoldPrimary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.2.dp,
                                    if (block.isChecked) GoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                    RoundedCornerShape(5.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (block.isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JakartaFontFamily,
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            ),
                            color = if (block.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.level * 16).dp, top = 3.dp, bottom = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
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
                            .padding(start = (block.level * 16).dp, top = 3.dp, bottom = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
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

                is MarkdownBlock.Blockquote -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.5.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(GoldPrimary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = GoldPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = parseInlineMarkdown(block.text),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JakartaFontFamily,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 13.5.sp,
                                    lineHeight = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                is MarkdownBlock.HorizontalRule -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = GoldPrimary.copy(alpha = 0.25f),
                            thickness = 1.dp
                        )
                        Text(
                            text = " ◆ ",
                            fontSize = 10.sp,
                            color = GoldPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = GoldPrimary.copy(alpha = 0.25f),
                            thickness = 1.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
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

@Composable
private fun MarkdownTableView(
    headers: List<String>,
    alignments: List<TextAlign>,
    rows: List<List<String>>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val borderColor = if (isDark) GoldPrimary.copy(alpha = 0.28f) else GoldPrimary.copy(alpha = 0.38f)
    val headerBg = if (isDark) GoldPrimary.copy(alpha = 0.15f) else GoldContainerLight.copy(alpha = 0.45f)

    Surface(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, borderColor),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
    ) {
        Box(modifier = Modifier.horizontalScroll(scrollState)) {
            Column {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .background(headerBg)
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    headers.forEachIndexed { colIdx, header ->
                        val align = alignments.getOrElse(colIdx) { TextAlign.Start }
                        Box(
                            modifier = Modifier
                                .widthIn(min = 120.dp, max = 240.dp)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = parseInlineMarkdown(header),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = align
                                ),
                                color = if (isDark) GoldAccent else GoldPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                HorizontalDivider(color = borderColor, thickness = 1.dp)

                // Table Data Rows
                rows.forEachIndexed { rowIdx, rowCells ->
                    val isEven = rowIdx % 2 == 0
                    val rowBg = if (isEven) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.25f else 0.35f)

                    Row(
                        modifier = Modifier
                            .background(rowBg)
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        headers.indices.forEach { colIdx ->
                            val cellText = rowCells.getOrElse(colIdx) { "" }
                            val align = alignments.getOrElse(colIdx) { TextAlign.Start }
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 120.dp, max = 240.dp)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = parseInlineMarkdown(cellText),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        textAlign = align
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (rowIdx < rows.lastIndex) {
                        HorizontalDivider(
                            color = borderColor.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class Table(val headers: List<String>, val alignments: List<TextAlign>, val rows: List<List<String>>) : MarkdownBlock()
    data class TaskItem(val level: Int, val isChecked: Boolean, val text: String) : MarkdownBlock()
    data class BulletItem(val level: Int, val text: String) : MarkdownBlock()
    data class NumberedItem(val level: Int, val number: Int, val text: String) : MarkdownBlock()
    data class Blockquote(val text: String) : MarkdownBlock()
    object HorizontalRule : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        // 1. Code Block ```
        if (trimmed.startsWith("```")) {
            val language = trimmed.removePrefix("```").trim().ifBlank { "kotlin" }
            val codeBuilder = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeBuilder.append(lines[i]).append("\n")
                i++
            }
            // Skip the closing ```
            if (i < lines.size) i++
            blocks.add(MarkdownBlock.CodeBlock(language, codeBuilder.toString().trimEnd()))
            continue
        }

        // 2. Table detection: current line has '|' and next line is separator '| --- |'
        if (isTableRow(line) && i + 1 < lines.size && isTableDivider(lines[i + 1])) {
            val headers = parseTableRow(line)
            val alignments = parseAlignments(lines[i + 1])
            i += 2
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && isTableRow(lines[i]) && !isTableDivider(lines[i])) {
                rows.add(parseTableRow(lines[i]))
                i++
            }
            blocks.add(MarkdownBlock.Table(headers, alignments, rows))
            continue
        }

        // 3. Blockquotes >
        if (trimmed.startsWith(">")) {
            val quoteBuilder = StringBuilder()
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                quoteBuilder.append(lines[i].trim().removePrefix(">").trim()).append(" ")
                i++
            }
            blocks.add(MarkdownBlock.Blockquote(quoteBuilder.toString().trim()))
            continue
        }

        // 4. Horizontal Rules
        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            blocks.add(MarkdownBlock.HorizontalRule)
            i++
            continue
        }

        // 5. Headers #
        if (line.startsWith("###### ")) {
            blocks.add(MarkdownBlock.Header(6, line.removePrefix("###### ")))
            i++
            continue
        } else if (line.startsWith("##### ")) {
            blocks.add(MarkdownBlock.Header(5, line.removePrefix("##### ")))
            i++
            continue
        } else if (line.startsWith("#### ")) {
            blocks.add(MarkdownBlock.Header(4, line.removePrefix("#### ")))
            i++
            continue
        } else if (line.startsWith("### ")) {
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

        // Indentation level calculation
        val indentSpaces = line.takeWhile { it == ' ' }.length
        val indentLevel = indentSpaces / 2

        // 6. Task lists: - [ ] or - [x]
        if (trimmed.startsWith("- [ ] ") || trimmed.startsWith("* [ ] ")) {
            blocks.add(MarkdownBlock.TaskItem(indentLevel, false, trimmed.substring(6)))
            i++
            continue
        } else if (trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") ||
                   trimmed.startsWith("* [x] ") || trimmed.startsWith("* [X] ")) {
            blocks.add(MarkdownBlock.TaskItem(indentLevel, true, trimmed.substring(6)))
            i++
            continue
        }

        // 7. Bullets: - or * or +
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ")) {
            blocks.add(MarkdownBlock.BulletItem(indentLevel, trimmed.substring(2)))
            i++
            continue
        }

        // 8. Numbered list: 1. item
        val numMatch = Regex("^(\\d+)\\.\\s+(.*)").find(trimmed)
        if (numMatch != null) {
            val num = numMatch.groupValues[1].toIntOrNull() ?: 1
            val content = numMatch.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(indentLevel, num, content))
            i++
            continue
        }

        // 9. Normal paragraph
        blocks.add(MarkdownBlock.Paragraph(line))
        i++
    }

    return blocks
}

private fun isTableRow(line: String): Boolean {
    val trimmed = line.trim()
    return trimmed.contains("|") && (trimmed.startsWith("|") || trimmed.count { it == '|' } >= 2)
}

private fun isTableDivider(line: String): Boolean {
    val trimmed = line.trim()
    if (!trimmed.contains("-") || !trimmed.contains("|")) return false
    val parts = trimmed.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    return parts.isNotEmpty() && parts.all { part ->
        val cleaned = part.replace(":", "")
        cleaned.isNotEmpty() && cleaned.all { it == '-' }
    }
}

private fun parseTableRow(line: String): List<String> {
    val trimmed = line.trim()
    val noLeading = if (trimmed.startsWith("|")) trimmed.substring(1) else trimmed
    val noTrailing = if (noLeading.endsWith("|")) noLeading.substring(0, noLeading.length - 1) else noLeading
    return noTrailing.split("|").map { it.trim() }
}

private fun parseAlignments(line: String): List<TextAlign> {
    val cells = parseTableRow(line)
    return cells.map { cell ->
        val c = cell.trim()
        val startsWithColon = c.startsWith(":")
        val endsWithColon = c.endsWith(":")
        when {
            startsWithColon && endsWithColon -> TextAlign.Center
            endsWithColon -> TextAlign.End
            else -> TextAlign.Start
        }
    }
}

fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        // Match bold, strikethrough, inline code, links, and italics
        val tokenRegex = Regex("(\\*\\*.*?\\*\\*|~~.*?~~|`.*?`|\\[.*?\\]\\(.*?\\)|\\*.*?\\*|_.*?_)")
        val matches = tokenRegex.findAll(text).toList()

        for (match in matches) {
            val range = match.range
            if (range.first > cursor) {
                append(text.substring(cursor, range.first))
            }
            val token = match.value
            when {
                token.startsWith("**") && token.endsWith("**") && token.length >= 4 -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = GoldPrimary)) {
                        append(token.substring(2, token.length - 2))
                    }
                }
                token.startsWith("~~") && token.endsWith("~~") && token.length >= 4 -> {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = Color.Gray)) {
                        append(token.substring(2, token.length - 2))
                    }
                }
                token.startsWith("`") && token.endsWith("`") && token.length >= 2 -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = GoldAccent,
                            background = GoldPrimary.copy(alpha = 0.12f)
                        )
                    ) {
                        append(" ${token.substring(1, token.length - 1)} ")
                    }
                }
                token.startsWith("[") && token.contains("](") && token.endsWith(")") -> {
                    val labelEnd = token.indexOf("](")
                    val label = token.substring(1, labelEnd)
                    withStyle(SpanStyle(color = GoldAccent, textDecoration = TextDecoration.Underline)) {
                        append(label)
                    }
                }
                (token.startsWith("*") && token.endsWith("*") && token.length >= 2) ||
                (token.startsWith("_") && token.endsWith("_") && token.length >= 2) -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(token.substring(1, token.length - 1))
                    }
                }
                else -> append(token)
            }
            cursor = range.last + 1
        }

        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}

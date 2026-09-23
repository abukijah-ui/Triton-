package com.example.ui.components.artifact

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlinx.coroutines.delay

/**
 * Modular Code Viewer with line numbers, syntax highlighting, wrap toggle, and font scaling.
 */
@Composable
fun ArtifactCodeViewer(
    code: String,
    language: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    var isWordWrap by remember { mutableStateOf(false) }
    var fontSizeSp by remember { mutableFloatStateOf(12.5f) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    val lines = remember(code) { code.lines() }
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("artifact_code_viewer")
    ) {
        // Code Toolbar Controls
        Surface(
            color = if (isDark) Color(0xFF141518) else Color(0xFFEBECEF),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GoldPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(0.6.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = language.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontFamily = JetBrainsMonoFontFamily,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${lines.size} lines · ${code.length} chars",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Font Scale Button
                    IconButton(
                        onClick = {
                            fontSizeSp = if (fontSizeSp >= 15f) 11f else fontSizeSp + 1.5f
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Change font size",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Wrap Text Toggle Button
                    IconButton(
                        onClick = { isWordWrap = !isWordWrap },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.WrapText,
                            contentDescription = "Toggle wrap",
                            tint = if (isWordWrap) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Copy Code Button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("code", code)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (isCopied) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Code Editor Box with Line Numbers
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF0F1012) else Color(0xFFF7F8FA))
        ) {
            val hModifier = if (isWordWrap) Modifier.fillMaxWidth() else Modifier.horizontalScroll(horizontalScrollState)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
                    .padding(vertical = 12.dp)
            ) {
                // Line Number Column
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.forEachIndexed { index, _ ->
                        Text(
                            text = "${index + 1}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.5f).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    }
                }

                // Vertical Divider Line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height((lines.size * fontSizeSp * 1.5f).dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Syntax-Highlighted Code Text
                Box(modifier = hModifier.padding(end = 16.dp)) {
                    Column {
                        lines.forEach { line ->
                            Text(
                                text = highlightSyntax(line, language, isDark),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp * 1.5f).sp,
                                color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun highlightSyntax(
    line: String,
    language: String,
    isDark: Boolean
): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        append(line)

        // Keywords
        val keywords = listOf(
            "val", "var", "fun", "class", "data", "object", "interface", "enum", "sealed",
            "import", "package", "return", "if", "else", "when", "for", "while", "true", "false",
            "null", "private", "public", "protected", "override", "const", "uniform", "precision",
            "vec2", "vec3", "vec4", "mat4", "float", "int", "void", "main", "out", "in", "inout",
            "def", "import", "from", "as", "const", "let", "function", "async", "await", "export"
        )

        keywords.forEach { kw ->
            val regex = Regex("\\b$kw\\b")
            regex.findAll(line).forEach { match ->
                addStyle(
                    SpanStyle(
                        color = if (isDark) GoldAccent else GoldPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // Comments
        val commentIdx = line.indexOf("//")
        if (commentIdx != -1) {
            addStyle(
                SpanStyle(
                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                commentIdx,
                line.length
            )
        }

        // Strings
        val strRegex = Regex("\"[^\"]*\"")
        strRegex.findAll(line).forEach { match ->
            addStyle(
                SpanStyle(color = if (isDark) Color(0xFF86EFAC) else Color(0xFF15803D)),
                match.range.first,
                match.range.last + 1
            )
        }

        // Numbers
        val numRegex = Regex("\\b\\d+(\\.\\d+)?f?\\b")
        numRegex.findAll(line).forEach { match ->
            addStyle(
                SpanStyle(color = if (isDark) GoldAmber else Color(0xFFD97706)),
                match.range.first,
                match.range.last + 1
            )
        }
    }
}

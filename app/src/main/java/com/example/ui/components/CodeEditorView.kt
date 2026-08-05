package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodeFileEntity
import com.example.engine.CodeSyntaxHighlighter
import com.example.ui.theme.IdeErrorRed
import com.example.ui.theme.IdePrimarySky

@Composable
fun CodeEditorView(
    activeFile: CodeFileEntity?,
    errorLines: Set<Int> = emptySet(),
    onContentChange: (String) -> Unit,
    onCopyCode: () -> Unit,
    onClearCode: () -> Unit
) {
    if (activeFile == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "</>",
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "لا يوجد ملف مفتوح",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "اختر ملفاً من شجرة الملفات للبدء في كتابة الكود.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        return
    }

    var isSyntaxHighlightingEnabled by remember { mutableStateOf(true) }
    val content = activeFile.content
    val lineCount = remember(content) { content.lines().size.coerceAtLeast(1) }

    // Syntax Highlighting Visual Transformation
    val syntaxTransformation = remember(activeFile.language, isSyntaxHighlightingEnabled) {
        VisualTransformation { text ->
            val annotated = CodeSyntaxHighlighter.highlight(
                code = text.text,
                language = activeFile.language,
                isEnabled = isSyntaxHighlightingEnabled
            )
            TransformedText(annotated, OffsetMapping.Identity)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Quick Code Symbol & Tools Helper Bar
        QuickSnippetBar(
            language = activeFile.language,
            isSyntaxHighlightingEnabled = isSyntaxHighlightingEnabled,
            onToggleSyntaxHighlighting = { isSyntaxHighlightingEnabled = !isSyntaxHighlightingEnabled },
            onInsertSnippet = { snippet ->
                onContentChange(content + snippet)
            }
        )

        // Main Editor Surface with Line Numbers & Code Area
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val verticalScrollState = rememberScrollState()

            // Line Numbers Bar with Error Indicators
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .verticalScroll(verticalScrollState)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..lineCount) {
                    val hasError = errorLines.contains(i)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp)
                    ) {
                        if (hasError) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(IdeErrorRed)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = "$i",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (hasError) FontWeight.Bold else FontWeight.Normal,
                            color = if (hasError) IdeErrorRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Editable Code Canvas with Syntax Highlighting
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(verticalScrollState)
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = content,
                    onValueChange = onContentChange,
                    visualTransformation = syntaxTransformation,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(IdePrimarySky),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("code_editor_text_field")
                )

                if (content.isEmpty()) {
                    Text(
                        text = "الصق كودك هنا أو ابدأ الكتابة...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickSnippetBar(
    language: String,
    isSyntaxHighlightingEnabled: Boolean,
    onToggleSyntaxHighlighting: () -> Unit,
    onInsertSnippet: (String) -> Unit
) {
    val snippets = remember(language) {
        when {
            language.contains("python") -> listOf(
                "def ", "import ", "class ", "if ", "for ", "print()", "=", ":", "(", ")", "[", "]", "{", "}", "\"", "'", "#"
            )
            language.contains("javascript") || language.contains("html") -> listOf(
                "function ", "const ", "let ", "if ", "console.log()", "=>", "==", "=", "{", "}", "(", ")", ";", "\"", "'", "<", ">"
            )
            else -> listOf(
                "if ", "for ", "class ", "=", "{", "}", "(", ")", ";", ":", "\"", "'", "<", ">", "//"
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Toggle Syntax Highlighting Button
        Surface(
            onClick = onToggleSyntaxHighlighting,
            shape = RoundedCornerShape(6.dp),
            color = if (isSyntaxHighlightingEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .height(26.dp)
                .testTag("toggle_syntax_highlighting_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "تلوين الكود",
                    tint = if (isSyntaxHighlightingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSyntaxHighlightingEnabled) "تلوين مفعّل" else "تلوين موقف",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSyntaxHighlightingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        VerticalDivider(modifier = Modifier.height(16.dp), thickness = 0.5.dp)

        // Snippets
        snippets.forEach { snippet ->
            Surface(
                onClick = { onInsertSnippet(snippet) },
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.height(26.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = snippet,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

package com.example.engine

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

object CodeSyntaxHighlighter {

    // Palette for Syntax Highlighting
    private val KeywordColor = Color(0xFFC678DD) // Purple / Magenta
    private val StringColor = Color(0xFF98C379)  // Soft Green
    private val NumberColor = Color(0xFFD19A66)  // Amber / Orange
    private val CommentColor = Color(0xFF7F848E) // Muted Gray
    private val TagColor = Color(0xFFE06C75)     // Red / Coral
    private val AttrColor = Color(0xFFD19A66)    // Orange
    private val OperatorColor = Color(0xFF56B6C2)// Cyan

    fun highlight(code: String, language: String, isEnabled: Boolean): AnnotatedString {
        if (!isEnabled || code.isEmpty()) {
            return AnnotatedString(code)
        }

        val langLower = language.lowercase()
        return buildAnnotatedString {
            append(code)

            when {
                langLower.contains("python") -> highlightPython(code)
                langLower.contains("html") || langLower.contains("javascript") || langLower.contains("js") -> highlightWeb(code)
                langLower.contains("kotlin") || langLower.contains("java") -> highlightKotlinJava(code)
                langLower.contains("cpp") || langLower.contains("c") -> highlightCpp(code)
                else -> highlightGeneric(code)
            }
        }
    }

    private fun AnnotatedString.Builder.highlightPython(code: String) {
        val keywords = setOf(
            "def", "class", "import", "from", "return", "if", "else", "elif",
            "for", "in", "while", "try", "except", "finally", "with", "as",
            "True", "False", "None", "lambda", "pass", "break", "continue",
            "raise", "global", "yield", "async", "await", "and", "or", "not", "is"
        )
        highlightTokens(code, keywords)
        highlightComments(code, listOf("#"))
        highlightStrings(code)
        highlightNumbers(code)
    }

    private fun AnnotatedString.Builder.highlightWeb(code: String) {
        val keywords = setOf(
            "function", "const", "let", "var", "return", "if", "else", "for",
            "while", "class", "import", "export", "from", "async", "await",
            "try", "catch", "finally", "new", "this", "typeof", "null",
            "undefined", "true", "false", "document", "window", "console"
        )
        highlightTokens(code, keywords)
        highlightComments(code, listOf("//", "/*"))
        highlightStrings(code)
        highlightNumbers(code)
        highlightHtmlTags(code)
    }

    private fun AnnotatedString.Builder.highlightKotlinJava(code: String) {
        val keywords = setOf(
            "fun", "val", "var", "class", "interface", "object", "package",
            "import", "return", "if", "else", "for", "when", "while", "override",
            "public", "private", "protected", "internal", "data", "sealed",
            "null", "true", "false", "this", "super", "try", "catch"
        )
        highlightTokens(code, keywords)
        highlightComments(code, listOf("//", "/*"))
        highlightStrings(code)
        highlightNumbers(code)
    }

    private fun AnnotatedString.Builder.highlightCpp(code: String) {
        val keywords = setOf(
            "#include", "#define", "#ifndef", "#endif", "int", "void", "char",
            "float", "double", "bool", "class", "struct", "public", "private",
            "return", "if", "else", "for", "while", "using", "namespace", "std",
            "auto", "const", "new", "delete", "true", "false"
        )
        highlightTokens(code, keywords)
        highlightComments(code, listOf("//", "/*"))
        highlightStrings(code)
        highlightNumbers(code)
    }

    private fun AnnotatedString.Builder.highlightGeneric(code: String) {
        val keywords = setOf("if", "else", "for", "while", "return", "true", "false", "null")
        highlightTokens(code, keywords)
        highlightComments(code, listOf("//", "#"))
        highlightStrings(code)
        highlightNumbers(code)
    }

    private fun AnnotatedString.Builder.highlightTokens(code: String, keywords: Set<String>) {
        val wordRegex = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b|#\\w+")
        for (match in wordRegex.findAll(code)) {
            val word = match.value
            if (keywords.contains(word)) {
                addStyle(
                    style = SpanStyle(color = KeywordColor, fontWeight = FontWeight.Bold),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
    }

    private fun AnnotatedString.Builder.highlightStrings(code: String) {
        val stringRegex = Regex("\"(\\\\.|[^\"])*\"|'(\\\\.|[^'])*'")
        for (match in stringRegex.findAll(code)) {
            addStyle(
                style = SpanStyle(color = StringColor),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }

    private fun AnnotatedString.Builder.highlightNumbers(code: String) {
        val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
        for (match in numberRegex.findAll(code)) {
            addStyle(
                style = SpanStyle(color = NumberColor),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }

    private fun AnnotatedString.Builder.highlightComments(code: String, commentPrefixes: List<String>) {
        val lines = code.lines()
        var currentOffset = 0
        for (line in lines) {
            for (prefix in commentPrefixes) {
                val idx = line.indexOf(prefix)
                if (idx != -1) {
                    addStyle(
                        style = SpanStyle(color = CommentColor),
                        start = currentOffset + idx,
                        end = currentOffset + line.length
                    )
                    break
                }
            }
            currentOffset += line.length + 1 // +1 for newline
        }
    }

    private fun AnnotatedString.Builder.highlightHtmlTags(code: String) {
        val tagRegex = Regex("</?[a-zA-Z0-9]+.*?>")
        for (match in tagRegex.findAll(code)) {
            addStyle(
                style = SpanStyle(color = TagColor),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }
}

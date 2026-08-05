package com.example.engine

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFO
}

data class CodeDiagnostic(
    val line: Int,
    val column: Int,
    val message: String,
    val codeSnippet: String,
    val severity: DiagnosticSeverity
)

object CodeLinterEngine {

    fun analyzeCode(code: String, language: String): List<CodeDiagnostic> {
        if (code.isBlank()) return emptyList()

        val diagnostics = mutableListOf<CodeDiagnostic>()
        val lines = code.lines()

        // 1. Check for Git Conflict Markers
        for ((index, line) in lines.withIndex()) {
            val lineNumber = index + 1
            if (line.startsWith("<<<<<<<") || line.startsWith("=======") || line.startsWith(">>>>>>>")) {
                diagnostics.add(
                    CodeDiagnostic(
                        line = lineNumber,
                        column = 1,
                        message = "تعارض كود (Git Conflict Marker) يجب حله.",
                        codeSnippet = line.take(30),
                        severity = DiagnosticSeverity.ERROR
                    )
                )
            }
        }

        // 2. Bracket Matching Analysis
        val stack = mutableListOf<Triple<Char, Int, Int>>() // char, line, col
        for ((lineIdx, line) in lines.withIndex()) {
            val lineNumber = lineIdx + 1
            var inString = false
            var stringChar = ' '

            for ((colIdx, ch) in line.withIndex()) {
                val colNumber = colIdx + 1

                // Handle string literals escaping
                if ((ch == '"' || ch == '\'') && (colIdx == 0 || line[colIdx - 1] != '\\')) {
                    if (!inString) {
                        inString = true
                        stringChar = ch
                    } else if (stringChar == ch) {
                        inString = false
                    }
                }

                if (!inString) {
                    when (ch) {
                        '(', '[', '{' -> stack.add(Triple(ch, lineNumber, colNumber))
                        ')' -> checkPop(stack, '(', lineNumber, colNumber, ")", diagnostics, line)
                        ']' -> checkPop(stack, '[', lineNumber, colNumber, "]", diagnostics, line)
                        '}' -> checkPop(stack, '{', lineNumber, colNumber, "}", diagnostics, line)
                    }
                }
            }

            // Check unclosed string literal on line (unless multiline raw string)
            if (inString && !language.lowercase().contains("python")) {
                diagnostics.add(
                    CodeDiagnostic(
                        line = lineNumber,
                        column = line.length.coerceAtLeast(1),
                        message = "نص غير مغلق (مفقود علامة الاقتباس $stringChar)",
                        codeSnippet = line.take(30),
                        severity = DiagnosticSeverity.WARNING
                    )
                )
            }
        }

        // Leftover unclosed brackets in stack
        for (item in stack) {
            diagnostics.add(
                CodeDiagnostic(
                    line = item.second,
                    column = item.third,
                    message = "قوس '${item.first}' غير مغلق",
                    codeSnippet = lines.getOrNull(item.second - 1)?.take(30) ?: "",
                    severity = DiagnosticSeverity.ERROR
                )
            )
        }

        // 3. Language specific checks (Python indentation / JS missing semi / HTML tags)
        val langLower = language.lowercase()
        if (langLower.contains("python")) {
            analyzePython(lines, diagnostics)
        } else if (langLower.contains("html") || langLower.contains("javascript") || langLower.contains("js")) {
            analyzeWeb(lines, diagnostics)
        }

        return diagnostics
    }

    private fun checkPop(
        stack: MutableList<Triple<Char, Int, Int>>,
        expectedOpening: Char,
        line: Int,
        col: Int,
        closingStr: String,
        diagnostics: MutableList<CodeDiagnostic>,
        lineText: String
    ) {
        if (stack.isEmpty()) {
            diagnostics.add(
                CodeDiagnostic(
                    line = line,
                    column = col,
                    message = "قوس إغلاق زائد '$closingStr' بدون قوس فتح مطابق",
                    codeSnippet = lineText.take(30),
                    severity = DiagnosticSeverity.ERROR
                )
            )
        } else {
            val last = stack.removeAt(stack.size - 1)
            if (last.first != expectedOpening) {
                diagnostics.add(
                    CodeDiagnostic(
                        line = line,
                        column = col,
                        message = "قوس غير متطابق: يتوقع إغلاق '${last.first}' المفتوح بالسطر ${last.second}",
                        codeSnippet = lineText.take(30),
                        severity = DiagnosticSeverity.ERROR
                    )
                )
            }
        }
    }

    private fun analyzePython(lines: List<String>, diagnostics: MutableList<CodeDiagnostic>) {
        for ((idx, line) in lines.withIndex()) {
            val lineNum = idx + 1
            val trimmed = line.trim()
            if (trimmed.endsWith(":")) {
                val nextLine = lines.getOrNull(idx + 1)
                if (nextLine != null && nextLine.isNotBlank()) {
                    val currentIndent = line.takeWhile { it.isWhitespace() }.length
                    val nextIndent = nextLine.takeWhile { it.isWhitespace() }.length
                    if (nextIndent <= currentIndent) {
                        diagnostics.add(
                            CodeDiagnostic(
                                line = lineNum + 1,
                                column = 1,
                                message = "خطأ في الإزاحة (IndentationError): يُتوقع كتلة ممتدة بعد ':'",
                                codeSnippet = nextLine.take(30),
                                severity = DiagnosticSeverity.WARNING
                            )
                        )
                    }
                }
            }
        }
    }

    private fun analyzeWeb(lines: List<String>, diagnostics: MutableList<CodeDiagnostic>) {
        // Simple HTML/JS tag check
        val htmlTagRegex = Regex("<([a-zA-Z1-6]+)(?:\\s+[^>]*)?>")
        val closingTagRegex = Regex("</([a-zA-Z1-6]+)>")

        val tagStack = mutableListOf<Pair<String, Int>>() // tag, line

        val voidTags = setOf("img", "br", "hr", "input", "meta", "link", "area", "base", "source")

        for ((idx, line) in lines.withIndex()) {
            val lineNum = idx + 1
            for (match in htmlTagRegex.findAll(line)) {
                val tag = match.groupValues[1].lowercase()
                if (!voidTags.contains(tag) && !line.contains("</$tag>")) {
                    tagStack.add(Pair(tag, lineNum))
                }
            }

            for (match in closingTagRegex.findAll(line)) {
                val tag = match.groupValues[1].lowercase()
                if (tagStack.isNotEmpty()) {
                    val lastIdx = tagStack.indexOfLast { it.first == tag }
                    if (lastIdx != -1) {
                        tagStack.removeAt(lastIdx)
                    }
                }
            }
        }

        // Remaining unclosed tags
        for ((tag, lineNum) in tagStack.take(3)) {
            diagnostics.add(
                CodeDiagnostic(
                    line = lineNum,
                    column = 1,
                    message = "وسم HTML غير مغلق <$tag>",
                    codeSnippet = "<$tag>",
                    severity = DiagnosticSeverity.INFO
                )
            )
        }
    }
}

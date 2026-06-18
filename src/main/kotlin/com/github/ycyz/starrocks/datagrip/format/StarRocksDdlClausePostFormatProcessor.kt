package com.github.ycyz.starrocks.datagrip.format

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.github.ycyz.starrocks.datagrip.dialect.ContextAwareDialectResolver

class StarRocksDdlClausePostFormatProcessor : PostFormatProcessor {
    private val resolver = ContextAwareDialectResolver()

    override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement = source

    override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
        val project = source.project
        val virtualFile = source.virtualFile
        val isStarRocks = source.language.id.equals("StarRocks", ignoreCase = true) ||
            resolver.shouldEnableStarRocksEnhancement(project, virtualFile)
        if (!isStarRocks) return rangeToReformat

        val document = PsiDocumentManager.getInstance(project).getDocument(source) ?: return rangeToReformat
        val original = document.text
        val formatted = collapseSimpleStarRocksClauses(original)
        if (formatted == original) return rangeToReformat

        document.replaceString(0, original.length, formatted)
        return TextRange(0, formatted.length)
    }

    private fun collapseSimpleStarRocksClauses(sql: String): String =
        normalizeUnnestFromItems(
            formatPropertiesBlock(
                normalizeDdlClauseBreaks(
                    collapseParenthesizedClause(
                        collapseParenthesizedClause(sql, DISTRIBUTED_HASH, PrefixStyle.NO_SPACE_BEFORE_PAREN),
                        PARTITION_BY,
                        PrefixStyle.SPACE_BEFORE_PAREN
                    )
                )
            )
        )

    private fun normalizeUnnestFromItems(sql: String): String {
        val lineSeparator = detectLineSeparator(sql)
        return sql
            .replace(Regex(",\\s+(UNNEST\\s*\\()", RegexOption.IGNORE_CASE), "," + lineSeparator + "$1")
            .replace(Regex("\\s+(CROSS\\s+JOIN\\s+LATERAL\\s+UNNEST\\s*\\()", RegexOption.IGNORE_CASE), lineSeparator + "$1")
            .replace(Regex("\\s+(LEFT\\s+JOIN\\s+UNNEST\\s*\\()", RegexOption.IGNORE_CASE), lineSeparator + "$1")
    }

    private fun normalizeDdlClauseBreaks(sql: String): String {
        val lineSeparator = detectLineSeparator(sql)
        return sql
            .replace(Regex("(^[\\t ]*\\))\\s+(COMMENT\\s+)", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)), "$1" + lineSeparator + "$2")
            .replace(Regex("\\)\\s+(REFRESH\\s+(?:IMMEDIATE|DEFERRED|MANUAL|ASYNC|SCHEDULE|EVERY))", RegexOption.IGNORE_CASE), ")" + lineSeparator + "$1")
            .replace(Regex("\\)\\s+(DISTRIBUTED\\s+BY)", RegexOption.IGNORE_CASE), ")" + lineSeparator + "$1")
            .replace(Regex("\\)\\s+(ORDER\\s+BY\\s*\\()", RegexOption.IGNORE_CASE), ")" + lineSeparator + "$1")
            .replace(Regex("\\)\\s+(PROPERTIES\\s*\\()", RegexOption.IGNORE_CASE), ")" + lineSeparator + "$1")
            .replace(Regex("\\b(COMMENT\\s+[^\\r\\n]+?)\\s+(PARTITION\\s+BY)", RegexOption.IGNORE_CASE), "$1" + lineSeparator + "$2")
            .replace(Regex("\\b(REFRESH\\s+(?:IMMEDIATE|DEFERRED|MANUAL|ASYNC|SCHEDULE|EVERY)[^\\r\\n]*?)\\s+(PARTITION\\s+BY)", RegexOption.IGNORE_CASE), "$1" + lineSeparator + "$2")
            .replace(Regex("\\b(REFRESH\\s+(?:IMMEDIATE|DEFERRED|MANUAL|ASYNC|SCHEDULE|EVERY)[^\\r\\n]*?)\\s+(ORDER\\s+BY\\s*\\()", RegexOption.IGNORE_CASE), "$1" + lineSeparator + "$2")
            .replace(Regex("\\b(REFRESH\\s+(?:IMMEDIATE|DEFERRED|MANUAL|ASYNC|SCHEDULE|EVERY)[^\\r\\n]*?)\\s+(PROPERTIES\\s*\\()", RegexOption.IGNORE_CASE), "$1" + lineSeparator + "$2")
            .replace(Regex("\\b(BUCKETS\\s+\\d+)\\s+(REFRESH\\s+(?:IMMEDIATE|DEFERRED|MANUAL|ASYNC|SCHEDULE|EVERY))", RegexOption.IGNORE_CASE), "$1" + lineSeparator + "$2")
            .replace(Regex("\\b(BUCKETS\\s+\\d+)\\s+(ORDER\\s+BY\\s*\\()", RegexOption.IGNORE_CASE), "$1" + lineSeparator + "$2")
            .replace(Regex("\\b(BUCKETS\\s+\\d+)\\s+(PROPERTIES\\s*\\()", RegexOption.IGNORE_CASE), "$1" + lineSeparator + "$2")
            .replace(Regex("\\b(DISTRIBUTED\\s+BY\\s+RANDOM(?:\\s+BUCKETS\\s+\\d+)?)\\s+(REFRESH\\s+(?:IMMEDIATE|DEFERRED|MANUAL|ASYNC|SCHEDULE|EVERY))", RegexOption.IGNORE_CASE), "$1" + lineSeparator + "$2")
            .replace(Regex("\\b(DISTRIBUTED\\s+BY\\s+RANDOM(?:\\s+BUCKETS\\s+\\d+)?)\\s+(ORDER\\s+BY\\s*\\()", RegexOption.IGNORE_CASE), "$1" + lineSeparator + "$2")
            .replace(Regex("\\b(DISTRIBUTED\\s+BY\\s+RANDOM(?:\\s+BUCKETS\\s+\\d+)?)\\s+(PROPERTIES\\s*\\()", RegexOption.IGNORE_CASE), "$1" + lineSeparator + "$2")
    }

    private fun formatPropertiesBlock(sql: String): String {
        val result = StringBuilder()
        var copiedUntil = 0
        var searchFrom = 0
        val lineSeparator = detectLineSeparator(sql)

        while (true) {
            val match = PROPERTIES.find(sql, searchFrom) ?: break
            val openParen = sql.indexOf('(', match.range.last)
            if (openParen < 0) break

            val closeParen = findMatchingParen(sql, openParen)
            if (closeParen < 0) {
                searchFrom = openParen + 1
                continue
            }

            val content = sql.substring(openParen + 1, closeParen)
            val properties = splitTopLevelCommaItems(content).map { it.trim() }.filter { it.isNotEmpty() }
            if (properties.size < 2) {
                searchFrom = closeParen + 1
                continue
            }

            val indent = detectLineIndent(sql, match.range.first) + "    "
            val formatted = buildString {
                append("PROPERTIES (")
                properties.forEachIndexed { index, property ->
                    append(lineSeparator)
                    append(indent)
                    append(property)
                    if (index < properties.lastIndex) append(',')
                }
                append(lineSeparator)
                append(detectLineIndent(sql, match.range.first))
                append(')')
            }

            result.append(sql, copiedUntil, match.range.first)
            result.append(formatted)
            copiedUntil = closeParen + 1
            searchFrom = closeParen + 1
        }

        if (copiedUntil == 0) return sql
        result.append(sql, copiedUntil, sql.length)
        return result.toString()
    }

    private fun collapseParenthesizedClause(sql: String, pattern: Regex, style: PrefixStyle): String {
        val result = StringBuilder()
        var copiedUntil = 0
        var searchFrom = 0

        while (true) {
            val match = pattern.find(sql, searchFrom) ?: break
            val openParen = sql.indexOf('(', match.range.last + 1)
            if (openParen < 0) break

            val closeParen = findMatchingParen(sql, openParen)
            if (closeParen < 0) {
                searchFrom = openParen + 1
                continue
            }

            val content = sql.substring(openParen + 1, closeParen)
            if (!content.contains('\n') && !content.contains('\r')) {
                searchFrom = closeParen + 1
                continue
            }

            val collapsed = normalizeWhitespaceOutsideQuotes(content).trim()
            if (collapsed.isEmpty() || collapsed.length > MAX_COLLAPSED_CLAUSE_LENGTH) {
                searchFrom = closeParen + 1
                continue
            }

            result.append(sql, copiedUntil, match.range.first)
            result.append(normalizePrefix(sql.substring(match.range.first, openParen), style))
            result.append(collapsed)
            result.append(')')
            copiedUntil = closeParen + 1
            searchFrom = closeParen + 1
        }

        if (copiedUntil == 0) return sql
        result.append(sql, copiedUntil, sql.length)
        return result.toString()
    }

    private fun normalizePrefix(prefix: String, style: PrefixStyle): String {
        val normalized = prefix.trim().replace(Regex("\\s+"), " ")
        return when (style) {
            PrefixStyle.NO_SPACE_BEFORE_PAREN -> "$normalized("
            PrefixStyle.SPACE_BEFORE_PAREN -> "$normalized ("
        }
    }

    private fun normalizeWhitespaceOutsideQuotes(text: String): String {
        val result = StringBuilder()
        var quote: Char? = null
        var pendingSpace = false
        var index = 0

        while (index < text.length) {
            val char = text[index]
            if (quote != null) {
                result.append(char)
                if (char == quote) quote = null
                index++
                continue
            }

            when {
                char == '\'' || char == '"' || char == '`' -> {
                    if (pendingSpace && result.isNotEmpty() && result.last() !in "(.") result.append(' ')
                    pendingSpace = false
                    quote = char
                    result.append(char)
                }
                char.isWhitespace() -> pendingSpace = true
                char == ',' -> {
                    trimTrailingSpace(result)
                    result.append(", ")
                    pendingSpace = false
                }
                char == ')' -> {
                    trimTrailingSpace(result)
                    result.append(char)
                    pendingSpace = false
                }
                char == '(' -> {
                    if (pendingSpace && result.isNotEmpty() && result.last().isLetterOrDigit()) result.append(' ')
                    pendingSpace = false
                    result.append(char)
                }
                else -> {
                    if (pendingSpace && result.isNotEmpty() && result.last() !in "(." && char !in "),") result.append(' ')
                    pendingSpace = false
                    result.append(char)
                }
            }
            index++
        }

        trimTrailingSpace(result)
        return result.toString().replace(Regex(",\\s+"), ", ")
    }

    private fun trimTrailingSpace(builder: StringBuilder) {
        while (builder.isNotEmpty() && builder.last().isWhitespace()) {
            builder.deleteCharAt(builder.length - 1)
        }
    }

    private fun splitTopLevelCommaItems(text: String): List<String> {
        val items = mutableListOf<String>()
        var start = 0
        var parenDepth = 0
        var quote: Char? = null
        for (index in text.indices) {
            val char = text[index]
            if (quote != null) {
                if (char == quote) quote = null
                continue
            }
            when (char) {
                '\'', '"', '`' -> quote = char
                '(' -> parenDepth++
                ')' -> if (parenDepth > 0) parenDepth--
                ',' -> if (parenDepth == 0) {
                    items.add(text.substring(start, index))
                    start = index + 1
                }
            }
        }
        items.add(text.substring(start))
        return items
    }

    private fun detectLineIndent(text: String, index: Int): String {
        val lineStart = text.lastIndexOf('\n', (index - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        return text.substring(lineStart, index).takeWhile { it == ' ' || it == '\t' }
    }

    private fun detectLineSeparator(text: String): String =
        if (text.contains("\r\n")) "\r\n" else "\n"

    private fun findMatchingParen(text: String, openParen: Int): Int {
        var depth = 0
        var quote: Char? = null
        for (index in openParen until text.length) {
            val char = text[index]
            if (quote != null) {
                if (char == quote) quote = null
                continue
            }
            when (char) {
                '\'', '"', '`' -> quote = char
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) return index
                }
            }
        }
        return -1
    }

    private enum class PrefixStyle {
        NO_SPACE_BEFORE_PAREN,
        SPACE_BEFORE_PAREN
    }

    private companion object {
        const val MAX_COLLAPSED_CLAUSE_LENGTH = 160
        val DISTRIBUTED_HASH = Regex("\\bDISTRIBUTED\\s+BY\\s+HASH\\s*(?=\\()", RegexOption.IGNORE_CASE)
        val PARTITION_BY = Regex("\\bPARTITION\\s+BY\\s*(?=\\()", RegexOption.IGNORE_CASE)
        val PROPERTIES = Regex("\\bPROPERTIES\\s*(?=\\()", RegexOption.IGNORE_CASE)
    }
}

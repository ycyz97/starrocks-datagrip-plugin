package com.github.ycyz.starrocks.datagrip.format

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.github.ycyz.starrocks.datagrip.dialect.ContextAwareDialectResolver

class StarRocksQueryPostFormatProcessor : PostFormatProcessor {
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
        val formatted = formatStarRocksQueryClauses(original)
        if (formatted == original) return rangeToReformat

        document.replaceString(0, original.length, formatted)
        return TextRange(0, formatted.length)
    }

    private fun formatStarRocksQueryClauses(sql: String): String =
        normalizeTopLevelQualify(
            normalizeGroupingClauses(
                normalizeUnnestFromItems(sql)
            )
        )

    private fun normalizeUnnestFromItems(sql: String): String {
        val lineSeparator = detectLineSeparator(sql)
        val result = StringBuilder(sql.length)
        var index = 0
        var state = ScanState()
        var changed = false

        while (index < sql.length) {
            val char = sql[index]
            val nextState = state.advance(char)
            if (state.isTopLevel && char == ',' && startsWithWordSequence(sql, index + 1, "UNNEST")) {
                result.append(',')
                appendLineBreakAndIndent(result, sql, index, lineSeparator)
                val nextIndex = skipSpaces(sql, index + 1)
                state = advanceState(sql, nextState, index + 1 until nextIndex)
                index = nextIndex
                changed = true
                continue
            }

            val joinMatch = topLevelJoinMatch(sql, index, state)
            if (joinMatch != null && !isAtLineStart(sql, index)) {
                appendLineBreakAndIndent(result, sql, index, lineSeparator)
                result.append(sql, index, joinMatch.end)
                state = advanceState(sql, state, index until joinMatch.end)
                index = joinMatch.end
                changed = true
                continue
            }

            result.append(char)
            state = nextState
            index++
        }

        return if (changed) result.toString() else sql
    }

    private fun normalizeTopLevelQualify(sql: String): String =
        normalizeTopLevelClauseStart(sql, "QUALIFY")

    private fun normalizeGroupingClauses(sql: String): String {
        val lineSeparator = detectLineSeparator(sql)
        val result = StringBuilder(sql.length)
        var index = 0
        var state = ScanState()
        var changed = false

        while (index < sql.length) {
            val match = when {
                state.isTopLevel -> GROUPING_PATTERNS.firstNotNullOfOrNull { pattern ->
                    matchWordSequenceEnd(sql, index, *pattern.words)?.let { GroupingMatch(pattern.normalized, it) }
                }
                else -> null
            }

            if (match != null && !isAtLineStart(sql, index)) {
                appendLineBreakAndIndent(result, sql, index, lineSeparator)
                result.append(match.normalized)
                state = advanceState(sql, state, index until match.end)
                index = match.end
                changed = true
                continue
            }

            val char = sql[index]
            result.append(char)
            state = state.advance(char)
            index++
        }

        return if (changed) result.toString() else sql
    }

    private fun normalizeTopLevelClauseStart(sql: String, keyword: String): String {
        val lineSeparator = detectLineSeparator(sql)
        val result = StringBuilder(sql.length)
        var index = 0
        var state = ScanState()
        var changed = false

        while (index < sql.length) {
            if (state.isTopLevel && startsWithWordSequence(sql, index, keyword) && !isAtLineStart(sql, index)) {
                appendLineBreakAndIndent(result, sql, index, lineSeparator)
                result.append(sql, index, index + keyword.length)
                state = advanceState(sql, state, index until index + keyword.length)
                index += keyword.length
                changed = true
                continue
            }

            val char = sql[index]
            result.append(char)
            state = state.advance(char)
            index++
        }

        return if (changed) result.toString() else sql
    }

    private fun topLevelJoinMatch(sql: String, index: Int, state: ScanState): JoinMatch? {
        if (!state.isTopLevel) return null
        if (index !in sql.indices || !sql[index].isLetter()) return null
        return JOIN_PATTERNS.firstNotNullOfOrNull { pattern ->
            matchWordSequenceEnd(sql, index, *pattern)?.let { JoinMatch(it) }
        }
    }

    private fun appendLineBreakAndIndent(builder: StringBuilder, sql: String, index: Int, lineSeparator: String) {
        while (builder.isNotEmpty() && builder.last().isWhitespace() && builder.last() != '\n' && builder.last() != '\r') {
            builder.deleteCharAt(builder.length - 1)
        }
        if (builder.endsWithLineBreak()) {
            return
        }
        builder.append(lineSeparator)
        builder.append(detectLineIndent(sql, index))
    }

    private fun startsWithWordSequence(sql: String, index: Int, vararg words: String): Boolean {
        return matchWordSequenceEnd(sql, index, *words) != null
    }

    private fun matchWordSequenceEnd(sql: String, index: Int, vararg words: String): Int? {
        var current = skipSpaces(sql, index)
        if (current > index && sql.substring(index, current).contains('\n')) return null

        words.forEachIndexed { wordIndex, word ->
            if (!sql.regionMatches(current, word, 0, word.length, ignoreCase = true)) return null
            if (!isWordBoundary(sql, current - 1) || !isWordBoundary(sql, current + word.length)) return null
            current += word.length
            if (wordIndex < words.lastIndex) {
                val next = skipSpaces(sql, current)
                if (next == current) return null
                current = next
            }
        }
        return current
    }

    private fun skipSpaces(sql: String, start: Int): Int {
        var index = start
        while (index < sql.length && sql[index].isWhitespace()) index++
        return index
    }

    private fun advanceState(sql: String, state: ScanState, range: IntRange): ScanState {
        var current = state
        for (index in range) {
            if (index in sql.indices) current = current.advance(sql[index])
        }
        return current
    }

    private fun isAtLineStart(sql: String, index: Int): Boolean {
        var cursor = index - 1
        while (cursor >= 0 && sql[cursor] != '\n' && sql[cursor] != '\r') {
            if (!sql[cursor].isWhitespace()) return false
            cursor--
        }
        return true
    }

    private fun detectLineIndent(text: String, index: Int): String {
        val lineStart = text.lastIndexOf('\n', (index - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        return text.substring(lineStart, index).takeWhile { it == ' ' || it == '\t' }
    }

    private fun detectLineSeparator(text: String): String =
        if (text.contains("\r\n")) "\r\n" else "\n"

    private fun isWordBoundary(text: String, index: Int): Boolean =
        index !in text.indices || !text[index].isLetterOrDigit() && text[index] != '_'

    private fun StringBuilder.endsWithLineBreak(): Boolean =
        isNotEmpty() && (last() == '\n' || last() == '\r')

    private data class JoinMatch(val end: Int)
    private data class GroupingMatch(val normalized: String, val end: Int)
    private data class GroupingPattern(val normalized: String, val words: Array<String>)

    private data class ScanState(
        val parenDepth: Int = 0,
        val angleDepth: Int = 0,
        val quote: Char? = null,
    ) {
        val isTopLevel: Boolean
            get() = parenDepth == 0 && angleDepth == 0 && quote == null

        fun advance(char: Char): ScanState {
            if (quote != null) {
                return if (char == quote) copy(quote = null) else this
            }
            return when (char) {
                '\'', '"', '`' -> copy(quote = char)
                '(' -> copy(parenDepth = parenDepth + 1)
                ')' -> copy(parenDepth = (parenDepth - 1).coerceAtLeast(0))
                '<' -> copy(angleDepth = angleDepth + 1)
                '>' -> copy(angleDepth = (angleDepth - 1).coerceAtLeast(0))
                else -> this
            }
        }
    }

    private companion object {
        val GROUPING_PATTERNS = listOf(
            GroupingPattern("GROUP BY GROUPING SETS", arrayOf("GROUP", "BY", "GROUPING", "SETS")),
            GroupingPattern("GROUP BY ROLLUP", arrayOf("GROUP", "BY", "ROLLUP")),
            GroupingPattern("GROUP BY CUBE", arrayOf("GROUP", "BY", "CUBE"))
        )

        val JOIN_PATTERNS = listOf(
            arrayOf("CROSS", "JOIN", "LATERAL", "UNNEST"),
            arrayOf("CROSS", "JOIN", "UNNEST"),
            arrayOf("LEFT", "JOIN", "LATERAL", "UNNEST"),
            arrayOf("LEFT", "JOIN", "UNNEST"),
            arrayOf("LEFT", "OUTER", "JOIN", "LATERAL", "UNNEST"),
            arrayOf("LEFT", "OUTER", "JOIN", "UNNEST")
        )
    }
}

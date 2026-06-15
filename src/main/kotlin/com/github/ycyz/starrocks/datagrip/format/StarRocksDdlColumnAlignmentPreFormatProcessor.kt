package com.github.ycyz.starrocks.datagrip.format

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.github.ycyz.starrocks.datagrip.dialect.ContextAwareDialectResolver

class StarRocksDdlColumnAlignmentPreFormatProcessor : PreFormatProcessor {
    private val resolver = ContextAwareDialectResolver()

    override fun process(element: ASTNode, range: TextRange): TextRange {
        val psi = element.psi ?: return range
        val file = psi.containingFile ?: return range
        val project = file.project
        val virtualFile = file.virtualFile
        val isStarRocks = file.language.id.equals("StarRocks", ignoreCase = true) ||
            resolver.shouldEnableStarRocksEnhancement(project, virtualFile)
        if (!isStarRocks) return range

        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return range
        val original = document.text
        val formatted = alignUpdateSetAssignments(formatCreateTableClauses(alignDdlColumns(original)))
        if (formatted == original) return range

        document.replaceString(0, original.length, formatted)
        return TextRange(0, formatted.length)
    }

    private fun alignDdlColumns(sql: String): String {
        val createDdl = Regex(
            "\\bCREATE\\s+(?:TEMPORARY\\s+|EXTERNAL\\s+)?(?:TABLE|MATERIALIZED\\s+VIEW)\\b",
            RegexOption.IGNORE_CASE
        )
        val result = StringBuilder()
        var searchFrom = 0
        var lastCopied = 0

        while (true) {
            val match = createDdl.find(sql, searchFrom) ?: break
            val openParen = sql.indexOf('(', match.range.last + 1)
            if (openParen < 0) break

            val closeParen = findMatchingParen(sql, openParen)
            if (closeParen < 0) {
                searchFrom = openParen + 1
                continue
            }

            val content = sql.substring(openParen + 1, closeParen)
            val columnIndent = detectLineIndent(sql, match.range.first) + DEFAULT_INDENT_UNIT
            val aligned = alignColumnBlock(content, detectLineSeparator(sql), columnIndent)
            if (aligned == null) {
                searchFrom = closeParen + 1
                continue
            }

            result.append(sql, lastCopied, openParen + 1)
            result.append(aligned)
            lastCopied = closeParen
            searchFrom = closeParen + 1
        }

        if (lastCopied == 0) return sql
        result.append(sql, lastCopied, sql.length)
        return result.toString()
    }

    private fun formatCreateTableClauses(sql: String): String {
        val lineSeparator = detectLineSeparator(sql)
        return sql
            .replace(Regex("\\)\\s+(AGGREGATE\\s+KEY|DUPLICATE\\s+KEY|PRIMARY\\s+KEY|UNIQUE\\s+KEY)", RegexOption.IGNORE_CASE), ")" + lineSeparator + "$1")
            .replace(Regex("\\)\\s+(PARTITION\\s+BY)", RegexOption.IGNORE_CASE), ")" + lineSeparator + "$1")
            .replace(Regex("\\)\\s+(DISTRIBUTED\\s+BY)", RegexOption.IGNORE_CASE), ")" + lineSeparator + "$1")
            .replace(Regex("\\)\\s+(ROLLUP\\s*\\()", RegexOption.IGNORE_CASE), ")" + lineSeparator + "$1")
            .replace(Regex("\\)\\s+(PROPERTIES\\s*\\()", RegexOption.IGNORE_CASE), ")" + lineSeparator + "$1")
            .replace(Regex("\\b(AGGREGATE\\s+KEY|DUPLICATE\\s+KEY|PRIMARY\\s+KEY|UNIQUE\\s+KEY)([^\\r\\n]*?)\\s+(PARTITION\\s+BY)", RegexOption.IGNORE_CASE), "$1$2" + lineSeparator + "$3")
            .replace(Regex("\\b(PARTITION\\s+BY)([^\\r\\n]*?)\\s+(DISTRIBUTED\\s+BY)", RegexOption.IGNORE_CASE), "$1$2" + lineSeparator + "$3")
            .replace(Regex("\\b(DISTRIBUTED\\s+BY)([^\\r\\n]*?)\\s+(ROLLUP\\s*\\()", RegexOption.IGNORE_CASE), "$1$2" + lineSeparator + "$3")
            .replace(Regex("\\b(DISTRIBUTED\\s+BY)([^\\r\\n]*?)\\s+(PROPERTIES\\s*\\()", RegexOption.IGNORE_CASE), "$1$2" + lineSeparator + "$3")
            .replace(Regex("\\b(ROLLUP\\s*\\([^;]*?\\))\\s+(PROPERTIES\\s*\\()", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), "$1" + lineSeparator + "$2")
    }

    private fun alignUpdateSetAssignments(sql: String): String {
        val update = Regex("\\bUPDATE\\b", RegexOption.IGNORE_CASE)
        val result = StringBuilder()
        var searchFrom = 0
        var lastCopied = 0

        while (true) {
            val match = update.find(sql, searchFrom) ?: break
            val setIndex = findTopLevelWord(sql, "SET", match.range.last + 1) ?: break
            val endIndex = findTopLevelWord(sql, "FROM", setIndex + 3)
                ?: findTopLevelWord(sql, "WHERE", setIndex + 3)
                ?: findStatementEnd(sql, setIndex + 3)

            val setBodyStart = setIndex + 3
            val setBody = sql.substring(setBodyStart, endIndex)
            val aligned = alignAssignmentBlock(setBody, detectLineSeparator(sql))
            if (aligned == null) {
                searchFrom = endIndex
                continue
            }

            result.append(sql, lastCopied, setBodyStart)
            result.append(aligned)
            lastCopied = endIndex
            searchFrom = endIndex
        }

        if (lastCopied == 0) return sql
        result.append(sql, lastCopied, sql.length)
        return result.toString()
    }

    private fun alignAssignmentBlock(content: String, lineSeparator: String): String? {
        val assignments = splitTopLevelCommaItems(content).map { it.trim() }.filter { it.isNotEmpty() }
        if (assignments.size < 2) return null

        val parsed = assignments.map { parseAssignment(it) }
        if (parsed.any { it == null }) return null

        val nameWidth = parsed.filterNotNull().maxOf { it.name.length }
        val indent = detectIndent(content).ifEmpty { "    " }
        val lines = parsed.filterNotNull().mapIndexed { index, assignment ->
            lineSeparator +
                indent +
                assignment.name.padEnd(nameWidth) +
                " = " +
                assignment.value +
                if (index < assignments.lastIndex) "," else ""
        }
        return lines.joinToString("") + lineSeparator
    }

    private fun parseAssignment(text: String): Assignment? {
        val equalsIndex = findTopLevelChar(text, '=') ?: return null
        val name = text.substring(0, equalsIndex).trim()
        val value = text.substring(equalsIndex + 1).trim()
        if (name.isEmpty() || value.isEmpty()) return null
        return Assignment(name, value)
    }

    private fun detectIndent(text: String): String {
        val lineStart = text.lastIndexOfAny(charArrayOf('\n', '\r')) + 1
        return text.substring(lineStart).takeWhile { it == ' ' || it == '\t' }
    }

    private fun detectLineIndent(text: String, index: Int): String {
        val lineStart = text.lastIndexOf('\n', (index - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        return text.substring(lineStart, index).takeWhile { it == ' ' || it == '\t' }
    }

    private fun parentIndent(indent: String): String =
        if (indent.endsWith(DEFAULT_INDENT_UNIT)) indent.dropLast(DEFAULT_INDENT_UNIT.length) else ""

    private fun findTopLevelWord(text: String, word: String, startIndex: Int): Int? {
        var index = startIndex
        var parenDepth = 0
        var angleDepth = 0
        var quote: Char? = null
        while (index < text.length) {
            val char = text[index]
            if (quote != null) {
                if (char == quote) quote = null
                index++
                continue
            }
            when (char) {
                '\'', '"', '`' -> quote = char
                '(' -> parenDepth++
                ')' -> if (parenDepth > 0) parenDepth--
                '<' -> angleDepth++
                '>' -> if (angleDepth > 0) angleDepth--
            }
            if (
                parenDepth == 0 &&
                angleDepth == 0 &&
                text.regionMatches(index, word, 0, word.length, ignoreCase = true) &&
                isWordBoundary(text, index - 1) &&
                isWordBoundary(text, index + word.length)
            ) {
                return index
            }
            if (char == ';' && parenDepth == 0 && angleDepth == 0) return null
            index++
        }
        return null
    }

    private fun findTopLevelChar(text: String, target: Char): Int? {
        var parenDepth = 0
        var angleDepth = 0
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
                '<' -> angleDepth++
                '>' -> if (angleDepth > 0) angleDepth--
            }
            if (char == target && parenDepth == 0 && angleDepth == 0) return index
        }
        return null
    }

    private fun findStatementEnd(text: String, startIndex: Int): Int {
        var parenDepth = 0
        var quote: Char? = null
        for (index in startIndex until text.length) {
            val char = text[index]
            if (quote != null) {
                if (char == quote) quote = null
                continue
            }
            when (char) {
                '\'', '"', '`' -> quote = char
                '(' -> parenDepth++
                ')' -> if (parenDepth > 0) parenDepth--
                ';' -> if (parenDepth == 0) return index
            }
        }
        return text.length
    }

    private fun alignColumnBlock(content: String, lineSeparator: String, indent: String): String? {
        val items = splitTopLevelCommaItems(content).map { it.trim() }.filter { it.isNotEmpty() }
        if (items.size < 2) return null

        val parsed = items.map { parseColumnDefinition(it) }
        val columnCount = parsed.count { it != null }
        if (columnCount < 2) return null

        val nameWidth = parsed.filterNotNull().maxOf { it.name.length }
        val typeWidth = parsed.filterNotNull().maxOf { it.type.length }
        val lines = items.mapIndexed { index, item ->
            val column = parsed[index]
            val body = if (column == null) {
                item
            } else if (column.type.isEmpty()) {
                buildString {
                    append(column.name.padEnd(nameWidth))
                    if (column.tail.isNotBlank()) {
                        append(' ')
                        append(column.tail)
                    }
                }
            } else {
                buildString {
                    append(column.name.padEnd(nameWidth))
                    append(' ')
                    append(column.type.padEnd(typeWidth))
                    if (column.tail.isNotBlank()) {
                        append(' ')
                        append(column.tail)
                    }
                }
            }
            lineSeparator + indent + body + if (index < items.lastIndex) "," else ""
        }
        return lines.joinToString("") + lineSeparator + parentIndent(indent)
    }

    private fun parseColumnDefinition(text: String): ColumnDefinition? {
        val name = readFirstToken(text) ?: return null
        if (name.uppercase().trim('`', '"') in NON_COLUMN_HEADS) return null

        val rest = text.substring(name.length).trimStart()
        if (rest.isEmpty()) return null

        val tokens = readTopLevelTokens(rest)
        if (tokens.isEmpty()) return null

        val tailStart = tokens.firstOrNull { it.value.uppercase() in COLUMN_TAIL_HEADS }?.start
        val type = if (tailStart == null) rest.trim() else rest.substring(0, tailStart).trim()
        if (type.isEmpty() && tailStart != 0) return null

        val tail = if (tailStart == null) "" else rest.substring(tailStart).trim()
        return ColumnDefinition(name, type, tail)
    }

    private fun readFirstToken(text: String): String? {
        val trimmed = text.trimStart()
        if (trimmed.isEmpty()) return null
        if (trimmed.first() == '`') {
            val end = trimmed.indexOf('`', startIndex = 1)
            return if (end > 0) trimmed.substring(0, end + 1) else null
        }
        return trimmed.takeWhile { !it.isWhitespace() }.ifEmpty { null }
    }

    private fun readTopLevelTokens(text: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0
        while (index < text.length) {
            while (index < text.length && text[index].isWhitespace()) index++
            if (index >= text.length) break

            val start = index
            var parenDepth = 0
            var angleDepth = 0
            var quote: Char? = null
            while (index < text.length) {
                val char = text[index]
                if (quote != null) {
                    if (char == quote) quote = null
                    index++
                    continue
                }
                when (char) {
                    '\'', '"', '`' -> quote = char
                    '(' -> parenDepth++
                    ')' -> if (parenDepth > 0) parenDepth--
                    '<' -> angleDepth++
                    '>' -> if (angleDepth > 0) angleDepth--
                }
                if (char.isWhitespace() && parenDepth == 0 && angleDepth == 0) break
                index++
            }
            tokens.add(Token(text.substring(start, index), start))
        }
        return tokens
    }

    private fun splitTopLevelCommaItems(text: String): List<String> {
        val items = mutableListOf<String>()
        var start = 0
        var parenDepth = 0
        var angleDepth = 0
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
                '<' -> angleDepth++
                '>' -> if (angleDepth > 0) angleDepth--
                ',' -> if (parenDepth == 0 && angleDepth == 0) {
                    items.add(text.substring(start, index))
                    start = index + 1
                }
            }
        }
        items.add(text.substring(start))
        return items
    }

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

    private fun detectLineSeparator(text: String): String =
        if (text.contains("\r\n")) "\r\n" else "\n"

    private fun isWordBoundary(text: String, index: Int): Boolean =
        index !in text.indices || !text[index].isLetterOrDigit() && text[index] != '_'

    private data class ColumnDefinition(val name: String, val type: String, val tail: String)
    private data class Assignment(val name: String, val value: String)
    private data class Token(val value: String, val start: Int)

    private companion object {
        const val DEFAULT_INDENT_UNIT = "    "

        val NON_COLUMN_HEADS = setOf(
            "AGGREGATE",
            "CONSTRAINT",
            "DISTRIBUTED",
            "DUPLICATE",
            "FOREIGN",
            "INDEX",
            "KEY",
            "ORDER",
            "PARTITION",
            "PRIMARY",
            "PROPERTIES",
            "ROLLUP",
            "UNIQUE"
        )

        val COLUMN_TAIL_HEADS = setOf(
            "AGGREGATE",
            "AS",
            "AUTO_INCREMENT",
            "BITMAP_UNION",
            "COMMENT",
            "DEFAULT",
            "HLL_UNION",
            "MAX",
            "MIN",
            "NOT",
            "NULL",
            "REPLACE",
            "REPLACE_IF_NOT_NULL",
            "SUM"
        )
    }
}

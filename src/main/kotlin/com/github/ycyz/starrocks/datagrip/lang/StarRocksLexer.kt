package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lexer.DelegateLexer
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.mysql.MysqlLexer
import com.intellij.sql.dialects.mysql.MysqlReservedKeywords.MYSQL_LEFT
import com.intellij.sql.psi.SqlTokens.SQL_IDENT
import com.intellij.sql.psi.SqlTokens.SQL_IDENT_DELIMITED
import com.intellij.sql.psi.SqlTokens.SQL_LEFT_BRACKET
import com.intellij.sql.psi.SqlTokens.SQL_LINE_COMMENT
import com.intellij.sql.psi.SqlTokens.SQL_LEFT_PAREN
import com.intellij.sql.psi.SqlTokens.SQL_RIGHT_BRACKET
import com.intellij.sql.psi.SqlTokens.SQL_RIGHT_PAREN
import com.intellij.sql.psi.SqlTokens.SQL_STRING_TOKEN

class StarRocksLexer : DelegateLexer(MysqlLexer()) {
    override fun getTokenType(): IElementType? {
        if (isLineStartDoubleDashComment()) {
            return SQL_LINE_COMMENT
        }

        val tokenType = super.getTokenType()
        if (tokenType == SQL_IDENT_DELIMITED && isDoubleQuotedToken()) {
            return SQL_STRING_TOKEN
        }
        if (isBareUnnestColumnToken()) {
            return SQL_IDENT
        }
        if (isFullJoinModifierToken()) {
            return MYSQL_LEFT
        }
        if (tokenType == SQL_LEFT_BRACKET && isArrayLiteralStart()) {
            return SQL_LEFT_PAREN
        }
        if (tokenType == SQL_RIGHT_BRACKET && isArrayLiteralEnd()) {
            return SQL_RIGHT_PAREN
        }
        return tokenType
    }

    override fun getTokenEnd(): Int {
        return if (isLineStartDoubleDashComment()) lineCommentEnd(tokenStart) else super.getTokenEnd()
    }

    override fun advance() {
        if (isLineStartDoubleDashComment()) {
            val commentEnd = lineCommentEnd(tokenStart)
            while (super.getTokenType() != null && super.getTokenStart() < commentEnd) {
                super.advance()
            }
            return
        }
        super.advance()
    }

    private fun isLineStartDoubleDashComment(): Boolean {
        val start = tokenStart
        if (start + LINE_COMMENT_PREFIX.length > bufferEnd) return false
        if (!bufferSequence.startsWith(LINE_COMMENT_PREFIX, start)) return false
        return isOnlyWhitespaceBeforeOnLine(start)
    }

    private fun isOnlyWhitespaceBeforeOnLine(offset: Int): Boolean {
        var index = offset - 1
        while (index >= 0) {
            val char = bufferSequence[index]
            if (char == '\n' || char == '\r') return true
            if (!char.isWhitespace()) return false
            index--
        }
        return true
    }

    private fun lineCommentEnd(start: Int): Int {
        var index = start
        while (index < bufferEnd) {
            val char = bufferSequence[index]
            if (char == '\n' || char == '\r') break
            index++
        }
        return index
    }

    private fun isDoubleQuotedToken(): Boolean {
        val start = tokenStart
        val end = tokenEnd
        return start < end && bufferSequence[start] == '"'
    }

    private fun isFullJoinModifierToken(): Boolean {
        val tokenText = bufferSequence.subSequence(tokenStart, tokenEnd).toString()
        if (!tokenText.equals("FULL", ignoreCase = true)) return false

        val nextWords = readNextWords(tokenEnd, limit = 2)
        return nextWords.firstOrNull() == "JOIN" ||
            nextWords.size >= 2 && nextWords[0] == "OUTER" && nextWords[1] == "JOIN"
    }

    private fun isBareUnnestColumnToken(): Boolean {
        val tokenText = bufferSequence.subSequence(tokenStart, tokenEnd).toString()
        if (!tokenText.equals("UNNEST", ignoreCase = true)) return false
        return nextSignificantChar(tokenEnd) != '('
    }

    private fun isArrayLiteralStart(): Boolean {
        val previous = previousSignificantChar(tokenStart)
        val next = nextSignificantChar(tokenEnd)
        return next != null &&
            next != ']' &&
            isArrayLiteralBoundaryBefore(previous, tokenStart) &&
            isArrayLiteralFirstChar(next)
    }

    private fun isArrayLiteralEnd(): Boolean {
        val previous = previousSignificantChar(tokenStart)
        val next = nextSignificantChar(tokenEnd)
        return previous != null &&
            previous != '[' &&
            isArrayLiteralBoundaryAfter(next, tokenEnd)
    }

    private fun isArrayLiteralBoundaryBefore(char: Char?, offset: Int): Boolean =
        char == null ||
            char in "([,{=+-*/%<>" ||
            previousWord(offset) in ARRAY_LITERAL_PREFIX_WORDS

    private fun isArrayLiteralBoundaryAfter(char: Char?, offset: Int): Boolean =
        char == null ||
            char in "),;+-*/%<>" ||
            nextWord(offset) in ARRAY_LITERAL_SUFFIX_WORDS

    private fun isArrayLiteralFirstChar(char: Char): Boolean =
        char.isDigit() || char == '\'' || char == '"' || char == '-' || char == '+' || char.isLetter()

    private fun previousSignificantChar(startOffset: Int): Char? {
        var index = startOffset - 1
        while (index >= 0) {
            val char = bufferSequence[index]
            if (!char.isWhitespace()) return char
            index--
        }
        return null
    }

    private fun nextSignificantChar(startOffset: Int): Char? {
        var index = startOffset
        while (index < bufferEnd) {
            val char = bufferSequence[index]
            if (!char.isWhitespace()) return char
            index++
        }
        return null
    }

    private fun previousWord(startOffset: Int): String? {
        var index = startOffset - 1
        while (index >= 0 && bufferSequence[index].isWhitespace()) index--
        val end = index + 1
        while (index >= 0 && (bufferSequence[index].isLetterOrDigit() || bufferSequence[index] == '_')) index--
        val start = index + 1
        if (start >= end) return null
        return bufferSequence.subSequence(start, end).toString().uppercase()
    }

    private fun nextWord(startOffset: Int): String? {
        var index = startOffset
        while (index < bufferEnd && bufferSequence[index].isWhitespace()) index++
        val start = index
        while (index < bufferEnd && (bufferSequence[index].isLetterOrDigit() || bufferSequence[index] == '_')) index++
        if (start == index) return null
        return bufferSequence.subSequence(start, index).toString().uppercase()
    }

    private fun readNextWords(startOffset: Int, limit: Int): List<String> {
        val words = mutableListOf<String>()
        var index = startOffset
        while (index < bufferEnd && words.size < limit) {
            while (index < bufferEnd && bufferSequence[index].isWhitespace()) index++
            if (index >= bufferEnd) break

            val start = index
            while (index < bufferEnd && (bufferSequence[index].isLetterOrDigit() || bufferSequence[index] == '_')) {
                index++
            }
            if (start == index) break
            words.add(bufferSequence.subSequence(start, index).toString().uppercase())
        }
        return words
    }

    private companion object {
        val ARRAY_LITERAL_PREFIX_WORDS = setOf("SELECT", "AS", "IN", "THEN", "ELSE", "VALUES", "ARRAY")
        val ARRAY_LITERAL_SUFFIX_WORDS = setOf(
            "AS",
            "FROM",
            "WHERE",
            "GROUP",
            "HAVING",
            "ORDER",
            "LIMIT",
            "UNION",
            "INTERSECT",
            "EXCEPT",
            "JOIN",
            "ON"
        )

        const val LINE_COMMENT_PREFIX = "--"
    }
}

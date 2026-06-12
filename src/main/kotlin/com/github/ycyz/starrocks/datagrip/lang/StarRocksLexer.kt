package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lexer.DelegateLexer
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.mysql.MysqlLexer
import com.intellij.sql.dialects.mysql.MysqlReservedKeywords.MYSQL_LEFT
import com.intellij.sql.psi.SqlTokens.SQL_IDENT_DELIMITED
import com.intellij.sql.psi.SqlTokens.SQL_STRING_TOKEN

class StarRocksLexer : DelegateLexer(MysqlLexer()) {
    override fun getTokenType(): IElementType? {
        val tokenType = super.getTokenType()
        if (tokenType == SQL_IDENT_DELIMITED && isDoubleQuotedToken()) {
            return SQL_STRING_TOKEN
        }
        if (isFullJoinModifierToken()) {
            return MYSQL_LEFT
        }
        return tokenType
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
}

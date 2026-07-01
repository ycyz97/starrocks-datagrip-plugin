package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.database.StarRocksTypeSystem
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksFunctionCatalog
import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType.BAD_CHARACTER
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlTokens.SQL_ASTERISK
import com.intellij.sql.psi.SqlTokens.SQL_BLOCK_COMMENT
import com.intellij.sql.psi.SqlTokens.SQL_COLON
import com.intellij.sql.psi.SqlTokens.SQL_COMMA
import com.intellij.sql.psi.SqlTokens.SQL_FLOAT_TOKEN
import com.intellij.sql.psi.SqlTokens.SQL_IDENT
import com.intellij.sql.psi.SqlTokens.SQL_IDENT_DELIMITED
import com.intellij.sql.psi.SqlTokens.SQL_INTEGER_TOKEN
import com.intellij.sql.psi.SqlTokens.SQL_KEYWORD_TOKEN
import com.intellij.sql.psi.SqlTokens.SQL_LEFT_BRACE
import com.intellij.sql.psi.SqlTokens.SQL_LEFT_BRACKET
import com.intellij.sql.psi.SqlTokens.SQL_LEFT_PAREN
import com.intellij.sql.psi.SqlTokens.SQL_LINE_COMMENT
import com.intellij.sql.psi.SqlTokens.SQL_OP_BITWISE_AND
import com.intellij.sql.psi.SqlTokens.SQL_OP_BITWISE_OR
import com.intellij.sql.psi.SqlTokens.SQL_OP_CONCAT
import com.intellij.sql.psi.SqlTokens.SQL_OP_DIV
import com.intellij.sql.psi.SqlTokens.SQL_OP_EQ
import com.intellij.sql.psi.SqlTokens.SQL_OP_GE
import com.intellij.sql.psi.SqlTokens.SQL_OP_GT
import com.intellij.sql.psi.SqlTokens.SQL_OP_LE
import com.intellij.sql.psi.SqlTokens.SQL_OP_LEFT_SHIFT
import com.intellij.sql.psi.SqlTokens.SQL_OP_LT
import com.intellij.sql.psi.SqlTokens.SQL_OP_MINUS
import com.intellij.sql.psi.SqlTokens.SQL_OP_MODULO
import com.intellij.sql.psi.SqlTokens.SQL_OP_NEQ
import com.intellij.sql.psi.SqlTokens.SQL_OP_NEQ2
import com.intellij.sql.psi.SqlTokens.SQL_OP_NOT2
import com.intellij.sql.psi.SqlTokens.SQL_OP_PLUS
import com.intellij.sql.psi.SqlTokens.SQL_OP_RIGHT_SHIFT
import com.intellij.sql.psi.SqlTokens.SQL_PERIOD
import com.intellij.sql.psi.SqlTokens.SQL_RIGHT_BRACE
import com.intellij.sql.psi.SqlTokens.SQL_RIGHT_BRACKET
import com.intellij.sql.psi.SqlTokens.SQL_RIGHT_PAREN
import com.intellij.sql.psi.SqlTokens.SQL_SEMICOLON
import com.intellij.sql.psi.SqlTokens.SQL_STRING_TOKEN

class StarRocksLexer(
    private val highlightCategories: Boolean = false
) : LexerBase() {
    private var buffer: CharSequence = ""
    private var endOffset: Int = 0
    private var currentStart: Int = 0
    private var currentEnd: Int = 0
    private var currentToken: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.endOffset = endOffset
        this.currentStart = startOffset
        locateToken()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = currentToken

    override fun getTokenStart(): Int = currentStart

    override fun getTokenEnd(): Int = currentEnd

    override fun advance() {
        currentStart = currentEnd
        locateToken()
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = endOffset

    private fun locateToken() {
        if (currentStart >= endOffset) {
            currentToken = null
            currentEnd = currentStart
            return
        }

        val char = buffer[currentStart]
        when {
            char.isWhitespace() -> locateWhile(WHITE_SPACE) { it.isWhitespace() }
            isLineCommentStart(currentStart) -> locateLineComment()
            isBlockCommentStart(currentStart) -> locateBlockComment()
            char == '\'' || char == '"' -> locateQuotedString(char)
            char == '`' -> locateDelimitedIdentifier()
            highlightCategories && char == '@' -> locateVariable()
            highlightCategories && char == ':' && isParameterStart(currentStart + 1) -> locateNamedParameter()
            highlightCategories && isBracedParameterStart(currentStart) -> locateBracedParameter()
            highlightCategories && char == '?' -> locateFixed(StarRocksHighlightTokenTypes.PARAMETER)
            char.isDigit() -> locateNumber()
            isIdentifierStart(char) -> locateWord()
            char == ',' -> locateFixed(SQL_COMMA)
            char == ';' -> locateFixed(SQL_SEMICOLON)
            char in SYMBOL_CHARS -> locateSymbol()
            else -> locateFixed(BAD_CHARACTER)
        }
    }

    private fun locateBlockComment() {
        currentToken = SQL_BLOCK_COMMENT
        currentEnd = currentStart + 2
        while (currentEnd + 1 < endOffset) {
            if (buffer[currentEnd] == '*' && buffer[currentEnd + 1] == '/') {
                currentEnd += 2
                return
            }
            currentEnd++
        }
        currentEnd = endOffset
    }

    private fun locateLineComment() {
        currentToken = SQL_LINE_COMMENT
        currentEnd = currentStart + 2
        while (currentEnd < endOffset && buffer[currentEnd] != '\n' && buffer[currentEnd] != '\r') {
            currentEnd++
        }
    }

    private fun locateQuotedString(quote: Char) {
        currentToken = SQL_STRING_TOKEN
        currentEnd = currentStart + 1
        while (currentEnd < endOffset) {
            val char = buffer[currentEnd]
            currentEnd++
            if (char == quote) {
                if (currentEnd < endOffset && buffer[currentEnd] == quote) {
                    currentEnd++
                    continue
                }
                break
            }
            if (char == '\\' && currentEnd < endOffset) {
                currentEnd++
            }
        }
    }

    private fun locateDelimitedIdentifier() {
        currentToken = SQL_IDENT_DELIMITED
        currentEnd = currentStart + 1
        while (currentEnd < endOffset) {
            val char = buffer[currentEnd]
            currentEnd++
            if (char == '`') break
        }
    }

    private fun locateNumber() {
        currentEnd = currentStart
        var hasDot = false
        while (currentEnd < endOffset && (buffer[currentEnd].isDigit() || buffer[currentEnd] == '.')) {
            if (buffer[currentEnd] == '.') {
                hasDot = true
            }
            currentEnd++
        }
        currentToken = if (hasDot) SQL_FLOAT_TOKEN else SQL_INTEGER_TOKEN
    }

    private fun locateVariable() {
        currentToken = StarRocksHighlightTokenTypes.VARIABLE
        currentEnd = currentStart + 1
        while (currentEnd < endOffset && isVariablePart(buffer[currentEnd])) {
            currentEnd++
        }
    }

    private fun locateNamedParameter() {
        currentToken = StarRocksHighlightTokenTypes.PARAMETER
        currentEnd = currentStart + 2
        while (currentEnd < endOffset && isParameterPart(buffer[currentEnd])) {
            currentEnd++
        }
    }

    private fun locateBracedParameter() {
        currentToken = StarRocksHighlightTokenTypes.PARAMETER
        currentEnd = currentStart + 2
        while (currentEnd < endOffset && buffer[currentEnd] != '}') {
            currentEnd++
        }
        if (currentEnd < endOffset) {
            currentEnd++
        }
    }

    private fun locateWord() {
        locateWhile(SQL_IDENT) { isIdentifierPart(it) }
        val word = buffer.subSequence(currentStart, currentEnd).toString().uppercase()
        currentToken = when {
            highlightCategories && isBuiltinFunctionCall(word) -> StarRocksHighlightTokenTypes.FUNCTION
            highlightCategories && word in DATA_TYPE_NAMES -> StarRocksHighlightTokenTypes.DATA_TYPE
            highlightCategories && isFunctionLikeKeywordCall(word) -> StarRocksHighlightTokenTypes.FUNCTION
            StarRocksKeywordCatalog.isKeyword(word) -> SQL_KEYWORD_TOKEN
            highlightCategories && isUserFunctionCall() -> StarRocksHighlightTokenTypes.FUNCTION
            else -> currentToken
        }
    }

    private fun isBuiltinFunctionCall(word: String): Boolean {
        if (word !in FUNCTION_NAMES) {
            return false
        }
        return nextNonWhitespaceChar(currentEnd) == '('
    }

    private fun isUserFunctionCall(): Boolean {
        return nextNonWhitespaceChar(currentEnd) == '('
    }

    private fun isFunctionLikeKeywordCall(word: String): Boolean {
        if (word !in FUNCTION_LIKE_KEYWORDS) {
            return false
        }
        return nextNonWhitespaceChar(currentEnd) == '('
    }

    private fun nextNonWhitespaceChar(offset: Int): Char? {
        var cursor = offset
        while (cursor < endOffset && buffer[cursor].isWhitespace()) {
            cursor++
        }
        return if (cursor < endOffset) buffer[cursor] else null
    }

    private fun locateFixed(token: IElementType) {
        currentToken = token
        currentEnd = currentStart + 1
    }

    private fun locateSymbol() {
        val next = if (currentStart + 1 < endOffset) buffer[currentStart + 1] else null
        val current = buffer[currentStart]

        when {
            current == '<' && next == '=' -> locateFixed(SQL_OP_LE, 2)
            current == '>' && next == '=' -> locateFixed(SQL_OP_GE, 2)
            current == '<' && next == '>' -> locateFixed(SQL_OP_NEQ, 2)
            current == '!' && next == '=' -> locateFixed(SQL_OP_NEQ2, 2)
            current == '<' && next == '<' -> locateFixed(SQL_OP_LEFT_SHIFT, 2)
            current == '>' && next == '>' -> locateFixed(SQL_OP_RIGHT_SHIFT, 2)
            current == '|' && next == '|' -> locateFixed(SQL_OP_CONCAT, 2)
            else -> locateFixed(SYMBOL_TOKENS[current] ?: SQL_IDENT)
        }
    }

    private fun locateFixed(token: IElementType, length: Int) {
        currentToken = token
        currentEnd = currentStart + length
    }

    private fun locateWhile(token: IElementType, predicate: (Char) -> Boolean) {
        currentToken = token
        currentEnd = currentStart
        while (currentEnd < endOffset && predicate(buffer[currentEnd])) {
            currentEnd++
        }
    }

    private fun isLineCommentStart(offset: Int): Boolean =
        offset + 1 < endOffset && buffer[offset] == '-' && buffer[offset + 1] == '-'

    private fun isBlockCommentStart(offset: Int): Boolean =
        offset + 1 < endOffset && buffer[offset] == '/' && buffer[offset + 1] == '*'

    private fun isBracedParameterStart(offset: Int): Boolean =
        offset + 1 < endOffset && buffer[offset] == '$' && buffer[offset + 1] == '{'

    private fun isParameterStart(offset: Int): Boolean =
        offset < endOffset && (buffer[offset] == '_' || buffer[offset].isLetter())

    private fun isParameterPart(char: Char): Boolean = char == '_' || char == '$' || char.isLetterOrDigit()

    private fun isVariablePart(char: Char): Boolean = char == '_' || char == '@' || char == '$' || char == '.' || char.isLetterOrDigit()

    private fun isIdentifierStart(char: Char): Boolean = char == '_' || char == '@' || char.isLetter()

    private fun isIdentifierPart(char: Char): Boolean = char == '_' || char == '@' || char == '$' || char.isLetterOrDigit()

    private companion object {
        const val SYMBOL_CHARS = "()[]{}.+-*/%=<>!|&:"

        val SYMBOL_TOKENS: Map<Char, IElementType> = mapOf(
            '(' to SQL_LEFT_PAREN,
            ')' to SQL_RIGHT_PAREN,
            '[' to SQL_LEFT_BRACKET,
            ']' to SQL_RIGHT_BRACKET,
            '{' to SQL_LEFT_BRACE,
            '}' to SQL_RIGHT_BRACE,
            '.' to SQL_PERIOD,
            '+' to SQL_OP_PLUS,
            '-' to SQL_OP_MINUS,
            '*' to SQL_ASTERISK,
            '/' to SQL_OP_DIV,
            '%' to SQL_OP_MODULO,
            '=' to SQL_OP_EQ,
            '<' to SQL_OP_LT,
            '>' to SQL_OP_GT,
            '!' to SQL_OP_NOT2,
            '|' to SQL_OP_BITWISE_OR,
            '&' to SQL_OP_BITWISE_AND,
            ':' to SQL_COLON
        )

        val FUNCTION_NAMES: Set<String> = StarRocksFunctionCatalog.BUILTIN_FUNCTION_NAMES

        val DATA_TYPE_NAMES: Set<String> = buildSet {
            addAll(StarRocksTypeSystem.SCALAR_TYPES)
            addAll(StarRocksTypeSystem.COMPLEX_TYPES)
            addAll(listOf("BOOL", "INTEGER", "DECIMAL", "DECIMALV2", "VARCHAR2", "TEXT"))
        }

        val FUNCTION_LIKE_KEYWORDS: Set<String> = setOf(
            "EXTRACT",
            "GROUPING",
            "GROUPING_ID",
            "PERCENTILE"
        )
    }
}

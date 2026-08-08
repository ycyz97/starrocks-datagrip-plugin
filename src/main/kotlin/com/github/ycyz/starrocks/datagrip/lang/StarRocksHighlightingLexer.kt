package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.database.StarRocksTypeSystem
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksFunctionNames
import com.intellij.psi.tree.IElementType
import com.intellij.psi.TokenType
import com.intellij.sql.psi.SqlTokens.SQL_IDENT
import com.intellij.sql.psi.SqlTokens.SQL_BLOCK_COMMENT
import com.intellij.sql.psi.SqlTokens.SQL_LINE_COMMENT
import com.intellij.sql.psi.SqlTokens.SQL_SEMICOLON
import com.intellij.sql.psi.SqlTokens.SQL_LEFT_PAREN
import java.util.Locale

class StarRocksHighlightingLexer : StarRocksLexer() {
    private var statementState = StatementState.START
    private var viewTargetPending = false

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        statementState = StatementState.START
        viewTargetPending = false
        super.start(buffer, startOffset, endOffset, initialState)
    }

    override fun advance() {
        val tokenType = parserTokenType()
        if (tokenType != null) {
            advanceStatementState(tokenType, tokenText().uppercase(Locale.ROOT))
        }
        super.advance()
    }

    override fun tokenTypeForParserToken(tokenType: IElementType): IElementType {
        val text = tokenText()
        val word = text.uppercase(Locale.ROOT)
        val followedByArguments = nextNonWhitespaceChar() == '('
        val isViewTarget = viewTargetPending && tokenType == SQL_IDENT && followedByArguments
        val highlighted = when {
            text.startsWith("@") -> StarRocksHighlightTokenTypes.VARIABLE
            followedByArguments && word in FUNCTION_NAMES -> StarRocksHighlightTokenTypes.FUNCTION
            followedByArguments && word in FUNCTION_LIKE_KEYWORDS -> StarRocksHighlightTokenTypes.FUNCTION
            followedByArguments && tokenType == SQL_IDENT && !isViewTarget ->
                StarRocksHighlightTokenTypes.FUNCTION
            word in DATA_TYPE_NAMES -> StarRocksHighlightTokenTypes.DATA_TYPE
            else -> tokenType
        }
        return highlighted
    }

    private fun advanceStatementState(tokenType: IElementType, word: String) {
        if (tokenType == SQL_SEMICOLON) {
            statementState = StatementState.START
            viewTargetPending = false
            return
        }
        if (tokenType == SQL_LEFT_PAREN && viewTargetPending) {
            viewTargetPending = false
            return
        }
        if (word == "AS" && viewTargetPending) {
            viewTargetPending = false
            statementState = StatementState.START
            return
        }
        if (tokenType == TokenType.WHITE_SPACE || tokenType == SQL_LINE_COMMENT ||
            tokenType == SQL_BLOCK_COMMENT || tokenType == com.intellij.sql.psi.SqlTokens.SQL_STRING_TOKEN) {
            return
        }
        if (viewTargetPending) return
        statementState = when (statementState) {
            StatementState.START -> if (word == "CREATE") StatementState.CREATE else StatementState.START
            StatementState.CREATE -> when (word) {
                "OR" -> StatementState.CREATE_OR
                "MATERIALIZED" -> StatementState.CREATE_MATERIALIZED
                "VIEW" -> { viewTargetPending = true; StatementState.VIEW; }
                else -> StatementState.START
            }
            StatementState.CREATE_OR -> if (word == "REPLACE") StatementState.CREATE_OR_REPLACE else StatementState.START
            StatementState.CREATE_OR_REPLACE -> if (word == "VIEW") { viewTargetPending = true; StatementState.VIEW } else StatementState.START
            StatementState.CREATE_MATERIALIZED -> if (word == "VIEW") { viewTargetPending = true; StatementState.VIEW } else StatementState.START
            StatementState.VIEW -> StatementState.VIEW
        }
    }

    private enum class StatementState { START, CREATE, CREATE_OR, CREATE_OR_REPLACE, CREATE_MATERIALIZED, VIEW }

    private companion object {
        val FUNCTION_NAMES: Set<String> = StarRocksFunctionNames.NAMES

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

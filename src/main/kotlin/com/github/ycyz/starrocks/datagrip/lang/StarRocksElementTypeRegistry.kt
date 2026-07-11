package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlCommonKeywords
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlTokenType
import com.intellij.sql.psi.SqlTokens
import com.intellij.sql.util.SqlTokenRegistry
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

internal object StarRocksElementTypeRegistry {
    private val tokens = ConcurrentHashMap<String, SqlTokenType>()

    fun token(text: String): SqlTokenType {
        val normalized = text.uppercase(Locale.ROOT)
        return tokens.computeIfAbsent(normalized, ::createToken)
    }

    fun elementType(name: String): IElementType {
        stubElementType(name)?.let { return it }
        platformCompositeElementType(name)?.let { return it }
        return StarRocksElementType("STARROCKS_$name")
    }

    private fun createToken(normalized: String): SqlTokenType {
        if (normalized == "STARROCKS_PARAMETER") {
            return StarRocksHighlightTokenTypes.PARAMETER
        }
        punctuationToken(normalized)?.let { return it }
        reflectedToken(SqlTokens::class.java, normalized)?.let { return it }
        reflectedToken(SqlCommonKeywords::class.java, "SQL_$normalized")?.let { return it }
        return SqlTokenRegistry.getType(normalized)
    }

    private fun punctuationToken(text: String): SqlTokenType? = when (text) {
        "(" -> SqlTokens.SQL_LEFT_PAREN
        ")" -> SqlTokens.SQL_RIGHT_PAREN
        "[" -> SqlTokens.SQL_LEFT_BRACKET
        "]" -> SqlTokens.SQL_RIGHT_BRACKET
        "{" -> SqlTokens.SQL_LEFT_BRACE
        "}" -> SqlTokens.SQL_RIGHT_BRACE
        "," -> SqlTokens.SQL_COMMA
        ";" -> SqlTokens.SQL_SEMICOLON
        "." -> SqlTokens.SQL_PERIOD
        ":" -> SqlTokens.SQL_COLON
        "+" -> SqlTokens.SQL_OP_PLUS
        "-" -> SqlTokens.SQL_OP_MINUS
        "*" -> SqlTokens.SQL_ASTERISK
        "/" -> SqlTokens.SQL_OP_DIV
        "%" -> SqlTokens.SQL_OP_MODULO
        "=" -> SqlTokens.SQL_OP_EQ
        "<" -> SqlTokens.SQL_OP_LT
        ">" -> SqlTokens.SQL_OP_GT
        "<=" -> SqlTokens.SQL_OP_LE
        ">=" -> SqlTokens.SQL_OP_GE
        "<>" -> SqlTokens.SQL_OP_NEQ
        "!=" -> SqlTokens.SQL_OP_NEQ2
        "<<" -> SqlTokens.SQL_OP_LEFT_SHIFT
        ">>" -> SqlTokens.SQL_OP_RIGHT_SHIFT
        "||" -> SqlTokens.SQL_OP_CONCAT
        "!" -> SqlTokens.SQL_OP_NOT2
        "|" -> SqlTokens.SQL_OP_BITWISE_OR
        "&" -> SqlTokens.SQL_OP_BITWISE_AND
        "?" -> StarRocksHighlightTokenTypes.PARAMETER
        else -> null
    }

    private fun reflectedToken(holder: Class<*>, name: String): SqlTokenType? = runCatching {
        holder.getField(name).get(null) as? SqlTokenType
    }.getOrNull()

    private fun platformCompositeElementType(name: String): IElementType? = runCatching {
        SqlCompositeElementTypes::class.java.getField(name).get(null) as? IElementType
    }.getOrNull()

    private fun stubElementType(name: String): IElementType? = when (name) {
        "COLUMN_NAME" -> StarRocksStubElementTypes.STARROCKS_COLUMN_NAME
        "CTE_COLUMN_NAME" -> StarRocksStubElementTypes.STARROCKS_CTE_COLUMN_NAME
        "TABLE_ALIAS" -> StarRocksStubElementTypes.STARROCKS_TABLE_ALIAS
        "TABLE_ALIAS_COLUMN_NAME" -> StarRocksStubElementTypes.STARROCKS_TABLE_ALIAS_COLUMN_NAME
        "WINDOW_NAME" -> StarRocksStubElementTypes.STARROCKS_WINDOW_NAME
        "SELECT_ALIAS" -> StarRocksStubElementTypes.STARROCKS_SELECT_ALIAS
        else -> null
    }
}

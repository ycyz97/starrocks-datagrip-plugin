package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.database.StarRocksTypeSystem
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksFunctionCatalog
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlTokens.SQL_IDENT
import java.util.Locale

class StarRocksHighlightingLexer : StarRocksLexer() {
    override fun tokenTypeForParserToken(tokenType: IElementType): IElementType {
        val text = tokenText()
        val word = text.uppercase(Locale.ROOT)
        val followedByArguments = nextNonWhitespaceChar() == '('
        return when {
            text.startsWith("@") -> StarRocksHighlightTokenTypes.VARIABLE
            followedByArguments && word in FUNCTION_NAMES -> StarRocksHighlightTokenTypes.FUNCTION
            followedByArguments && word in FUNCTION_LIKE_KEYWORDS -> StarRocksHighlightTokenTypes.FUNCTION
            followedByArguments && tokenType == SQL_IDENT -> StarRocksHighlightTokenTypes.FUNCTION
            word in DATA_TYPE_NAMES -> StarRocksHighlightTokenTypes.DATA_TYPE
            else -> tokenType
        }
    }

    private companion object {
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

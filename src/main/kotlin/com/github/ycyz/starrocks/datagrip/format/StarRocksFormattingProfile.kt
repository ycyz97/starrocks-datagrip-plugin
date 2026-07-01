package com.github.ycyz.starrocks.datagrip.format

import com.github.ycyz.starrocks.datagrip.lang.StarRocksLexer
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStatementFamily
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStatementWordsClassifier

object StarRocksFormattingProfile {
    val QUERY_CLAUSE_ORDER: List<String> = listOf(
        "WITH",
        "SELECT",
        "FROM",
        "WHERE",
        "GROUP BY",
        "HAVING",
        "QUALIFY",
        "ORDER BY",
        "LIMIT"
    )

    val TABLE_DDL_CLAUSE_ORDER: List<String> = listOf(
        "CREATE TABLE",
        "ENGINE",
        "PRIMARY KEY",
        "DUPLICATE KEY",
        "UNIQUE KEY",
        "AGGREGATE KEY",
        "COMMENT",
        "PARTITION BY",
        "DISTRIBUTED BY",
        "ORDER BY",
        "PROPERTIES",
        "AS SELECT"
    )

    val MATERIALIZED_VIEW_CLAUSE_ORDER: List<String> = listOf(
        "CREATE MATERIALIZED VIEW",
        "COMMENT",
        "PARTITION BY",
        "DISTRIBUTED BY",
        "ORDER BY",
        "REFRESH",
        "PROPERTIES",
        "AS SELECT"
    )

    val DML_CLAUSE_ORDER: List<String> = listOf(
        "INSERT",
        "UPDATE",
        "DELETE",
        "MERGE",
        "SET",
        "USING",
        "ON",
        "WHEN",
        "VALUES",
        "SELECT"
    )

    val LINE_BREAK_BEFORE_CLAUSES: Set<String> = (
        QUERY_CLAUSE_ORDER +
            TABLE_DDL_CLAUSE_ORDER +
            MATERIALIZED_VIEW_CLAUSE_ORDER +
            listOf("PROPERTIES", "REFRESH", "VALUES", "WHEN MATCHED", "WHEN NOT MATCHED")
        ).toSet()

    const val USE_PLATFORM_SQL_FORMATTER: Boolean = true
    const val USE_GENERIC_SQL_FORMATTER_BRIDGE: Boolean = true
    const val USE_SAFE_DDL_FORMATTER: Boolean = true
    const val USE_WHOLE_FILE_STRING_REWRITE: Boolean = false

    private val SAFE_FORMATTER_FAMILIES: Set<StarRocksStatementFamily> = setOf(
        StarRocksStatementFamily.TABLE_DDL,
        StarRocksStatementFamily.VIEW,
        StarRocksStatementFamily.MATERIALIZED_VIEW
    )

    private val STARROCKS_DDL_PHRASES: List<List<String>> = listOf(
        listOf("DISTRIBUTED", "BY"),
        listOf("PARTITION", "BY"),
        listOf("PRIMARY", "KEY"),
        listOf("DUPLICATE", "KEY"),
        listOf("UNIQUE", "KEY"),
        listOf("AGGREGATE", "KEY"),
        listOf("PROPERTIES"),
        listOf("CREATE", "VIEW"),
        listOf("CREATE", "MATERIALIZED", "VIEW"),
        listOf("REFRESH", "MATERIALIZED", "VIEW"),
        listOf("CANCEL", "REFRESH", "MATERIALIZED", "VIEW")
    )

    fun requiresSafeFormatter(sql: CharSequence): Boolean {
        val lexer = StarRocksLexer()
        val statementWords = mutableListOf<String>()
        lexer.start(sql)

        while (lexer.tokenType != null) {
            val tokenText = sql.substring(lexer.tokenStart, lexer.tokenEnd)
            if (tokenText == ";") {
                if (statementRequiresSafeFormatter(statementWords)) {
                    return true
                }
                statementWords.clear()
            } else if (tokenText.firstOrNull()?.isLetter() == true) {
                statementWords += tokenText.uppercase()
            }
            lexer.advance()
        }

        return statementRequiresSafeFormatter(statementWords)
    }

    private fun statementRequiresSafeFormatter(words: List<String>): Boolean {
        if (words.isEmpty()) {
            return false
        }
        val family = StarRocksStatementWordsClassifier.classify(words)
        return family in SAFE_FORMATTER_FAMILIES ||
            STARROCKS_DDL_PHRASES.any { phrase -> words.containsPhrase(phrase) }
    }

    private fun List<String>.containsPhrase(phrase: List<String>): Boolean {
        if (phrase.size > size) {
            return false
        }
        return windowed(phrase.size).any { it == phrase }
    }
}

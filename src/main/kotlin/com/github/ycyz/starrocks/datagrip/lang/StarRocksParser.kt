package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder
import com.intellij.sql.dialects.mysql.MysqlParser
import com.intellij.sql.psi.SqlElementTypes.SQL_INSERT_STATEMENT
import com.intellij.sql.psi.SqlElementTypes.SQL_STATEMENT
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect

class StarRocksParser : MysqlParser(StarRocksDialect.INSTANCE) {
    override fun parseSqlStatement(builder: PsiBuilder, level: Int): Boolean {
        if (isCreateMaterializedView(builder)) {
            return parseCreateMaterializedView(builder)
        }
        if (isCreateTableWithStarRocksClauses(builder)) {
            return parseCreateTableWithStarRocksClauses(builder)
        }
        if (isInsertOverwrite(builder)) {
            return parseInsertOverwrite(builder)
        }
        if (isStarRocksDmlStatement(builder)) {
            return parseLenientStarRocksStatement(builder)
        }
        if (isRefreshMaterializedView(builder)) {
            return parseRefreshMaterializedView(builder)
        }
        if (isCatalogManagementStatement(builder)) {
            return parseLenientStarRocksStatement(builder)
        }
        if (isResourceManagementStatement(builder)) {
            return parseLenientStarRocksStatement(builder)
        }
        if (isQueryWithStarRocksTableFunction(builder)) {
            return parseLenientStarRocksStatement(builder)
        }
        if (isQueryWithStarRocksAnalyticClause(builder)) {
            return parseLenientStarRocksStatement(builder)
        }
        if (hasStarRocksQueryTail(builder)) {
            return parseStarRocksStatementWithQueryTail(builder)
        }
        if (isStarRocksSpecificStatement(builder)) {
            return parseLenientStarRocksStatement(builder)
        }
        return super.parseSqlStatement(builder, level)
    }

    private fun isCreateMaterializedView(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        val result = consumeWord(builder, "CREATE") &&
            consumeWord(builder, "MATERIALIZED") &&
            consumeWord(builder, "VIEW")
        marker.rollbackTo()
        return result
    }

    private fun isCreateTableWithStarRocksClauses(builder: PsiBuilder): Boolean {
        return wordAt(builder, 0) == "CREATE" &&
            createTableKeywordOffset(builder) != null &&
            statementContainsAny(
                builder,
                "DISTRIBUTED",
                "BUCKETS",
                "PROPERTIES",
                "DUPLICATE",
                "AGGREGATE",
                "PRIMARY",
                "UNIQUE",
                "ENGINE",
                "OLAP",
                "ROLLUP",
                "RANDOM",
                "ORDER"
            )
    }

    private fun isInsertOverwrite(builder: PsiBuilder): Boolean {
        return wordAt(builder, 0) == "INSERT" && wordAt(builder, 1) == "OVERWRITE"
    }

    private fun isRefreshMaterializedView(builder: PsiBuilder): Boolean {
        return wordAt(builder, 0) == "REFRESH" &&
            wordAt(builder, 1) == "MATERIALIZED" &&
            wordAt(builder, 2) == "VIEW"
    }

    private fun isCatalogManagementStatement(builder: PsiBuilder): Boolean {
        return when (wordAt(builder, 0)) {
            "CREATE" -> wordAt(builder, 1) == "EXTERNAL" && wordAt(builder, 2) == "CATALOG"
            "ALTER", "DROP" -> wordAt(builder, 1) == "CATALOG"
            "SHOW" -> wordAt(builder, 1) == "CATALOGS" ||
                wordAt(builder, 1) == "CREATE" && wordAt(builder, 2) == "CATALOG"
            else -> false
        }
    }

    private fun isResourceManagementStatement(builder: PsiBuilder): Boolean {
        return when (wordAt(builder, 0)) {
            "CREATE", "ALTER", "DROP" -> wordAt(builder, 1) == "RESOURCE"
            "SHOW" -> wordAt(builder, 1) == "RESOURCES"
            else -> false
        }
    }

    private fun isQueryWithStarRocksTableFunction(builder: PsiBuilder): Boolean {
        if (!isQueryStart(builder)) return false
        return statementContainsAny(builder, "UNNEST", "LATERAL")
    }

    private fun isQueryWithStarRocksAnalyticClause(builder: PsiBuilder): Boolean {
        if (!isQueryStart(builder)) return false
        return statementContainsAny(builder, "ROLLUP", "CUBE") ||
            statementContainsSequence(builder, "GROUPING", "SETS")
    }

    private fun statementContainsSequence(builder: PsiBuilder, vararg words: String): Boolean {
        if (words.isEmpty()) return false

        val marker = builder.mark()
        var scanned = 0
        var matched = 0
        var found = false
        while (!builder.eof() && builder.tokenText != ";" && scanned < MAX_LOOKAHEAD_TOKENS) {
            val text = builder.tokenText
            if (text != null && text.firstOrNull()?.isLetter() == true) {
                if (text.equals(words[matched], ignoreCase = true)) {
                    matched++
                    if (matched == words.size) {
                        found = true
                        break
                    }
                } else {
                    matched = if (text.equals(words[0], ignoreCase = true)) 1 else 0
                }
            }
            builder.advanceLexer()
            scanned++
        }
        marker.rollbackTo()
        return found
    }

    private fun hasStarRocksQueryTail(builder: PsiBuilder): Boolean {
        val first = wordAt(builder, 0)
        val second = wordAt(builder, 1)
        return when {
            first == "EXPORT" && second in setOf("TABLE", "DATABASE") -> statementContainsAny(builder, "SELECT", "WITH")
            first == "CREATE" && second in QUERY_TAIL_CREATE_TARGETS -> statementContainsAny(builder, "AS", "SELECT", "WITH")
            first == "SUBMIT" && second == "TASK" -> statementContainsAny(builder, "AS", "INSERT", "SELECT", "WITH")
            first == "ALTER" && statementContainsAny(builder, "AS", "SELECT", "WITH") -> true
            else -> false
        }
    }

    private fun parseCreateMaterializedView(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        parseUntilQueryTail(builder, "AS")
        marker.done(SQL_STATEMENT)
        return true
    }

    private fun isStarRocksSpecificStatement(builder: PsiBuilder): Boolean {
        return when (wordAt(builder, 0)) {
            "ADMIN" -> true
            "ALTER" -> statementContainsAny(
                builder,
                "MATERIALIZED",
                "ROLLUP",
                "CATALOG",
                "RESOURCE",
                "SET",
                "UNSET",
                "DISTRIBUTED",
                "BUCKETS",
                "PROPERTIES",
                "PARTITION"
            )
            "BACKUP", "RESTORE", "RECOVER", "SYNC" -> true
            "CANCEL" -> wordAt(builder, 1) in setOf("LOAD", "EXPORT", "ALTER", "DECOMMISSION", "REFRESH")
            "CLEAN" -> wordAt(builder, 1) in setOf("TEMPORARY", "TRASH")
            "CREATE" -> isStarRocksCreateStatement(builder)
            "DROP" -> wordAt(builder, 1) in setOf(
                "MATERIALIZED",
                "RESOURCE",
                "CATALOG",
                "REPOSITORY",
                "WAREHOUSE",
                "STORAGE",
                "PIPE",
                "ROUTINE"
            )
            "EXPORT" -> wordAt(builder, 1) in setOf("TABLE", "DATABASE")
            "INSERT" -> wordAt(builder, 1) == "OVERWRITE"
            "LOAD" -> wordAt(builder, 1) == "LABEL"
            "PAUSE", "RESUME", "STOP" -> wordAt(builder, 1) in setOf("ROUTINE", "PIPE")
            "REFRESH" -> wordAt(builder, 1) == "MATERIALIZED"
            "SHOW" -> statementContainsAny(
                builder,
                "MATERIALIZED",
                "ROUTINE",
                "RESOURCES",
                "CATALOGS",
                "BACKENDS",
                "FRONTENDS",
                "PROC",
                "TABLET",
                "PARTITIONS",
                "WAREHOUSES",
                "LOAD",
                "ROUTINES",
                "PIPE",
                "PIPES",
                "TRANSACTION",
                "TEMPORARY"
            )
            "SUBMIT" -> wordAt(builder, 1) == "TASK"
            "UPDATE" -> isStarRocksUpdateStatement(builder)
            "DELETE" -> isStarRocksDeleteStatement(builder)
            "MERGE" -> wordAt(builder, 1) == "INTO"
            else -> false
        }
    }

    private fun isStarRocksDmlStatement(builder: PsiBuilder): Boolean {
        return when (wordAt(builder, 0)) {
            "UPDATE" -> isStarRocksUpdateStatement(builder)
            "DELETE" -> isStarRocksDeleteStatement(builder)
            "MERGE" -> wordAt(builder, 1) == "INTO"
            else -> false
        }
    }

    private fun isStarRocksUpdateStatement(builder: PsiBuilder): Boolean {
        return wordAt(builder, 0) == "UPDATE" &&
            statementContainsAny(builder, "SET") &&
            statementContainsAny(builder, "FROM")
    }

    private fun isStarRocksDeleteStatement(builder: PsiBuilder): Boolean {
        return wordAt(builder, 0) == "DELETE" &&
            statementContainsAny(builder, "FROM", "USING")
    }

    private fun isStarRocksCreateStatement(builder: PsiBuilder): Boolean {
        val second = wordAt(builder, 1)
        val third = wordAt(builder, 2)
        if (second == "MATERIALIZED" && third == "VIEW") return true
        if (second == "ROUTINE" && third == "LOAD") return true
        if (createTableKeywordOffset(builder) != null) {
            return statementContainsAny(
                builder,
                "DISTRIBUTED",
                "BUCKETS",
                "PROPERTIES",
                "DUPLICATE",
                "AGGREGATE",
                "PRIMARY",
                "UNIQUE",
                "ENGINE",
                "OLAP",
                "ROLLUP",
                "RANDOM",
                "ORDER"
            )
        }
        if (second in setOf(
                "RESOURCE",
                "CATALOG",
                "EXTERNAL",
                "WAREHOUSE",
                "STORAGE",
                "PIPE",
                "FILE",
                "REPOSITORY",
                "TASK",
                "DICTIONARY",
                "ANALYZE",
                "MATERIALIZED"
            )
        ) {
            return true
        }
        return false
    }

    private fun createTableKeywordOffset(builder: PsiBuilder): Int? {
        if (wordAt(builder, 0) != "CREATE") return null
        for (offset in 1..4) {
            val word = wordAt(builder, offset) ?: return null
            if (word == "TABLE") return offset
            if (word !in CREATE_TABLE_MODIFIERS) return null
        }
        return null
    }

    private fun parseLenientStarRocksStatement(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        while (!builder.eof() && builder.tokenText != ";") {
            builder.advanceLexer()
        }
        marker.done(SQL_STATEMENT)
        return true
    }

    private fun parseCreateTableWithStarRocksClauses(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        parseUntilQueryTail(builder, "AS")
        marker.done(SQL_STATEMENT)
        return true
    }

    private fun parseInsertOverwrite(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        consumeWord(builder, "INSERT")
        consumeWord(builder, "OVERWRITE")
        parseUntilQueryTail(builder)
        marker.done(SQL_INSERT_STATEMENT)
        return true
    }

    private fun parseRefreshMaterializedView(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        parseUntilQueryTail(builder, "AS")
        marker.done(SQL_STATEMENT)
        return true
    }

    private fun parseStarRocksStatementWithQueryTail(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        parseUntilQueryTail(builder, "AS")
        marker.done(SQL_STATEMENT)
        return true
    }

    private fun parseUntilQueryTail(builder: PsiBuilder, vararg queryIntroducers: String) {
        while (!builder.eof() && builder.tokenText != ";") {
            if (queryIntroducers.any { isCurrentWord(builder, it) }) {
                builder.advanceLexer()
                parseQueryOrConsumeRemainder(builder)
                return
            }
            if (isCurrentWord(builder, "INSERT")) {
                parseStatementTailOrConsumeRemainder(builder)
                return
            }
            if (isQueryStart(builder)) {
                parseQueryOrConsumeRemainder(builder)
                return
            }
            builder.advanceLexer()
        }
    }

    private fun parseStatementTailOrConsumeRemainder(builder: PsiBuilder) {
        if (!parseSqlStatement(builder, 0)) {
            consumeStatementRemainder(builder)
        }
        consumeStatementRemainder(builder)
    }

    private fun parseQueryOrConsumeRemainder(builder: PsiBuilder) {
        if (isQueryStart(builder)) {
            parseQueryExpression(builder, 0)
        }
        consumeStatementRemainder(builder)
    }

    private fun consumeStatementRemainder(builder: PsiBuilder) {
        while (!builder.eof() && builder.tokenText != ";") {
            builder.advanceLexer()
        }
    }

    private fun statementContainsAny(builder: PsiBuilder, vararg words: String): Boolean {
        val expectedWords = words.toSet()
        val marker = builder.mark()
        var scanned = 0
        var found = false
        while (!builder.eof() && builder.tokenText != ";" && scanned < MAX_LOOKAHEAD_TOKENS) {
            val text = builder.tokenText
            if (text != null && expectedWords.contains(text.uppercase())) {
                found = true
                break
            }
            builder.advanceLexer()
            scanned++
        }
        marker.rollbackTo()
        return found
    }

    private fun wordAt(builder: PsiBuilder, offset: Int): String? {
        val marker = builder.mark()
        var current = 0
        var result: String? = null
        while (!builder.eof() && builder.tokenText != ";" && current <= offset) {
            val text = builder.tokenText
            if (text != null && text.firstOrNull()?.isLetter() == true) {
                if (current == offset) {
                    result = text.uppercase()
                    break
                }
                current++
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun consumeWord(builder: PsiBuilder, expected: String): Boolean {
        if (!builder.tokenText.equals(expected, ignoreCase = true)) return false
        builder.advanceLexer()
        return true
    }

    private fun isCurrentWord(builder: PsiBuilder, expected: String): Boolean =
        builder.tokenText.equals(expected, ignoreCase = true)

    private fun isQueryStart(builder: PsiBuilder): Boolean =
        isCurrentWord(builder, "SELECT") || isCurrentWord(builder, "WITH")

    private companion object {
        const val MAX_LOOKAHEAD_TOKENS = 512
        val CREATE_TABLE_MODIFIERS = setOf("TEMPORARY", "EXTERNAL")
        val QUERY_TAIL_CREATE_TARGETS = setOf("PIPE", "TASK", "DICTIONARY", "ANALYZE")
    }
}

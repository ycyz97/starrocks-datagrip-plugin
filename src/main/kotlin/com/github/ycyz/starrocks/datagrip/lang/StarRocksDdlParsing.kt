package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlCompositeElementTypes

object StarRocksDdlParsing {
    @JvmStatic
    fun type_element(builder: PsiBuilder, level: Int): Boolean {
        val word = StarRocksParsingUtil.word(builder)
        if (word !in COMPLEX_TYPE_WORDS) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (builder.tokenText == "<" || builder.tokenText == "(") {
            StarRocksParsingUtil.consumeBalancedTail(builder, TYPE_BOUNDARIES)
            if (builder.tokenText == ">" || builder.tokenText == ")") {
                builder.advanceLexer()
            }
        }
        marker.done(SqlCompositeElementTypes.SQL_TYPE_ELEMENT)
        return true
    }

    @JvmStatic
    fun type_element_ext(builder: PsiBuilder, level: Int): Boolean = type_element(builder, level)

    @JvmStatic
    fun properties_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "PROPERTIES")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (builder.tokenText == "(") {
            builder.advanceLexer()
            parsePropertyPairs(builder)
            if (builder.tokenText == ")") {
                builder.advanceLexer()
            }
        }
        marker.done(StarRocksElementTypes.PROPERTIES_CLAUSE)
        return true
    }

    @JvmStatic
    fun create_table_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "CREATE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        consumeUntilWord(builder, "TABLE") ?: return marker.rollbackFalse()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        consumeOptionalWords(builder, "IF", "NOT", "EXISTS")
        StarRocksParsingUtil.skipNoise(builder)
        parseTableName(builder)
        StarRocksParsingUtil.skipNoise(builder)
        table_column_list(builder, level + 1)
        while (!builder.eof() && builder.tokenText != ";") {
            when {
                key_model_clause(builder, level + 1) -> continue
                partition_clause(builder, level + 1) -> continue
                distribution_clause(builder, level + 1) -> continue
                buckets_clause(builder, level + 1) -> continue
                comment_clause(builder, level + 1) -> continue
                table_column_list(builder, level + 1) -> continue
                properties_clause(builder, level + 1) -> continue
                type_element(builder, level + 1) -> continue
            }
            builder.advanceLexer()
        }
        marker.done(SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT)
        return true
    }

    @JvmStatic
    fun key_model_clause(builder: PsiBuilder, level: Int): Boolean {
        if (StarRocksParsingUtil.word(builder) !in KEY_MODEL_WORDS) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "KEY")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        parseKeyColumnList(builder)
        marker.done(StarRocksElementTypes.KEY_MODEL_CLAUSE)
        return true
    }

    @JvmStatic
    fun partition_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "PARTITION")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "BY")) {
            return marker.rollbackFalse()
        }
        parseClauseExpression(builder, StarRocksElementTypes.PARTITION_EXPRESSION, DDL_CLAUSE_BOUNDARIES)
        marker.done(StarRocksElementTypes.PARTITION_CLAUSE)
        return true
    }

    @JvmStatic
    fun distribution_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "DISTRIBUTED")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "BY")) {
            return marker.rollbackFalse()
        }
        parseClauseExpression(builder, StarRocksElementTypes.DISTRIBUTION_EXPRESSION, DISTRIBUTION_BOUNDARIES)
        marker.done(StarRocksElementTypes.DISTRIBUTION_CLAUSE)
        return true
    }

    @JvmStatic
    fun buckets_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "BUCKETS")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.consumeBalancedTail(builder, DDL_CLAUSE_BOUNDARIES)
        marker.done(StarRocksElementTypes.BUCKETS_CLAUSE)
        return true
    }

    @JvmStatic
    fun comment_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "COMMENT")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.consumeBalancedTail(builder, DDL_CLAUSE_BOUNDARIES)
        marker.done(StarRocksElementTypes.COMMENT_CLAUSE)
        return true
    }

    @JvmStatic
    fun refresh_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "REFRESH")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.consumeBalancedTail(builder, DDL_CLAUSE_BOUNDARIES)
        marker.done(StarRocksElementTypes.REFRESH_CLAUSE)
        return true
    }

    @JvmStatic
    fun table_column_list(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        while (!builder.eof() && builder.tokenText != ")") {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                StarRocksParsingUtil.word(builder) in COLUMN_DEFINITION_SKIP_WORDS -> consumeTableConstraint(builder)
                column_definition(builder, level + 1) -> continue
                else -> builder.advanceLexer()
            }
        }
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.TABLE_COLUMN_LIST)
        return true
    }

    @JvmStatic
    fun column_definition(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.isIdentifier(builder) || StarRocksParsingUtil.word(builder) in COLUMN_DEFINITION_SKIP_WORDS) {
            return false
        }
        val marker = builder.mark()
        val columnName = builder.mark()
        builder.advanceLexer()
        columnName.done(StarRocksElementTypes.COLUMN_NAME)
        StarRocksParsingUtil.skipNoise(builder)
        parseColumnType(builder)
        StarRocksParsingUtil.consumeBalancedTail(builder, setOf(","))
        marker.done(StarRocksElementTypes.COLUMN_DEFINITION)
        return true
    }

    @JvmStatic
    fun create_materialized_view_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "CREATE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        consumeUntilWord(builder, "MATERIALIZED") ?: return marker.rollbackFalse()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "VIEW")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeQualifiedIdentifier(builder)
        while (!builder.eof() && builder.tokenText != ";") {
            when {
                partition_clause(builder, level + 1) -> continue
                distribution_clause(builder, level + 1) -> continue
                buckets_clause(builder, level + 1) -> continue
                refresh_clause(builder, level + 1) -> continue
                comment_clause(builder, level + 1) -> continue
                properties_clause(builder, level + 1) -> continue
                type_element(builder, level + 1) -> continue
                StarRocksParsingUtil.tokenIs(builder, "AS") -> {
                    val asMarker = builder.mark()
                    builder.advanceLexer()
                    StarRocksDmlParsing.top_query_expression(builder, level + 1)
                    asMarker.done(StarRocksElementTypes.AS_SELECT_CLAUSE)
                    continue
                }
            }
            builder.advanceLexer()
        }
        marker.done(SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT)
        return true
    }

    @JvmStatic
    fun create_view_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "CREATE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        consumeOptionalWords(builder, "OR", "REPLACE")
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.tokenIs(builder, "MATERIALIZED") || !StarRocksParsingUtil.consumeWord(builder, "VIEW")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        consumeOptionalWords(builder, "IF", "NOT", "EXISTS")
        StarRocksParsingUtil.skipNoise(builder)
        parseTableName(builder)
        StarRocksParsingUtil.skipNoise(builder)
        table_column_list(builder, level + 1)
        while (!builder.eof() && builder.tokenText != ";") {
            when {
                comment_clause(builder, level + 1) -> continue
                table_column_list(builder, level + 1) -> continue
                StarRocksParsingUtil.tokenIs(builder, "AS") -> {
                    val asMarker = builder.mark()
                    builder.advanceLexer()
                    StarRocksDmlParsing.top_query_expression(builder, level + 1)
                    asMarker.done(StarRocksElementTypes.AS_SELECT_CLAUSE)
                    continue
                }
            }
            builder.advanceLexer()
        }
        marker.done(SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT)
        return true
    }

    private fun parseTableName(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.isIdentifier(builder)) {
            return false
        }
        val marker = builder.mark()
        val parsed = StarRocksParsingUtil.consumeQualifiedIdentifier(builder)
        return if (parsed) {
            marker.done(StarRocksElementTypes.TABLE_NAME)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun consumeOptionalWords(builder: PsiBuilder, vararg words: String): Boolean {
        val marker = builder.mark()
        words.forEach { word ->
            StarRocksParsingUtil.skipNoise(builder)
            if (!StarRocksParsingUtil.consumeWord(builder, word)) {
                marker.rollbackTo()
                return false
            }
        }
        marker.drop()
        return true
    }

    private fun consumeTableConstraint(builder: PsiBuilder) {
        StarRocksParsingUtil.consumeBalancedTail(builder, setOf(","))
    }

    private fun parseKeyColumnList(builder: PsiBuilder): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        builder.advanceLexer()
        while (!builder.eof() && builder.tokenText != ")") {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                StarRocksParsingUtil.isIdentifier(builder) -> {
                    val column = builder.mark()
                    StarRocksParsingUtil.consumeQualifiedIdentifier(builder)
                    column.done(StarRocksElementTypes.KEY_COLUMN)
                }
                else -> builder.advanceLexer()
            }
        }
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        return true
    }

    private fun parseClauseExpression(
        builder: PsiBuilder,
        elementType: IElementType,
        stopWords: Set<String>
    ): Boolean {
        StarRocksParsingUtil.skipNoise(builder)
        val marker = builder.mark()
        val before = builder.currentOffset
        StarRocksParsingUtil.consumeBalancedTail(builder, stopWords)
        return if (builder.currentOffset > before) {
            marker.done(elementType)
            true
        } else {
            marker.drop()
            false
        }
    }

    private fun parseColumnType(builder: PsiBuilder): Boolean {
        if (type_element(builder, 0)) {
            return true
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        var parenDepth = 0
        var angleDepth = 0
        while (!builder.eof()) {
            val text = builder.tokenText
            val upper = text?.uppercase()
            if (parenDepth == 0 && angleDepth == 0 && (text == "," || text == ")" || upper in COLUMN_ATTRIBUTE_STARTERS)) {
                break
            }
            when (text) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth-- else break
                "<" -> angleDepth++
                ">" -> if (angleDepth > 0) angleDepth--
            }
            builder.advanceLexer()
        }
        return if (builder.currentOffset > before) {
            marker.done(SqlCompositeElementTypes.SQL_TYPE_ELEMENT)
            true
        } else {
            marker.drop()
            false
        }
    }

    private fun parsePropertyPairs(builder: PsiBuilder) {
        while (!builder.eof() && builder.tokenText != ")") {
            if (builder.tokenText == ",") {
                builder.advanceLexer()
                continue
            }
            val pair = builder.mark()
            val key = builder.mark()
            StarRocksParsingUtil.consumeBalancedTail(builder, setOf("="))
            key.done(StarRocksElementTypes.PROPERTY_KEY)
            if (builder.tokenText == "=") {
                builder.advanceLexer()
            }
            val value = builder.mark()
            StarRocksParsingUtil.consumeBalancedTail(builder, setOf(","))
            value.done(StarRocksElementTypes.PROPERTY_VALUE)
            pair.done(StarRocksElementTypes.PROPERTY_PAIR)
        }
    }

    private fun consumeUntilWord(builder: PsiBuilder, word: String): PsiBuilder? {
        while (!builder.eof() && builder.tokenText != ";") {
            if (StarRocksParsingUtil.tokenIs(builder, word)) {
                return builder
            }
            builder.advanceLexer()
        }
        return null
    }

    private fun PsiBuilder.Marker.rollbackFalse(): Boolean {
        rollbackTo()
        return false
    }

    private val COMPLEX_TYPE_WORDS = setOf("ARRAY", "MAP", "STRUCT", "JSON")

    private val COLUMN_DEFINITION_SKIP_WORDS = setOf(
        "AGGREGATE",
        "DUPLICATE",
        "INDEX",
        "KEY",
        "PRIMARY",
        "UNIQUE"
    )

    private val COLUMN_ATTRIBUTE_STARTERS = setOf(
        "AGGREGATE",
        "AUTO_INCREMENT",
        "COMMENT",
        "DEFAULT",
        "KEY",
        "NOT",
        "NULL"
    )

    private val KEY_MODEL_WORDS = setOf("PRIMARY", "DUPLICATE", "UNIQUE", "AGGREGATE")

    private val DDL_CLAUSE_BOUNDARIES = setOf(
        "PRIMARY",
        "DUPLICATE",
        "UNIQUE",
        "AGGREGATE",
        "COMMENT",
        "PARTITION",
        "DISTRIBUTED",
        "BUCKETS",
        "ORDER",
        "PROPERTIES",
        "REFRESH",
        "AS"
    )

    private val DISTRIBUTION_BOUNDARIES = DDL_CLAUSE_BOUNDARIES + "BUCKETS"

    private val TYPE_BOUNDARIES = setOf(
        ",",
        "NULL",
        "NOT",
        "DEFAULT",
        "COMMENT",
        "AGGREGATE",
        "KEY",
        "PRIMARY",
        "DUPLICATE",
        "UNIQUE",
        "REPLACE"
    )
}

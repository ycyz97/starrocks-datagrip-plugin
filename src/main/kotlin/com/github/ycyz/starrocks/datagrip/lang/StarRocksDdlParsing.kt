package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.base.SqlGeneratedParserUtil
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlReferenceElementType

object StarRocksDdlParsing {
    @JvmStatic
    fun ddl_statement(builder: PsiBuilder, level: Int): Boolean =
        create_statement(builder, level + 1) ||
            alter_statement(builder, level + 1) ||
            drop_statement(builder, level + 1) ||
            truncate_table_statement(builder, level + 1) ||
            refresh_materialized_view_statement(builder, level + 1) ||
            grant_statement(builder, level + 1) ||
            revoke_statement(builder, level + 1)

    @JvmStatic
    fun create_statement(builder: PsiBuilder, level: Int): Boolean =
        create_materialized_view_statement(builder, level + 1) ||
            create_view_statement(builder, level + 1) ||
            create_table_statement(builder, level + 1) ||
            create_catalog_statement(builder, level + 1) ||
            create_resource_statement(builder, level + 1) ||
            create_routine_load_statement(builder, level + 1) ||
            create_repository_statement(builder, level + 1) ||
            create_user_statement(builder, level + 1) ||
            create_role_statement(builder, level + 1) ||
            create_database_statement(builder, level + 1) ||
            create_schema_statement(builder, level + 1) ||
            create_index_statement(builder, level + 1)

    @JvmStatic
    fun alter_statement(builder: PsiBuilder, level: Int): Boolean =
        alter_user_statement(builder, level + 1) ||
            alter_role_statement(builder, level + 1) ||
            alter_database_statement(builder, level + 1) ||
            alter_schema_statement(builder, level + 1) ||
            alter_table_statement(builder, level + 1) ||
            alter_view_statement(builder, level + 1) ||
            alter_materialized_view_statement(builder, level + 1) ||
            alter_catalog_statement(builder, level + 1) ||
            alter_resource_statement(builder, level + 1) ||
            alter_routine_load_statement(builder, level + 1)

    @JvmStatic
    fun drop_statement(builder: PsiBuilder, level: Int): Boolean =
        drop_user_statement(builder, level + 1) ||
            drop_role_statement(builder, level + 1) ||
            drop_database_statement(builder, level + 1) ||
            drop_schema_statement(builder, level + 1) ||
            drop_index_statement(builder, level + 1) ||
            drop_table_statement(builder, level + 1) ||
            drop_view_statement(builder, level + 1) ||
            drop_materialized_view_statement(builder, level + 1) ||
            drop_catalog_statement(builder, level + 1) ||
            drop_resource_statement(builder, level + 1) ||
            drop_repository_statement(builder, level + 1)

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
        parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)
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
        parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_MATERIALIZED_VIEW_REFERENCE)
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
        parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_VIEW_REFERENCE)
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

    @JvmStatic
    fun create_catalog_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_CREATE_CATALOG_STATEMENT, "CREATE", "CATALOG") ||
            prefixedStatement(builder, SqlCompositeElementTypes.SQL_CREATE_CATALOG_STATEMENT, "CREATE", "EXTERNAL", "CATALOG")

    @JvmStatic
    fun create_resource_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.RESOURCE_STATEMENT, "CREATE", "RESOURCE")

    @JvmStatic
    fun create_routine_load_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.ROUTINE_LOAD_STATEMENT, "CREATE", "ROUTINE", "LOAD")

    @JvmStatic
    fun create_repository_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.BACKUP_RESTORE_STATEMENT, "CREATE", "REPOSITORY")

    @JvmStatic
    fun create_user_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.CREATE_USER_STATEMENT, "CREATE", "USER")

    @JvmStatic
    fun create_role_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.CREATE_ROLE_STATEMENT, "CREATE", "ROLE")

    @JvmStatic
    fun create_database_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_CREATE_SCHEMA_STATEMENT, "CREATE", "DATABASE")

    @JvmStatic
    fun create_schema_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_CREATE_SCHEMA_STATEMENT, "CREATE", "SCHEMA")

    @JvmStatic
    fun create_index_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_CREATE_INDEX_STATEMENT, "CREATE", "INDEX") ||
            prefixedStatement(builder, SqlCompositeElementTypes.SQL_CREATE_INDEX_STATEMENT, "CREATE", "BITMAP", "INDEX")

    @JvmStatic
    fun alter_user_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.ALTER_USER_STATEMENT, "ALTER", "USER")

    @JvmStatic
    fun alter_role_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.ALTER_ROLE_STATEMENT, "ALTER", "ROLE")

    @JvmStatic
    fun alter_database_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_ALTER_SCHEMA_STATEMENT, "ALTER", "DATABASE")

    @JvmStatic
    fun alter_schema_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_ALTER_SCHEMA_STATEMENT, "ALTER", "SCHEMA")

    @JvmStatic
    fun alter_table_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_ALTER_TABLE_STATEMENT, "ALTER", "TABLE")

    @JvmStatic
    fun alter_view_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_ALTER_VIEW_STATEMENT, "ALTER", "VIEW")

    @JvmStatic
    fun alter_materialized_view_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT, "ALTER", "MATERIALIZED", "VIEW")

    @JvmStatic
    fun alter_catalog_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_ALTER_CATALOG_STATEMENT, "ALTER", "CATALOG")

    @JvmStatic
    fun alter_resource_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.RESOURCE_STATEMENT, "ALTER", "RESOURCE")

    @JvmStatic
    fun alter_routine_load_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.ROUTINE_LOAD_STATEMENT, "ALTER", "ROUTINE", "LOAD")

    @JvmStatic
    fun drop_user_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.DROP_USER_STATEMENT, "DROP", "USER")

    @JvmStatic
    fun drop_role_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.DROP_ROLE_STATEMENT, "DROP", "ROLE")

    @JvmStatic
    fun drop_database_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.SCHEMA_STATEMENT, "DROP", "DATABASE")

    @JvmStatic
    fun drop_schema_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.SCHEMA_STATEMENT, "DROP", "SCHEMA")

    @JvmStatic
    fun drop_index_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.INDEX_STATEMENT, "DROP", "INDEX")

    @JvmStatic
    fun drop_table_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.TABLE_DDL_STATEMENT, "DROP", "TABLE")

    @JvmStatic
    fun drop_view_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.VIEW_STATEMENT, "DROP", "VIEW")

    @JvmStatic
    fun drop_materialized_view_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT, "DROP", "MATERIALIZED", "VIEW")

    @JvmStatic
    fun drop_catalog_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.CATALOG_STATEMENT, "DROP", "CATALOG")

    @JvmStatic
    fun drop_resource_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.RESOURCE_STATEMENT, "DROP", "RESOURCE")

    @JvmStatic
    fun drop_repository_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.BACKUP_RESTORE_STATEMENT, "DROP", "REPOSITORY")

    @JvmStatic
    fun truncate_table_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_TRUNCATE_TABLE_STATEMENT, "TRUNCATE", "TABLE")

    @JvmStatic
    fun refresh_materialized_view_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT, "REFRESH", "MATERIALIZED", "VIEW") ||
            prefixedStatement(builder, StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT, "CANCEL", "REFRESH", "MATERIALIZED", "VIEW")

    @JvmStatic
    fun grant_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.GRANT_STATEMENT, "GRANT")

    @JvmStatic
    fun revoke_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.REVOKE_STATEMENT, "REVOKE")

    private fun parseDefinitionReference(
        builder: PsiBuilder,
        level: Int,
        referenceType: SqlReferenceElementType
    ): Boolean {
        if (!StarRocksParsingUtil.isIdentifier(builder)) {
            return false
        }
        return SqlGeneratedParserUtil.parseReference(
            builder,
            level + 1,
            referenceType
        )
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

    private fun prefixedStatement(builder: PsiBuilder, elementType: IElementType, vararg words: String): Boolean {
        if (!hasWords(builder, *words)) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        while (!builder.eof() && builder.tokenText != ";") {
            when {
                properties_clause(builder, 0) -> continue
                type_element(builder, 0) -> continue
                StarRocksDmlParsing.qualify_clause(builder, 0) -> continue
                StarRocksDmlParsing.table_function_call(builder, 0) -> continue
                else -> builder.advanceLexer()
            }
        }
        return if (builder.currentOffset > before) {
            marker.done(elementType)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun hasWords(builder: PsiBuilder, vararg words: String): Boolean {
        return words.indices.all { index -> wordAt(builder, index) == words[index] }
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

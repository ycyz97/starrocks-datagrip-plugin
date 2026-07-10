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
    fun create_catalog_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "CREATE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "EXTERNAL")
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "CATALOG")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_CATALOG_REFERENCE)) {
            return marker.rollbackFalse()
        }
        consumeCatalogTail(builder, level + 1)
        marker.done(SqlCompositeElementTypes.SQL_CREATE_CATALOG_STATEMENT)
        return true
    }

    @JvmStatic
    fun create_resource_statement(builder: PsiBuilder, level: Int): Boolean {
        return resource_statement(builder, level + 1, StarRocksElementTypes.RESOURCE_STATEMENT, "CREATE", "RESOURCE")
    }

    @JvmStatic
    fun create_routine_load_statement(builder: PsiBuilder, level: Int): Boolean {
        return routine_load_statement(builder, level + 1, "CREATE")
    }

    @JvmStatic
    fun create_repository_statement(builder: PsiBuilder, level: Int): Boolean {
        return repository_statement(builder, level + 1, create = true)
    }

    @JvmStatic
    fun create_user_statement(builder: PsiBuilder, level: Int): Boolean {
        return principal_statement(builder, level + 1, StarRocksElementTypes.CREATE_USER_STATEMENT, "CREATE", "USER")
    }

    @JvmStatic
    fun create_role_statement(builder: PsiBuilder, level: Int): Boolean {
        return principal_statement(builder, level + 1, StarRocksElementTypes.CREATE_ROLE_STATEMENT, "CREATE", "ROLE")
    }

    @JvmStatic
    fun create_database_statement(builder: PsiBuilder, level: Int): Boolean {
        return create_schema_like_statement(builder, level + 1, "DATABASE")
    }

    @JvmStatic
    fun create_schema_statement(builder: PsiBuilder, level: Int): Boolean {
        return create_schema_like_statement(builder, level + 1, "SCHEMA")
    }

    @JvmStatic
    fun create_index_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "CREATE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "BITMAP")
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "INDEX")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_INDEX_REFERENCE)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "ON")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        parseIndexColumnList(builder, level + 1)
        while (!builder.eof() && builder.tokenText != ";") {
            when {
                properties_clause(builder, level + 1) -> continue
                comment_clause(builder, level + 1) -> continue
                else -> builder.advanceLexer()
            }
        }
        marker.done(SqlCompositeElementTypes.SQL_CREATE_INDEX_STATEMENT)
        return true
    }

    @JvmStatic
    fun alter_user_statement(builder: PsiBuilder, level: Int): Boolean {
        return principal_statement(builder, level + 1, StarRocksElementTypes.ALTER_USER_STATEMENT, "ALTER", "USER")
    }

    @JvmStatic
    fun alter_role_statement(builder: PsiBuilder, level: Int): Boolean {
        return principal_statement(builder, level + 1, StarRocksElementTypes.ALTER_ROLE_STATEMENT, "ALTER", "ROLE")
    }

    @JvmStatic
    fun alter_database_statement(builder: PsiBuilder, level: Int): Boolean {
        return alter_schema_like_statement(builder, level + 1, "DATABASE")
    }

    @JvmStatic
    fun alter_schema_statement(builder: PsiBuilder, level: Int): Boolean {
        return alter_schema_like_statement(builder, level + 1, "SCHEMA")
    }

    @JvmStatic
    fun alter_table_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "ALTER", "TABLE")) {
            return false
        }
        val marker = builder.mark()
        if (!consumeStatementWords(builder, "ALTER", "TABLE")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        consumeOptionalWords(builder, "IF", "EXISTS")
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        alter_table_action(builder, level + 1)
        marker.done(SqlCompositeElementTypes.SQL_ALTER_TABLE_STATEMENT)
        return true
    }

    @JvmStatic
    fun alter_view_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "ALTER", "VIEW")) {
            return false
        }
        val marker = builder.mark()
        if (!consumeStatementWords(builder, "ALTER", "VIEW")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_VIEW_REFERENCE)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        alter_view_tail(builder, level + 1)
        marker.done(SqlCompositeElementTypes.SQL_ALTER_VIEW_STATEMENT)
        return true
    }

    @JvmStatic
    fun alter_materialized_view_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "ALTER", "MATERIALIZED", "VIEW")) {
            return false
        }
        val marker = builder.mark()
        if (!consumeStatementWords(builder, "ALTER", "MATERIALIZED", "VIEW")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_MATERIALIZED_VIEW_REFERENCE)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        materialized_view_action(builder, level + 1)
        marker.done(StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT)
        return true
    }

    @JvmStatic
    fun alter_catalog_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "ALTER", "CATALOG")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_CATALOG_REFERENCE)) {
            return marker.rollbackFalse()
        }
        consumeCatalogTail(builder, level + 1)
        marker.done(SqlCompositeElementTypes.SQL_ALTER_CATALOG_STATEMENT)
        return true
    }

    @JvmStatic
    fun alter_resource_statement(builder: PsiBuilder, level: Int): Boolean {
        return resource_statement(builder, level + 1, StarRocksElementTypes.RESOURCE_STATEMENT, "ALTER", "RESOURCE")
    }

    @JvmStatic
    fun alter_routine_load_statement(builder: PsiBuilder, level: Int): Boolean =
        routine_load_statement(builder, level + 1, "ALTER")

    @JvmStatic
    fun drop_user_statement(builder: PsiBuilder, level: Int): Boolean {
        return principal_statement(builder, level + 1, StarRocksElementTypes.DROP_USER_STATEMENT, "DROP", "USER")
    }

    @JvmStatic
    fun drop_role_statement(builder: PsiBuilder, level: Int): Boolean {
        return principal_statement(builder, level + 1, StarRocksElementTypes.DROP_ROLE_STATEMENT, "DROP", "ROLE")
    }

    @JvmStatic
    fun drop_database_statement(builder: PsiBuilder, level: Int): Boolean {
        return drop_schema_like_statement(builder, level + 1, "DATABASE")
    }

    @JvmStatic
    fun drop_schema_statement(builder: PsiBuilder, level: Int): Boolean {
        return drop_schema_like_statement(builder, level + 1, "SCHEMA")
    }

    @JvmStatic
    fun drop_index_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "DROP", "INDEX")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_INDEX_REFERENCE)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.consumeWord(builder, "ON")) {
            StarRocksParsingUtil.skipNoise(builder)
            parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)
        }
        marker.done(StarRocksElementTypes.INDEX_STATEMENT)
        return true
    }

    @JvmStatic
    fun drop_table_statement(builder: PsiBuilder, level: Int): Boolean {
        return drop_named_object_statement(
            builder,
            level + 1,
            StarRocksElementTypes.TABLE_DDL_STATEMENT,
            SqlCompositeElementTypes.SQL_TABLE_REFERENCE,
            "TABLE"
        )
    }

    @JvmStatic
    fun drop_view_statement(builder: PsiBuilder, level: Int): Boolean {
        return drop_named_object_statement(
            builder,
            level + 1,
            StarRocksElementTypes.VIEW_STATEMENT,
            SqlCompositeElementTypes.SQL_VIEW_REFERENCE,
            "VIEW"
        )
    }

    @JvmStatic
    fun drop_materialized_view_statement(builder: PsiBuilder, level: Int): Boolean {
        return drop_named_object_statement(
            builder,
            level + 1,
            StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT,
            SqlCompositeElementTypes.SQL_MATERIALIZED_VIEW_REFERENCE,
            "MATERIALIZED",
            "VIEW"
        )
    }

    @JvmStatic
    fun drop_catalog_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "DROP", "CATALOG")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        consumeOptionalWords(builder, "IF", "EXISTS")
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_CATALOG_REFERENCE)) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.CATALOG_STATEMENT)
        return true
    }

    @JvmStatic
    fun drop_resource_statement(builder: PsiBuilder, level: Int): Boolean {
        return resource_statement(builder, level + 1, StarRocksElementTypes.RESOURCE_STATEMENT, "DROP", "RESOURCE")
    }

    @JvmStatic
    fun drop_repository_statement(builder: PsiBuilder, level: Int): Boolean {
        return repository_statement(builder, level + 1, create = false)
    }

    @JvmStatic
    fun truncate_table_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "TRUNCATE", "TABLE")) {
            return false
        }
        val marker = builder.mark()
        if (!consumeStatementWords(builder, "TRUNCATE", "TABLE")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        truncate_tail(builder)
        marker.done(SqlCompositeElementTypes.SQL_TRUNCATE_TABLE_STATEMENT)
        return true
    }

    @JvmStatic
    fun refresh_materialized_view_statement(builder: PsiBuilder, level: Int): Boolean {
        val cancel = hasWords(builder, "CANCEL", "REFRESH", "MATERIALIZED", "VIEW")
        if (!cancel && !hasWords(builder, "REFRESH", "MATERIALIZED", "VIEW")) {
            return false
        }
        val marker = builder.mark()
        val words = if (cancel) {
            arrayOf("CANCEL", "REFRESH", "MATERIALIZED", "VIEW")
        } else {
            arrayOf("REFRESH", "MATERIALIZED", "VIEW")
        }
        if (!consumeStatementWords(builder, *words)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_MATERIALIZED_VIEW_REFERENCE)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        materialized_view_refresh_tail(builder)
        marker.done(StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT)
        return true
    }

    @JvmStatic
    fun grant_statement(builder: PsiBuilder, level: Int): Boolean {
        return privilege_statement(builder, level + 1, StarRocksElementTypes.GRANT_STATEMENT, "GRANT", "TO")
    }

    @JvmStatic
    fun revoke_statement(builder: PsiBuilder, level: Int): Boolean {
        return privilege_statement(builder, level + 1, StarRocksElementTypes.REVOKE_STATEMENT, "REVOKE", "FROM")
    }

    private fun resource_statement(
        builder: PsiBuilder,
        level: Int,
        elementType: IElementType,
        vararg words: String
    ): Boolean {
        if (!hasWordsAllowingExternalResource(builder, *words)) {
            return false
        }
        val marker = builder.mark()
        if (!consumeStatementWords(builder, *words)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        consumeOptionalWords(builder, "IF", "EXISTS")
        consumeOptionalWords(builder, "IF", "NOT", "EXISTS")
        StarRocksParsingUtil.skipNoise(builder)
        if (!resource_reference(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        consumeResourceTail(builder, level + 1)
        marker.done(elementType)
        return true
    }

    private fun principal_statement(
        builder: PsiBuilder,
        level: Int,
        elementType: IElementType,
        vararg words: String
    ): Boolean {
        if (!hasWords(builder, *words)) {
            return false
        }
        val marker = builder.mark()
        if (!consumeStatementWords(builder, *words)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        consumeOptionalWords(builder, "IF", "EXISTS")
        consumeOptionalWords(builder, "IF", "NOT", "EXISTS")
        StarRocksParsingUtil.skipNoise(builder)
        if (!security_principal(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        consumePrincipalTail(builder, level + 1)
        marker.done(elementType)
        return true
    }

    private fun privilege_statement(
        builder: PsiBuilder,
        level: Int,
        elementType: IElementType,
        verb: String,
        principalSeparator: String
    ): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, verb)) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!privilege_list(builder, level + 1, setOf("ON", principalSeparator))) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        privilege_target(builder, level + 1, principalSeparator)
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, principalSeparator)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!security_principal_list(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        consumePrivilegeTail(builder)
        marker.done(elementType)
        return true
    }

    private fun resource_reference(builder: PsiBuilder, level: Int): Boolean {
        return namedReference(builder, StarRocksElementTypes.RESOURCE_REFERENCE)
    }

    private fun security_principal(builder: PsiBuilder, level: Int): Boolean {
        StarRocksParsingUtil.skipNoise(builder)
        val marker = builder.mark()
        val before = builder.currentOffset
        if (StarRocksParsingUtil.word(builder) in PRINCIPAL_TYPE_WORDS) {
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
        }
        if (!consumeNameLike(builder, allowQualified = false, allowUserHost = true)) {
            return marker.rollbackFalse()
        }
        return if (builder.currentOffset > before) {
            marker.done(StarRocksElementTypes.SECURITY_PRINCIPAL)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun security_principal_list(builder: PsiBuilder, level: Int): Boolean {
        var parsedAny = false
        while (!builder.eof() && builder.tokenText != ";") {
            StarRocksParsingUtil.skipNoise(builder)
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                StarRocksParsingUtil.word(builder) in PRIVILEGE_TAIL_BOUNDARIES -> return parsedAny
                security_principal(builder, level + 1) -> parsedAny = true
                else -> return parsedAny
            }
        }
        return parsedAny
    }

    private fun privilege_list(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        val marker = builder.mark()
        val parsed = consumeUntilTopLevelWord(builder, stopWords)
        return if (parsed) {
            marker.done(StarRocksElementTypes.PRIVILEGE_LIST)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun privilege_target(builder: PsiBuilder, level: Int, principalSeparator: String): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "ON")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        val parsed = consumeUntilTopLevelWord(builder, setOf(principalSeparator))
        return if (parsed) {
            marker.done(StarRocksElementTypes.PRIVILEGE_TARGET)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun routine_load_statement(builder: PsiBuilder, level: Int, verb: String): Boolean {
        if (!hasWords(builder, verb, "ROUTINE", "LOAD")) {
            return false
        }
        val marker = builder.mark()
        if (!consumeStatementWords(builder, verb, "ROUTINE", "LOAD")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!namedReference(builder, StarRocksElementTypes.RESOURCE_REFERENCE)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.consumeWord(builder, "ON")) {
            StarRocksParsingUtil.skipNoise(builder)
            parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)
        }
        consumeKnownTail(builder) {
            routine_load_columns_clause(builder) ||
                properties_clause(builder, level + 1) ||
                routine_load_from_clause(builder)
        }
        marker.done(StarRocksElementTypes.ROUTINE_LOAD_STATEMENT)
        return true
    }

    private fun repository_statement(builder: PsiBuilder, level: Int, create: Boolean): Boolean {
        val words = if (create) arrayOf("CREATE", "REPOSITORY") else arrayOf("DROP", "REPOSITORY")
        if (!hasWords(builder, *words)) {
            return false
        }
        val marker = builder.mark()
        if (!consumeStatementWords(builder, *words)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!namedReference(builder, StarRocksElementTypes.RESOURCE_REFERENCE)) {
            return marker.rollbackFalse()
        }
        if (create) {
            consumeKnownTail(builder) {
                with_broker_clause(builder) ||
                    on_location_clause(builder) ||
                    properties_clause(builder, level + 1)
            }
        }
        marker.done(StarRocksElementTypes.BACKUP_RESTORE_STATEMENT)
        return true
    }

    private fun drop_named_object_statement(
        builder: PsiBuilder,
        level: Int,
        elementType: IElementType,
        referenceType: SqlReferenceElementType,
        vararg words: String
    ): Boolean {
        val allWords = arrayOf("DROP", *words)
        if (!hasWords(builder, *allWords)) {
            return false
        }
        val marker = builder.mark()
        if (!consumeStatementWords(builder, *allWords)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        consumeOptionalWords(builder, "IF", "EXISTS")
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, referenceType)) {
            return marker.rollbackFalse()
        }
        marker.done(elementType)
        return true
    }

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

    private fun create_schema_like_statement(builder: PsiBuilder, level: Int, schemaKeyword: String): Boolean {
        if (!hasWords(builder, "CREATE", schemaKeyword)) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        consumeOptionalWords(builder, "IF", "NOT", "EXISTS")
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_SCHEMA_REFERENCE)) {
            return marker.rollbackFalse()
        }
        consumeSchemaTail(builder, level + 1)
        marker.done(SqlCompositeElementTypes.SQL_CREATE_SCHEMA_STATEMENT)
        return true
    }

    private fun alter_schema_like_statement(builder: PsiBuilder, level: Int, schemaKeyword: String): Boolean {
        if (!hasWords(builder, "ALTER", schemaKeyword)) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_SCHEMA_REFERENCE)) {
            return marker.rollbackFalse()
        }
        consumeAlterSchemaTail(builder)
        marker.done(SqlCompositeElementTypes.SQL_ALTER_SCHEMA_STATEMENT)
        return true
    }

    private fun drop_schema_like_statement(builder: PsiBuilder, level: Int, schemaKeyword: String): Boolean {
        if (!hasWords(builder, "DROP", schemaKeyword)) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        consumeOptionalWords(builder, "IF", "EXISTS")
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_SCHEMA_REFERENCE)) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.SCHEMA_STATEMENT)
        return true
    }

    private fun alter_table_action(builder: PsiBuilder, level: Int): Boolean {
        return when (StarRocksParsingUtil.word(builder)) {
            "ADD" -> add_table_action(builder, level + 1)
            "DROP" -> drop_table_action(builder, level + 1)
            "MODIFY", "CHANGE", "ALTER" -> modify_table_action(builder, level + 1)
            "RENAME" -> rename_table_action(builder, level + 1)
            "SET" -> set_tail_clause(builder, level + 1)
            else -> false
        }
    }

    private fun add_table_action(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "ADD")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "COLUMN") || StarRocksParsingUtil.consumeWord(builder, "COLUMNS")
        StarRocksParsingUtil.skipNoise(builder)
        val parsed = table_column_list(builder, level + 1) ||
            column_definition(builder, level + 1) ||
            parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_COLUMN_REFERENCE)
        return if (parsed) {
            marker.drop()
            true
        } else {
            marker.rollbackFalse()
        }
    }

    private fun drop_table_action(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "DROP")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "COLUMN") ||
            StarRocksParsingUtil.consumeWord(builder, "PARTITION") ||
            StarRocksParsingUtil.consumeWord(builder, "ROLLUP")
        StarRocksParsingUtil.skipNoise(builder)
        val parsed = parenthesized_payload_clause(builder) ||
            parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_COLUMN_REFERENCE)
        return if (parsed) {
            marker.drop()
            true
        } else {
            marker.rollbackFalse()
        }
    }

    private fun modify_table_action(builder: PsiBuilder, level: Int): Boolean {
        if (StarRocksParsingUtil.word(builder) !in TABLE_MODIFY_ACTIONS) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "COLUMN")
        StarRocksParsingUtil.skipNoise(builder)
        val parsed = column_definition(builder, level + 1) ||
            parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_COLUMN_REFERENCE)
        return if (parsed) {
            marker.drop()
            true
        } else {
            marker.rollbackFalse()
        }
    }

    private fun rename_table_action(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "RENAME")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "TO")
        StarRocksParsingUtil.skipNoise(builder)
        return if (parseDefinitionReference(builder, level + 1, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)) {
            marker.drop()
            true
        } else {
            marker.rollbackFalse()
        }
    }

    private fun alter_view_tail(builder: PsiBuilder, level: Int): Boolean {
        if (StarRocksParsingUtil.tokenIs(builder, "AS")) {
            val asMarker = builder.mark()
            builder.advanceLexer()
            StarRocksDmlParsing.query_expression(builder, level + 1, -1)
            asMarker.done(StarRocksElementTypes.AS_SELECT_CLAUSE)
            return true
        }
        consumeKnownTail(builder) {
            comment_clause(builder, level + 1) ||
                properties_clause(builder, level + 1)
        }
        return true
    }

    private fun materialized_view_action(builder: PsiBuilder, level: Int): Boolean {
        if (StarRocksParsingUtil.word(builder) in MATERIALIZED_VIEW_STATUS_ACTIONS) {
            builder.advanceLexer()
            return true
        }
        if (StarRocksParsingUtil.tokenIs(builder, "AS")) {
            val asMarker = builder.mark()
            builder.advanceLexer()
            StarRocksDmlParsing.query_expression(builder, level + 1, -1)
            asMarker.done(StarRocksElementTypes.AS_SELECT_CLAUSE)
            return true
        }
        consumeKnownTail(builder) {
            refresh_clause(builder, level + 1) ||
                properties_clause(builder, level + 1) ||
                comment_clause(builder, level + 1)
        }
        return true
    }

    private fun truncate_tail(builder: PsiBuilder): Boolean {
        consumeKnownTail(builder) {
            partition_name_clause(builder)
        }
        return true
    }

    private fun materialized_view_refresh_tail(builder: PsiBuilder): Boolean {
        consumeKnownTail(builder) {
            partition_name_clause(builder) ||
                force_clause(builder)
        }
        return true
    }

    private fun parseIndexColumnList(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        while (!builder.eof() && builder.tokenText != ")") {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                SqlGeneratedParserUtil.parseReference(
                    builder,
                    level + 1,
                    SqlCompositeElementTypes.SQL_COLUMN_REFERENCE
                ) -> Unit
                StarRocksExpressionParsing.value_expression(builder, level + 1, setOf(",", ")")) -> Unit
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.TABLE_COLUMN_LIST)
        return true
    }

    private fun routine_load_columns_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "COLUMNS")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.consumeWord(builder, "TERMINATED")) {
            StarRocksParsingUtil.skipNoise(builder)
            if (!StarRocksParsingUtil.consumeWord(builder, "BY")) {
                return marker.rollbackFalse()
            }
            StarRocksParsingUtil.skipNoise(builder)
            if (!consumeSingleTailAtom(builder)) {
                return marker.rollbackFalse()
            }
        }
        marker.drop()
        return true
    }

    private fun routine_load_from_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "FROM")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "KAFKA")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        parenthesized_properties_clause(builder)
        marker.drop()
        return true
    }

    private fun with_broker_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "WITH")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "BROKER")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (builder.tokenText != ";" && builder.tokenText != ")" && builder.tokenText != "(" &&
            StarRocksParsingUtil.word(builder) !in BROKER_CLAUSE_BOUNDARIES
        ) {
            consumeSingleTailAtom(builder)
        }
        marker.drop()
        return true
    }

    private fun on_location_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "ON")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "LOCATION")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeSingleTailAtom(builder)) {
            return marker.rollbackFalse()
        }
        marker.drop()
        return true
    }

    private fun partition_name_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "PARTITION")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val parsed = parenthesized_payload_clause(builder) || consumeSingleTailAtom(builder)
        return if (parsed) {
            marker.drop()
            true
        } else {
            marker.rollbackFalse()
        }
    }

    private fun force_clause(builder: PsiBuilder): Boolean {
        if (StarRocksParsingUtil.word(builder) !in FORCE_REFRESH_WORDS) {
            return false
        }
        builder.advanceLexer()
        return true
    }

    private fun parenthesized_payload_clause(builder: PsiBuilder): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        builder.advanceLexer()
        StarRocksParsingUtil.consumeBalancedTail(builder)
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        return true
    }

    private fun consumeCatalogTail(builder: PsiBuilder, level: Int) {
        consumeKnownTail(builder) {
            properties_clause(builder, level + 1) ||
                comment_clause(builder, level + 1) ||
                set_tail_clause(builder, level + 1)
        }
    }

    private fun consumeResourceTail(builder: PsiBuilder, level: Int) {
        consumeKnownTail(builder) {
            properties_clause(builder, level + 1) ||
                set_tail_clause(builder, level + 1)
        }
    }

    private fun consumePrincipalTail(builder: PsiBuilder, level: Int) {
        consumeKnownTail(builder) {
            identified_by_clause(builder) ||
                default_role_clause(builder) ||
                properties_clause(builder, level + 1) ||
                set_tail_clause(builder, level + 1)
        }
    }

    private fun consumePrivilegeTail(builder: PsiBuilder) {
        consumeKnownTail(builder) {
            with_grant_option_clause(builder)
        }
    }

    private fun consumeSchemaTail(builder: PsiBuilder, level: Int) {
        consumeKnownTail(builder) {
            properties_clause(builder, level + 1) ||
                comment_clause(builder, level + 1)
        }
    }

    private fun consumeAlterSchemaTail(builder: PsiBuilder) {
        consumeKnownTail(builder) {
            set_tail_clause(builder, 0)
        }
    }

    private fun consumeKnownTail(builder: PsiBuilder, parseClause: () -> Boolean) {
        while (!builder.eof() && builder.tokenText != ";") {
            StarRocksParsingUtil.skipNoise(builder)
            val before = builder.currentOffset
            if (!parseClause() || builder.currentOffset == before) {
                return
            }
        }
    }

    private fun set_tail_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "SET")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val parsed = properties_clause(builder, level + 1) ||
            parenthesized_properties_clause(builder) ||
            comment_clause(builder, level + 1) ||
            quota_tail_clause(builder)
        return if (parsed) {
            marker.drop()
            true
        } else {
            marker.rollbackFalse()
        }
    }

    private fun parenthesized_properties_clause(builder: PsiBuilder): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        parsePropertyPairs(builder)
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.PROPERTIES_CLAUSE)
        return true
    }

    private fun quota_tail_clause(builder: PsiBuilder): Boolean {
        if (StarRocksParsingUtil.word(builder) !in QUOTA_KINDS) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "QUOTA")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeSingleTailAtom(builder)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.word(builder) in QUOTA_UNITS) {
            builder.advanceLexer()
        }
        marker.drop()
        return true
    }

    private fun identified_by_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "IDENTIFIED")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "BY")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeSingleTailAtom(builder)) {
            return marker.rollbackFalse()
        }
        marker.drop()
        return true
    }

    private fun default_role_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "DEFAULT")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "ROLE")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!security_principal_list(builder, 0)) {
            return marker.rollbackFalse()
        }
        marker.drop()
        return true
    }

    private fun with_grant_option_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "WITH")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "GRANT")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "OPTION")) {
            return marker.rollbackFalse()
        }
        marker.drop()
        return true
    }

    private fun consumeSingleTailAtom(builder: PsiBuilder): Boolean {
        val text = builder.tokenText
        if (builder.eof() || text == null || text == ";" || text == "," || text == ")" || text == "(") {
            return false
        }
        builder.advanceLexer()
        return true
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

    private fun consumeStatementWords(builder: PsiBuilder, vararg words: String): Boolean {
        words.forEachIndexed { index, word ->
            StarRocksParsingUtil.skipNoise(builder)
            if (index == 1 && words.contentEquals(arrayOf("CREATE", "RESOURCE"))) {
                StarRocksParsingUtil.consumeWord(builder, "EXTERNAL")
                StarRocksParsingUtil.skipNoise(builder)
            }
            if (!StarRocksParsingUtil.consumeWord(builder, word)) {
                return false
            }
        }
        return true
    }

    private fun consumeTableConstraint(builder: PsiBuilder) {
        StarRocksParsingUtil.consumeBalancedTail(builder, setOf(","))
    }

    private fun hasWords(builder: PsiBuilder, vararg words: String): Boolean {
        return words.indices.all { index -> wordAt(builder, index) == words[index] }
    }

    private fun hasWordsAllowingExternalResource(builder: PsiBuilder, vararg words: String): Boolean {
        if (hasWords(builder, *words)) {
            return true
        }
        return words.contentEquals(arrayOf("CREATE", "RESOURCE")) &&
            wordAt(builder, 0) == "CREATE" &&
            wordAt(builder, 1) == "EXTERNAL" &&
            wordAt(builder, 2) == "RESOURCE"
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

    private fun namedReference(builder: PsiBuilder, elementType: IElementType): Boolean {
        val marker = builder.mark()
        val before = builder.currentOffset
        return if (consumeNameLike(builder, allowQualified = true, allowUserHost = false) &&
            builder.currentOffset > before
        ) {
            marker.done(elementType)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun consumeNameLike(
        builder: PsiBuilder,
        allowQualified: Boolean,
        allowUserHost: Boolean
    ): Boolean {
        var consumed = false
        var expectPart = true
        while (!builder.eof()) {
            val text = builder.tokenText
            when {
                expectPart && isNameLikeToken(builder) -> {
                    builder.advanceLexer()
                    consumed = true
                    expectPart = false
                }
                consumed && allowQualified && text == "." -> {
                    builder.advanceLexer()
                    expectPart = true
                }
                consumed && allowUserHost && text == "@" -> {
                    builder.advanceLexer()
                    expectPart = true
                }
                else -> return consumed
            }
        }
        return consumed
    }

    private fun isNameLikeToken(builder: PsiBuilder): Boolean {
        val text = builder.tokenText ?: return false
        if (text in NAME_TOKEN_EXCLUSIONS) {
            return false
        }
        return StarRocksParsingUtil.isIdentifier(builder) ||
            text.startsWith("'") ||
            text.startsWith("\"") ||
            text.startsWith("@")
    }

    private fun consumeUntilTopLevelWord(builder: PsiBuilder, stopWords: Set<String>): Boolean {
        val before = builder.currentOffset
        var parenDepth = 0
        while (!builder.eof()) {
            val text = builder.tokenText
            val word = StarRocksParsingUtil.word(builder)
            if (parenDepth == 0 && (text == ";" || word in stopWords)) {
                return builder.currentOffset > before
            }
            when (text) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth-- else return builder.currentOffset > before
            }
            builder.advanceLexer()
        }
        return builder.currentOffset > before
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
            if (parenDepth == 0 && angleDepth == 0 &&
                (text == ";" || text == "," || text == ")" || upper in COLUMN_ATTRIBUTE_STARTERS)
            ) {
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

    private val PRINCIPAL_TYPE_WORDS = setOf("USER", "ROLE")

    private val PRIVILEGE_TAIL_BOUNDARIES = setOf("WITH", "AS", "DEFAULT", "MAX_USER_CONNECTIONS")

    private val QUOTA_KINDS = setOf("DATA", "REPLICA")

    private val QUOTA_UNITS = setOf("B", "K", "KB", "M", "MB", "G", "GB", "T", "TB", "P", "PB")

    private val TABLE_MODIFY_ACTIONS = setOf("MODIFY", "CHANGE", "ALTER")

    private val MATERIALIZED_VIEW_STATUS_ACTIONS = setOf("ACTIVE", "INACTIVE", "SUSPEND", "RESUME")

    private val BROKER_CLAUSE_BOUNDARIES = setOf("ON", "PROPERTIES", "FROM", "WITH")

    private val FORCE_REFRESH_WORDS = setOf("FORCE", "WITH")

    private val NAME_TOKEN_EXCLUSIONS = setOf(",", ";", "(", ")", "=", ":", "+", "-", "*", "/")

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

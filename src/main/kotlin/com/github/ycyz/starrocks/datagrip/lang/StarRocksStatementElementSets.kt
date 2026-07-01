package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlCompositeElementTypes

object StarRocksStatementElementSets {
    val PLATFORM_STATEMENT_TYPES: Set<IElementType> = setOf(
        SqlCompositeElementTypes.SQL_SELECT_STATEMENT,
        SqlCompositeElementTypes.SQL_INSERT_STATEMENT,
        SqlCompositeElementTypes.SQL_UPDATE_STATEMENT,
        SqlCompositeElementTypes.SQL_DELETE_STATEMENT,
        SqlCompositeElementTypes.SQL_MERGE_STATEMENT,
        SqlCompositeElementTypes.SQL_SET_STATEMENT,
        SqlCompositeElementTypes.SQL_USE_SCHEMA_STATEMENT,
        SqlCompositeElementTypes.SQL_USE_CATALOG_STATEMENT,
        SqlCompositeElementTypes.SQL_USE_NAMESPACE_STATEMENT,
        SqlCompositeElementTypes.SQL_EXPLAIN_STATEMENT,
        SqlCompositeElementTypes.SQL_TRUNCATE_TABLE_STATEMENT,
        SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT,
        SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT,
        SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT
    )

    val STARROCKS_STATEMENT_TYPES: Set<IElementType> = setOf(
        StarRocksElementTypes.TABLE_DDL_STATEMENT,
        StarRocksElementTypes.VIEW_STATEMENT,
        StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT,
        StarRocksElementTypes.CATALOG_STATEMENT,
        StarRocksElementTypes.RESOURCE_STATEMENT,
        StarRocksElementTypes.LOAD_STATEMENT,
        StarRocksElementTypes.ROUTINE_LOAD_STATEMENT,
        StarRocksElementTypes.TASK_STATEMENT,
        StarRocksElementTypes.EXPORT_STATEMENT,
        StarRocksElementTypes.BACKUP_RESTORE_STATEMENT,
        StarRocksElementTypes.ADMIN_STATEMENT
    )

    val STATEMENT_TYPES: Set<IElementType> = PLATFORM_STATEMENT_TYPES + STARROCKS_STATEMENT_TYPES

    val QUERY_SCOPE_TYPES: Set<IElementType> = setOf(
        SqlCompositeElementTypes.SQL_SELECT_STATEMENT,
        SqlCompositeElementTypes.SQL_INSERT_STATEMENT,
        SqlCompositeElementTypes.SQL_UPDATE_STATEMENT,
        SqlCompositeElementTypes.SQL_DELETE_STATEMENT,
        SqlCompositeElementTypes.SQL_MERGE_STATEMENT,
        StarRocksElementTypes.AS_SELECT_QUERY,
        StarRocksElementTypes.CTE_QUERY,
        StarRocksElementTypes.SUBQUERY_EXPRESSION
    )
}

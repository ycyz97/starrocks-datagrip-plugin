package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlCompositeElementTypes

object StarRocksAuxiliaryParsing {
    @JvmStatic
    fun create_catalog_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.CATALOG_STATEMENT, "CREATE", "CATALOG") ||
            prefixedStatement(builder, StarRocksElementTypes.CATALOG_STATEMENT, "CREATE", "EXTERNAL", "CATALOG")

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
    fun alter_starrocks_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.TABLE_DDL_STATEMENT, "ALTER", "TABLE") ||
            prefixedStatement(builder, StarRocksElementTypes.VIEW_STATEMENT, "ALTER", "VIEW") ||
            prefixedStatement(builder, StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT, "ALTER", "MATERIALIZED", "VIEW") ||
            prefixedStatement(builder, StarRocksElementTypes.CATALOG_STATEMENT, "ALTER", "CATALOG") ||
            prefixedStatement(builder, StarRocksElementTypes.RESOURCE_STATEMENT, "ALTER", "RESOURCE") ||
            prefixedStatement(builder, StarRocksElementTypes.ROUTINE_LOAD_STATEMENT, "ALTER", "ROUTINE", "LOAD")

    @JvmStatic
    fun drop_starrocks_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.TABLE_DDL_STATEMENT, "DROP", "TABLE") ||
            prefixedStatement(builder, StarRocksElementTypes.VIEW_STATEMENT, "DROP", "VIEW") ||
            prefixedStatement(builder, StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT, "DROP", "MATERIALIZED", "VIEW") ||
            prefixedStatement(builder, StarRocksElementTypes.CATALOG_STATEMENT, "DROP", "CATALOG") ||
            prefixedStatement(builder, StarRocksElementTypes.RESOURCE_STATEMENT, "DROP", "RESOURCE") ||
            prefixedStatement(builder, StarRocksElementTypes.BACKUP_RESTORE_STATEMENT, "DROP", "REPOSITORY")

    @JvmStatic
    fun show_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "SHOW")) {
            return false
        }
        val elementType = when (wordAt(builder, 1)) {
            "CATALOGS" -> StarRocksElementTypes.CATALOG_STATEMENT
            "RESOURCES" -> StarRocksElementTypes.RESOURCE_STATEMENT
            "BACKUP", "RESTORE" -> StarRocksElementTypes.BACKUP_RESTORE_STATEMENT
            "MATERIALIZED" -> StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT
            "CREATE" -> when (wordAt(builder, 2)) {
                "CATALOG" -> StarRocksElementTypes.CATALOG_STATEMENT
                "MATERIALIZED" -> StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT
                "TABLE" -> StarRocksElementTypes.TABLE_DDL_STATEMENT
                "VIEW" -> StarRocksElementTypes.VIEW_STATEMENT
                else -> StarRocksElementTypes.ADMIN_STATEMENT
            }
            else -> StarRocksElementTypes.ADMIN_STATEMENT
        }
        return prefixedStatement(builder, elementType, "SHOW")
    }

    @JvmStatic
    fun admin_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.ADMIN_STATEMENT, "ADMIN")

    @JvmStatic
    fun analyze_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.ADMIN_STATEMENT, "ANALYZE")

    @JvmStatic
    fun set_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_SET_STATEMENT, "SET")

    @JvmStatic
    fun unset_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.ADMIN_STATEMENT, "UNSET")

    @JvmStatic
    fun kill_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.ADMIN_STATEMENT, "KILL")

    @JvmStatic
    fun sync_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.ADMIN_STATEMENT, "SYNC")

    @JvmStatic
    fun use_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_USE_SCHEMA_STATEMENT, "USE")

    @JvmStatic
    fun explain_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_EXPLAIN_STATEMENT, "EXPLAIN")

    @JvmStatic
    fun describe_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.ADMIN_STATEMENT, "DESC") ||
            prefixedStatement(builder, StarRocksElementTypes.ADMIN_STATEMENT, "DESCRIBE")

    @JvmStatic
    fun load_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.LOAD_STATEMENT, "LOAD")

    @JvmStatic
    fun cancel_load_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.LOAD_STATEMENT, "CANCEL", "LOAD")

    @JvmStatic
    fun task_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.TASK_STATEMENT, "SUBMIT", "TASK")

    @JvmStatic
    fun export_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.EXPORT_STATEMENT, "EXPORT") ||
            prefixedStatement(builder, StarRocksElementTypes.EXPORT_STATEMENT, "CANCEL", "EXPORT")

    @JvmStatic
    fun backup_restore_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.BACKUP_RESTORE_STATEMENT, "BACKUP") ||
            prefixedStatement(builder, StarRocksElementTypes.BACKUP_RESTORE_STATEMENT, "RESTORE") ||
            prefixedStatement(builder, StarRocksElementTypes.BACKUP_RESTORE_STATEMENT, "RECOVER")

    @JvmStatic
    fun refresh_materialized_view_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT, "REFRESH", "MATERIALIZED", "VIEW") ||
            prefixedStatement(builder, StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT, "CANCEL", "REFRESH", "MATERIALIZED", "VIEW")

    @JvmStatic
    fun truncate_table_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_TRUNCATE_TABLE_STATEMENT, "TRUNCATE", "TABLE")

    @JvmStatic
    fun dml_statement(builder: PsiBuilder, level: Int): Boolean {
        return when (StarRocksParsingUtil.word(builder)) {
            "UPDATE" -> prefixedStatement(builder, SqlCompositeElementTypes.SQL_UPDATE_STATEMENT, "UPDATE")
            "DELETE" -> prefixedStatement(builder, SqlCompositeElementTypes.SQL_DELETE_STATEMENT, "DELETE")
            "MERGE" -> prefixedStatement(builder, SqlCompositeElementTypes.SQL_MERGE_STATEMENT, "MERGE")
            else -> false
        }
    }

    private fun prefixedStatement(builder: PsiBuilder, elementType: IElementType, vararg words: String): Boolean {
        if (!hasWords(builder, *words)) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        while (!builder.eof() && builder.tokenText != ";") {
            when {
                StarRocksDdlParsing.properties_clause(builder, 0) -> continue
                StarRocksDdlParsing.type_element(builder, 0) -> continue
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
}

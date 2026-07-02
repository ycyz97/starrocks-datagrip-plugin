package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlCompositeElementTypes

object StarRocksOtherParsing {
    @JvmStatic
    fun other_statement(builder: PsiBuilder, level: Int): Boolean =
        show_statement(builder, level + 1) ||
            admin_statement(builder, level + 1) ||
            analyze_statement(builder, level + 1) ||
            set_password_statement(builder, level + 1) ||
            set_statement(builder, level + 1) ||
            unset_statement(builder, level + 1) ||
            kill_statement(builder, level + 1) ||
            sync_statement(builder, level + 1) ||
            call_statement(builder, level + 1) ||
            start_transaction_statement(builder, level + 1) ||
            commit_statement(builder, level + 1) ||
            rollback_statement(builder, level + 1) ||
            use_statement(builder, level + 1) ||
            explain_statement(builder, level + 1) ||
            describe_statement(builder, level + 1) ||
            load_statement(builder, level + 1) ||
            cancel_load_statement(builder, level + 1) ||
            task_statement(builder, level + 1) ||
            export_statement(builder, level + 1) ||
            backup_restore_statement(builder, level + 1)

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
    fun set_password_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, StarRocksElementTypes.SET_PASSWORD_STATEMENT, "SET", "PASSWORD")

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
    fun call_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_CALL_STATEMENT, "CALL")

    @JvmStatic
    fun start_transaction_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_START_TRANSACTION_STATEMENT, "BEGIN") ||
            prefixedStatement(builder, SqlCompositeElementTypes.SQL_START_TRANSACTION_STATEMENT, "START", "TRANSACTION")

    @JvmStatic
    fun commit_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_COMMIT_STATEMENT, "COMMIT")

    @JvmStatic
    fun rollback_statement(builder: PsiBuilder, level: Int): Boolean =
        prefixedStatement(builder, SqlCompositeElementTypes.SQL_ROLLBACK_STATEMENT, "ROLLBACK")

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

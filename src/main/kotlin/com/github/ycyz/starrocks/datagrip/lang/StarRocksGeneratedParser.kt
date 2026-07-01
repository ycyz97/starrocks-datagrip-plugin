package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.LightPsiParser
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class StarRocksGeneratedParser : PsiParser, LightPsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        parseLight(root, builder)
        return builder.treeBuilt
    }

    override fun parseLight(root: IElementType, builder: PsiBuilder) {
        val marker = builder.mark()
        parse_root_(root, builder)
        marker.done(root)
    }

    protected fun parse_root_(root: IElementType, builder: PsiBuilder): Boolean {
        return parse_root_(root, builder, 0)
    }

    companion object {
        @JvmField
        val EXTENDS_SETS_: Array<TokenSet> = emptyArray()

        @JvmStatic
        fun parse_root_(root: IElementType, builder: PsiBuilder, level: Int): Boolean {
            var parsedAny = false
            while (!builder.eof()) {
                if (builder.tokenText == ";") {
                    builder.advanceLexer()
                    continue
                }
                val before = builder.currentOffset
                val parsed = statement(builder, level + 1)
                parsedAny = parsedAny || parsed
                if (builder.currentOffset == before && !builder.eof()) {
                    builder.advanceLexer()
                }
            }
            return parsedAny
        }

        @JvmStatic
        fun statement(builder: PsiBuilder, level: Int): Boolean {
            val word = StarRocksParsingUtil.word(builder)
            return when (word) {
                "CREATE" -> StarRocksDdlParsing.create_materialized_view_statement(builder, level + 1) ||
                    StarRocksDdlParsing.create_view_statement(builder, level + 1) ||
                    StarRocksDdlParsing.create_table_statement(builder, level + 1) ||
                    StarRocksAuxiliaryParsing.create_catalog_statement(builder, level + 1) ||
                    StarRocksAuxiliaryParsing.create_resource_statement(builder, level + 1) ||
                    StarRocksAuxiliaryParsing.create_routine_load_statement(builder, level + 1) ||
                    StarRocksAuxiliaryParsing.create_repository_statement(builder, level + 1)
                "INSERT" -> StarRocksDmlParsing.insert_statement(builder, level + 1)
                "SELECT", "WITH", "VALUES" -> StarRocksDmlParsing.top_query_expression(builder, level + 1)
                "QUALIFY" -> StarRocksDmlParsing.qualify_clause(builder, level + 1)
                "UPDATE", "DELETE", "MERGE" -> StarRocksAuxiliaryParsing.dml_statement(builder, level + 1)
                "ALTER" -> StarRocksAuxiliaryParsing.alter_starrocks_statement(builder, level + 1)
                "DROP" -> StarRocksAuxiliaryParsing.drop_starrocks_statement(builder, level + 1)
                "SHOW" -> StarRocksAuxiliaryParsing.show_statement(builder, level + 1)
                "ADMIN" -> StarRocksAuxiliaryParsing.admin_statement(builder, level + 1)
                "ANALYZE" -> StarRocksAuxiliaryParsing.analyze_statement(builder, level + 1)
                "SET" -> StarRocksAuxiliaryParsing.set_statement(builder, level + 1)
                "UNSET" -> StarRocksAuxiliaryParsing.unset_statement(builder, level + 1)
                "KILL" -> StarRocksAuxiliaryParsing.kill_statement(builder, level + 1)
                "SYNC" -> StarRocksAuxiliaryParsing.sync_statement(builder, level + 1)
                "USE" -> StarRocksAuxiliaryParsing.use_statement(builder, level + 1)
                "EXPLAIN" -> StarRocksAuxiliaryParsing.explain_statement(builder, level + 1)
                "DESC", "DESCRIBE" -> StarRocksAuxiliaryParsing.describe_statement(builder, level + 1)
                "LOAD" -> StarRocksAuxiliaryParsing.load_statement(builder, level + 1)
                "SUBMIT" -> StarRocksAuxiliaryParsing.task_statement(builder, level + 1)
                "EXPORT", "CANCEL" -> StarRocksAuxiliaryParsing.cancel_load_statement(builder, level + 1) ||
                    StarRocksAuxiliaryParsing.export_statement(builder, level + 1) ||
                    StarRocksAuxiliaryParsing.refresh_materialized_view_statement(builder, level + 1)
                "BACKUP", "RESTORE", "RECOVER" -> StarRocksAuxiliaryParsing.backup_restore_statement(builder, level + 1)
                "REFRESH" -> StarRocksAuxiliaryParsing.refresh_materialized_view_statement(builder, level + 1)
                "TRUNCATE" -> StarRocksAuxiliaryParsing.truncate_table_statement(builder, level + 1)
                else -> false
            }
        }

        @JvmStatic
        fun expression(builder: PsiBuilder, level: Int): Boolean {
            return StarRocksExpressionParsing.value_expression(builder, level + 1)
        }

        @JvmStatic
        fun table_column_list(builder: PsiBuilder, level: Int): Boolean {
            return StarRocksDdlParsing.table_column_list(builder, level + 1)
        }
    }
}

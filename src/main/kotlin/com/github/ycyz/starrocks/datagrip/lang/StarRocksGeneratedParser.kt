package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.LightPsiParser
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.parser.GeneratedParserUtilBase.Parser
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.sql.dialects.base.SqlGeneratedParserUtil

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
            return SqlGeneratedParserUtil.parseScript(builder, level + 1, STATEMENT_PARSER)
        }

        @JvmStatic
        fun statement(builder: PsiBuilder, level: Int): Boolean {
            return sql_statement(builder, level + 1)
        }

        @JvmStatic
        fun sql_statement(builder: PsiBuilder, level: Int): Boolean {
            return statement_inner(builder, level + 1)
        }

        @JvmStatic
        fun statement_inner(builder: PsiBuilder, level: Int): Boolean {
            return StarRocksDdlParsing.ddl_statement(builder, level + 1) ||
                StarRocksDmlParsing.dml_statement(builder, level + 1) ||
                StarRocksOtherParsing.other_statement(builder, level + 1)
        }

        @JvmStatic
        fun statement_recover_prefix(builder: PsiBuilder, level: Int): Boolean {
            return consumeStatementStarter(builder)
        }

        @JvmStatic
        fun expression(builder: PsiBuilder, level: Int): Boolean {
            return StarRocksExpressionParsing.value_expression(builder, level + 1)
        }

        @JvmStatic
        fun analytic_clause(builder: PsiBuilder, level: Int): Boolean {
            return StarRocksExpressionParsing.analytic_clause(builder, level + 1)
        }

        @JvmStatic
        fun table_column_list(builder: PsiBuilder, level: Int): Boolean {
            return StarRocksDdlParsing.table_column_list(builder, level + 1)
        }

        private fun consumeStatementStarter(builder: PsiBuilder): Boolean {
            val word = StarRocksParsingUtil.word(builder) ?: return false
            if (word !in STATEMENT_STARTERS) {
                return false
            }
            builder.advanceLexer()
            return true
        }

        private val STATEMENT_PARSER = Parser { builder, level -> statement(builder, level) }

        private val STATEMENT_STARTERS = setOf(
            "ADMIN",
            "ALTER",
            "ANALYZE",
            "BACKUP",
            "BEGIN",
            "CALL",
            "CANCEL",
            "COMMIT",
            "CREATE",
            "DELETE",
            "DESC",
            "DESCRIBE",
            "DROP",
            "EXPORT",
            "EXPLAIN",
            "GRANT",
            "INSERT",
            "KILL",
            "LOAD",
            "RECOVER",
            "REFRESH",
            "RESTORE",
            "REVOKE",
            "ROLLBACK",
            "SELECT",
            "SET",
            "SHOW",
            "START",
            "SUBMIT",
            "SYNC",
            "TRUNCATE",
            "UNSET",
            "UPDATE",
            "USE",
            "VALUES",
            "WITH",
            "MERGE"
        )
    }
}

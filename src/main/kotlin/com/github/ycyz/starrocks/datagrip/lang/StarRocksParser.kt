package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.sql.dialects.SqlLanguageDialectEx
import com.intellij.sql.dialects.base.SqlGeneratedParserUtil
import com.intellij.sql.dialects.base.SqlParser
import com.intellij.sql.dialects.base.SqlParserUtil
import com.intellij.sql.injection.SqlSuggestedInjection
import com.intellij.sql.psi.SqlTokens.SQL_IDENT_DELIMITED
import com.intellij.database.DatabaseBundle

class StarRocksParser : SqlParser(StarRocksDialect.INSTANCE) {
    override fun getLanguage(): SqlLanguageDialectEx = StarRocksDialect.INSTANCE

    override fun getCurrentSqlInjection(): SqlSuggestedInjection = STARROCKS_INJECTION

    override fun getExtendsTokenSets(): Array<TokenSet> = StarRocksGeneratedParser.EXTENDS_SETS_

    override fun parseExtraRoots(root: IElementType, builder: PsiBuilder, level: Int): Boolean {
        return StarRocksGeneratedParser.parse_root_(root, builder, level)
    }

    override fun parseSqlStatement(builder: PsiBuilder, level: Int): Boolean {
        return StarRocksGeneratedParser.statement(builder, level)
    }

    override fun parseQueryExpression(builder: PsiBuilder, level: Int): Boolean {
        return StarRocksDmlParsing.top_query_expression(builder, level)
    }

    override fun parseDataTypeExt(builder: PsiBuilder): Boolean {
        return parseTableDataType(builder) || parseDataType(builder, 0, true)
    }

    override fun parseDataType(builder: PsiBuilder, level: Int, ext: Boolean): Boolean {
        return if (ext) {
            StarRocksDdlParsing.type_element_ext(builder, level)
        } else {
            StarRocksDdlParsing.type_element(builder, level)
        }
    }

    override fun parseCastDataType(builder: PsiBuilder, level: Int): Boolean {
        return StarRocksExpressionParsing.cast_type(builder, level)
    }

    override fun parseValueExpression(builder: PsiBuilder, level: Int, immediate: Boolean, strict: Boolean): Boolean {
        val parsed = StarRocksExpressionParsing.value_expression(builder, level)
        if (!parsed && !immediate) {
            builder.error(DatabaseBundle.message("parsing.error.expression.expected"))
        }
        return parsed
    }

    override fun parseEvaluableExpression(builder: PsiBuilder, level: Int): Boolean {
        return SqlGeneratedParserUtil.parseAndRemapToGenericReference(builder, level, StarRocksExpressionParsing::evaluable_expression)
    }

    override fun parseFunctionCallTail(builder: PsiBuilder, level: Int): Boolean {
        val parsed = super.parseFunctionCallTail(builder, level)
        if (parsed) {
            StarRocksGeneratedParser.analytic_clause(builder, level)
        }
        return parsed
    }

    override fun parseForeignKeyRefList(builder: PsiBuilder, level: Int): Boolean {
        return StarRocksGeneratedParser.table_column_list(builder, level)
    }

    override fun allowNoopStringConcatenation(builder: PsiBuilder, strict: Boolean): Boolean {
        return (!strict && SqlParserUtil.nextTokenIs(builder, SQL_IDENT_DELIMITED)) ||
            super.allowNoopStringConcatenation(builder, strict)
    }

    private companion object {
        val STARROCKS_INJECTION = SqlSuggestedInjection("StarRocks")
    }
}

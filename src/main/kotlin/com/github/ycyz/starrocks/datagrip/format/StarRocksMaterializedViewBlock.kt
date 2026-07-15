package com.github.ycyz.starrocks.datagrip.format

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.intellij.formatting.WrapType
import com.intellij.lang.ASTNode
import com.intellij.sql.formatter.model.BlockRole
import com.intellij.sql.formatter.model.CONTINUATION_INDENT
import com.intellij.sql.formatter.model.FlowPattern
import com.intellij.sql.formatter.model.NONE_INDENT
import com.intellij.sql.formatter.model.NORMAL_INDENT
import com.intellij.sql.formatter.model.SingletonExpandPattern
import com.intellij.sql.formatter.model.SingletonPattern
import com.intellij.sql.formatter.model.SqlCommentBlock
import com.intellij.sql.formatter.model.SqlKeyword
import com.intellij.sql.formatter.model.SqlNameBlock
import com.intellij.sql.formatter.model.SqlNodeBlock
import com.intellij.sql.formatter.model.SqlPhraseBlock
import com.intellij.sql.formatter.model.SqlQueryBlock
import com.intellij.sql.formatter.model.SqlRangeBlock
import com.intellij.sql.formatter.model.StartStopPattern
import com.intellij.sql.formatter.model.TailPattern
import com.intellij.sql.formatter.model.UntilPattern
import com.intellij.sql.formatter.model.matchElementClass
import com.intellij.sql.formatter.model.matchEverything
import com.intellij.sql.formatter.model.matchType
import com.intellij.sql.psi.SqlCommonKeywords
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlExpression

/**
 * View formatting with StarRocks-aware clauses before AS.
 *
 * The platform SqlViewBlock delegates that range to a private option block whose
 * keyword list cannot be extended. StarRocks clauses are composite PSI nodes, so
 * the platform treats them as continuation text and adds another indent level.
 */
class StarRocksMaterializedViewBlock : SqlNodeBlock() {
    override fun flowPatterns(): List<FlowPattern> = PATTERNS

    override fun whetherToFlatten(node: ASTNode): Boolean =
        node.elementType == SqlCompositeElementTypes.SQL_AS_QUERY_CLAUSE

    override fun configureFormattingAttributes() {
        val sql = context.sql
        val clauseWrap = makeWrap(WrapType.ALWAYS, false)
        val firstContent = nestedBlocks.indexOfFirst { it !is SqlCommentBlock }

        if (firstContent > 0) {
            nestedBlocks.subList(0, firstContent).forEach { it.myIndent = NONE_INDENT }
        }

        nestedBlocks.drop(firstContent.coerceAtLeast(0)).forEach { block ->
            when (block.role) {
                BlockRole.HEAD -> block.myIndent = NONE_INDENT
                BlockRole.ALIAS1 -> {
                    block.myWrap = null
                    block.myIndent = NORMAL_INDENT
                }
                BlockRole.PREFIX -> {
                    block.myWrap = clauseWrap
                    block.myIndent = NORMAL_INDENT
                }
                BlockRole.AS -> {
                    block.myWrap = if (sql.VIEW_WRAP_AS || hasRole(BlockRole.PREFIX)) clauseWrap else null
                    block.myIndent = NONE_INDENT
                }
                BlockRole.BODY -> {
                    block.myWrap = if (sql.VIEW_WRAP_QUERY) clauseWrap else null
                    block.myIndent = if (sql.VIEW_INDENT_QUERY) NORMAL_INDENT else NONE_INDENT
                }
                BlockRole.SUFFIX -> {
                    block.myWrap = clauseWrap
                    block.myIndent = NONE_INDENT
                }
                else -> block.myWrap = null
            }
        }
    }

    override fun userRequiresExpand(): Boolean = true

    private companion object {
        val PATTERNS: List<FlowPattern> = listOf(
            StartStopPattern(
                0,
                1,
                matchType(SqlCommonKeywords.SQL_CREATE),
                matchType(SqlCommonKeywords.SQL_VIEW),
                BlockRole.HEAD,
                ::SqlPhraseBlock
            ),
            SingletonExpandPattern(
                1,
                2,
                matchType(
                    SqlCompositeElementTypes.SQL_VIEW_REFERENCE,
                    SqlCompositeElementTypes.SQL_MATERIALIZED_VIEW_REFERENCE
                ),
                BlockRole.ALIAS1,
                ::SqlNameBlock
            ),
            UntilPattern(
                2,
                null,
                matchType(SqlCommonKeywords.SQL_AS, SqlCommonKeywords.SQL_IS),
                false,
                BlockRole.PREFIX,
                ::StarRocksMaterializedViewOptionsBlock
            ),
            SingletonPattern(
                null,
                3,
                matchType(SqlCommonKeywords.SQL_AS, SqlCommonKeywords.SQL_IS),
                BlockRole.AS,
                ::SqlKeyword
            ),
            SingletonPattern(
                3,
                4,
                matchElementClass(SqlExpression::class.java),
                BlockRole.BODY,
                ::SqlQueryBlock
            ),
            TailPattern(4, null, matchEverything(), BlockRole.SUFFIX, ::SqlPhraseBlock)
        )
    }
}

private class StarRocksMaterializedViewOptionsBlock : SqlRangeBlock() {
    override fun determineRole(node: ASTNode): BlockRole =
        if (node.elementType in CLAUSE_TYPES) BlockRole.ELEMENT else super.determineRole(node)

    override fun configureFormattingAttributes() {
        val clauseWrap = makeWrap(WrapType.ALWAYS, false)
        nestedBlocks.forEach { block ->
            if (block.role == BlockRole.ELEMENT) {
                block.myWrap = clauseWrap
                block.myIndent = NONE_INDENT
            } else {
                block.myWrap = null
                block.myIndent = CONTINUATION_INDENT
            }
        }
    }

    override fun userRequiresExpand(): Boolean = true

    private companion object {
        val CLAUSE_TYPES = setOf(
            StarRocksElementTypes.COMMENT_CLAUSE,
            StarRocksElementTypes.PARTITION_CLAUSE,
            StarRocksElementTypes.DISTRIBUTION_CLAUSE,
            StarRocksElementTypes.BUCKETS_CLAUSE,
            StarRocksElementTypes.MATERIALIZED_VIEW_ORDER_BY_CLAUSE,
            StarRocksElementTypes.REFRESH_CLAUSE,
            StarRocksElementTypes.PROPERTIES_CLAUSE
        )
    }
}

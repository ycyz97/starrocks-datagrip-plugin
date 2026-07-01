package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType

data class StarRocksClauseBoundary(
    val startOffset: Int,
    val elementType: IElementType
)

object StarRocksClauseBoundaryScanner {
    fun scan(builder: PsiBuilder, family: StarRocksStatementFamily?): List<StarRocksClauseBoundary> {
        val marker = builder.mark()
        val boundaries = mutableListOf<StarRocksClauseBoundary>()
        var parenDepth = 0
        var foundLeadingColumnList = false
        while (!builder.eof()) {
            val text = builder.tokenText?.uppercase()
            if (
                !foundLeadingColumnList &&
                parenDepth == 0 &&
                builder.tokenText == "(" &&
                (family == StarRocksStatementFamily.TABLE_DDL ||
                    family == StarRocksStatementFamily.VIEW ||
                    family == StarRocksStatementFamily.MATERIALIZED_VIEW)
            ) {
                boundaries += StarRocksClauseBoundary(builder.currentOffset, StarRocksElementTypes.TABLE_COLUMN_LIST)
                foundLeadingColumnList = true
            }
            when (builder.tokenText) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth--
                ";" -> if (parenDepth == 0) break
            }
            if (parenDepth == 0) {
                clauseType(builder, family, text)?.let { boundaries += StarRocksClauseBoundary(builder.currentOffset, it) }
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return boundaries.distinctBy { it.startOffset }
    }

    private fun clauseType(
        builder: PsiBuilder,
        family: StarRocksStatementFamily?,
        text: String?
    ): IElementType? {
        return when (family) {
            StarRocksStatementFamily.QUERY -> queryClauseType(builder, text)
            StarRocksStatementFamily.DML -> dmlClauseType(builder, text)
            StarRocksStatementFamily.TABLE_DDL -> ddlClauseType(builder, text)
            StarRocksStatementFamily.VIEW -> viewClauseType(builder, text)
            StarRocksStatementFamily.MATERIALIZED_VIEW -> materializedViewClauseType(builder, text)
            else -> null
        }
    }

    private fun queryClauseType(builder: PsiBuilder, text: String?): IElementType? {
        return when (text) {
            "WITH" -> StarRocksElementTypes.WITH_CLAUSE
            "SELECT" -> StarRocksElementTypes.SELECT_CLAUSE
            "FROM" -> StarRocksElementTypes.FROM_CLAUSE
            "WHERE" -> StarRocksElementTypes.WHERE_CLAUSE
            "GROUP" -> if (nextWord(builder) == "BY") StarRocksElementTypes.GROUP_BY_CLAUSE else null
            "HAVING" -> StarRocksElementTypes.HAVING_CLAUSE
            "WINDOW" -> if (isNamedWindowClauseStart(builder)) StarRocksElementTypes.WINDOW_CLAUSE else null
            "QUALIFY" -> StarRocksElementTypes.QUALIFY_CLAUSE
            "ORDER" -> if (nextWord(builder) == "BY") StarRocksElementTypes.ORDER_BY_CLAUSE else null
            "LIMIT" -> StarRocksElementTypes.LIMIT_CLAUSE
            in SET_OPERATION_WORDS -> StarRocksElementTypes.SET_OPERATION_CLAUSE
            else -> null
        }
    }

    private fun dmlClauseType(builder: PsiBuilder, text: String?): IElementType? {
        return when (text) {
            "INSERT", "UPDATE", "DELETE", "MERGE" -> StarRocksElementTypes.INSERT_TARGET_CLAUSE
            "SET" -> StarRocksElementTypes.SET_CLAUSE
            "VALUES" -> StarRocksElementTypes.VALUES_CLAUSE
            else -> queryClauseType(builder, text)
        }
    }

    private fun ddlClauseType(builder: PsiBuilder, text: String?): IElementType? {
        return when (text) {
            "PRIMARY", "DUPLICATE", "UNIQUE", "AGGREGATE" -> if (nextWord(builder) == "KEY") {
                StarRocksElementTypes.KEY_MODEL_CLAUSE
            } else {
                null
            }
            "COMMENT" -> StarRocksElementTypes.COMMENT_CLAUSE
            "PARTITION" -> if (nextWord(builder) == "BY") StarRocksElementTypes.PARTITION_CLAUSE else null
            "DISTRIBUTED" -> if (nextWord(builder) == "BY") StarRocksElementTypes.DISTRIBUTION_CLAUSE else null
            "ORDER" -> if (nextWord(builder) == "BY") StarRocksElementTypes.ORDER_BY_CLAUSE else null
            "PROPERTIES" -> StarRocksElementTypes.PROPERTIES_CLAUSE
            "AS" -> if (nextWord(builder) == "SELECT" || nextWord(builder) == "WITH") StarRocksElementTypes.AS_SELECT_CLAUSE else null
            else -> null
        }
    }

    private fun materializedViewClauseType(builder: PsiBuilder, text: String?): IElementType? {
        return when (text) {
            "COMMENT" -> StarRocksElementTypes.COMMENT_CLAUSE
            "PARTITION" -> if (nextWord(builder) == "BY") StarRocksElementTypes.PARTITION_CLAUSE else null
            "DISTRIBUTED" -> if (nextWord(builder) == "BY") StarRocksElementTypes.DISTRIBUTION_CLAUSE else null
            "ORDER" -> if (nextWord(builder) == "BY") StarRocksElementTypes.ORDER_BY_CLAUSE else null
            "REFRESH" -> StarRocksElementTypes.REFRESH_CLAUSE
            "PROPERTIES" -> StarRocksElementTypes.PROPERTIES_CLAUSE
            "AS" -> if (nextWord(builder) == "SELECT" || nextWord(builder) == "WITH") StarRocksElementTypes.AS_SELECT_CLAUSE else null
            else -> null
        }
    }

    private fun viewClauseType(builder: PsiBuilder, text: String?): IElementType? {
        return when (text) {
            "COMMENT" -> StarRocksElementTypes.COMMENT_CLAUSE
            "AS" -> if (nextWord(builder) == "SELECT" || nextWord(builder) == "WITH") StarRocksElementTypes.AS_SELECT_CLAUSE else null
            else -> null
        }
    }

    private fun nextWord(builder: PsiBuilder): String? {
        val marker = builder.mark()
        builder.advanceLexer()
        var result: String? = null
        while (!builder.eof()) {
            val text = builder.tokenText
            if (text != null && text.firstOrNull()?.isLetter() == true) {
                result = text.uppercase()
                break
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun isNamedWindowClauseStart(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        builder.advanceLexer()
        var seenWindowName = false
        var result = false
        while (!builder.eof()) {
            val text = builder.tokenText
            if (text != null && text.firstOrNull()?.let { it == '_' || it == '`' || it.isLetter() } == true) {
                if (!seenWindowName) {
                    seenWindowName = true
                } else {
                    result = text.equals("AS", ignoreCase = true)
                    break
                }
            } else if (text == "(" || text == ")" || text == "," || text == ";") {
                break
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private val SET_OPERATION_WORDS = setOf("UNION", "INTERSECT", "EXCEPT", "MINUS")
}

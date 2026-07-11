package com.github.ycyz.starrocks.datagrip.completion

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.resolve.containingElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.sql.psi.SqlCompositeElementTypes
import java.util.Locale

internal object StarRocksCompletionContext {
    fun isTable(position: PsiElement): Boolean {
        if (isProperty(position) || isColumnClause(position)) {
            return false
        }
        return containingElement(position, StarRocksElementTypes.DML_TARGET_TABLE) != null ||
            containingElement(position, SqlCompositeElementTypes.SQL_TABLE_REFERENCE) != null ||
            previousKeyword(position) in TABLE_PREVIOUS_KEYWORDS
    }

    fun isColumn(position: PsiElement): Boolean =
        !isTable(position) && !isProperty(position) &&
            (isColumnClause(position) || previousKeyword(position) in COLUMN_PREVIOUS_KEYWORDS)

    fun isOrderBy(position: PsiElement): Boolean =
        containingElement(position, StarRocksElementTypes.SQL_ORDER_BY_CLAUSE) != null

    fun isProperty(position: PsiElement): Boolean =
        containingElement(position, StarRocksElementTypes.PROPERTIES_CLAUSE) != null

    private fun isColumnClause(position: PsiElement): Boolean =
        COLUMN_CLAUSE_TYPES.any { containingElement(position, it) != null }

    private fun previousKeyword(position: PsiElement): String? {
        var leaf = PsiTreeUtil.prevVisibleLeaf(position)
        while (leaf != null) {
            val text = leaf.text
            if (IDENTIFIER.matches(text)) {
                return text.uppercase(Locale.ROOT)
            }
            leaf = PsiTreeUtil.prevVisibleLeaf(leaf)
        }
        return null
    }

    private val COLUMN_CLAUSE_TYPES = setOf(
        StarRocksElementTypes.SQL_SELECT_CLAUSE,
        StarRocksElementTypes.SQL_JOIN_CONDITION_CLAUSE,
        StarRocksElementTypes.SQL_WHERE_CLAUSE,
        StarRocksElementTypes.SQL_GROUP_BY_CLAUSE,
        StarRocksElementTypes.SQL_HAVING_CLAUSE,
        StarRocksElementTypes.SQL_QUALIFY_CLAUSE,
        StarRocksElementTypes.SQL_ORDER_BY_CLAUSE,
        StarRocksElementTypes.SQL_SET_CLAUSE
    )

    private val TABLE_PREVIOUS_KEYWORDS = setOf("FROM", "JOIN", "INTO", "UPDATE")
    private val COLUMN_PREVIOUS_KEYWORDS = setOf("SELECT", "ON", "WHERE", "HAVING", "QUALIFY", "SET")
    private val IDENTIFIER = Regex("[A-Za-z_][A-Za-z0-9_]*")
}

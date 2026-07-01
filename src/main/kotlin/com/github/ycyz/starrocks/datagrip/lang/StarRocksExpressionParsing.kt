package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder

object StarRocksExpressionParsing {
    @JvmStatic
    fun value_expression(builder: PsiBuilder, level: Int): Boolean {
        return value_expression(builder, level, EXPRESSION_BOUNDARIES)
    }

    @JvmStatic
    fun evaluable_expression(builder: PsiBuilder, level: Int): Boolean {
        return value_expression(builder, level)
    }

    fun value_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        val before = builder.currentOffset
        if (StarRocksDmlParsing.table_function_call(builder, level + 1)) {
            return true
        }
        StarRocksParsingUtil.consumeBalancedTail(builder, stopWords)
        return builder.currentOffset > before
    }

    @JvmStatic
    fun cast_type(builder: PsiBuilder, level: Int): Boolean {
        return StarRocksDdlParsing.type_element(builder, level)
    }

    private val EXPRESSION_BOUNDARIES = setOf(
        "FROM",
        "WHERE",
        "GROUP",
        "HAVING",
        "QUALIFY",
        "WINDOW",
        "ORDER",
        "LIMIT",
        "UNION",
        "INTERSECT",
        "EXCEPT",
        "MINUS"
    )
}

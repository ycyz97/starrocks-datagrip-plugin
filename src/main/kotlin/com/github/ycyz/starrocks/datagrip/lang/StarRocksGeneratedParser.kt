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
            return StarRocksDdlParsing.ddl_statement(builder, level + 1) ||
                StarRocksDmlParsing.dml_statement(builder, level + 1) ||
                StarRocksOtherParsing.other_statement(builder, level + 1) ||
                (word == "QUALIFY" && StarRocksDmlParsing.qualify_clause(builder, level + 1))
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

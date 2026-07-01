package com.github.ycyz.starrocks.datagrip.highlight

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.github.ycyz.starrocks.datagrip.lang.StarRocksHighlightTokenTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import com.intellij.sql.editor.SqlColors
import com.intellij.sql.editor.SqlSyntaxHighlighter

class StarRocksSyntaxHighlighter(
    project: Project?,
    file: VirtualFile?
) : SqlSyntaxHighlighter(StarRocksDialect.INSTANCE, project, file) {
    override fun getHighlightingLexer(): Lexer = StarRocksLexer(highlightCategories = true)

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            StarRocksHighlightTokenTypes.FUNCTION -> arrayOf(SqlColors.SQL_PROCEDURE)
            StarRocksHighlightTokenTypes.DATA_TYPE -> arrayOf(SqlColors.SQL_TYPE)
            StarRocksHighlightTokenTypes.VARIABLE -> arrayOf(SqlColors.SQL_VARIABLE)
            StarRocksHighlightTokenTypes.PARAMETER -> arrayOf(SqlColors.SQL_PARAMETER)
            else -> super.getTokenHighlights(tokenType)
        }
    }
}

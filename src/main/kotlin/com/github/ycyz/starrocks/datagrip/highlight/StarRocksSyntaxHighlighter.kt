package com.github.ycyz.starrocks.datagrip.highlight

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.github.ycyz.starrocks.datagrip.lang.StarRocksHighlightingLexer
import com.github.ycyz.starrocks.datagrip.lang.StarRocksHighlightTokenTypes
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
    override fun getHighlightingLexer(): Lexer = StarRocksHighlightingLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            // SQL_PROCEDURE inherits the static-method color, which is red in
            // Darcula. Functions without a resolved database object should
            // retain the platform's ordinary SQL identifier color instead.
            StarRocksHighlightTokenTypes.FUNCTION -> arrayOf(SqlColors.SQL_IDENT)
            StarRocksHighlightTokenTypes.DATA_TYPE -> arrayOf(SqlColors.SQL_TYPE)
            StarRocksHighlightTokenTypes.VARIABLE -> arrayOf(SqlColors.SQL_VARIABLE)
            StarRocksHighlightTokenTypes.PARAMETER -> arrayOf(SqlColors.SQL_PARAMETER)
            else -> super.getTokenHighlights(tokenType)
        }
    }
}

package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IFileElementType
import com.intellij.sql.dialects.base.SqlElementFactoryBase
import com.intellij.sql.dialects.base.SqlParserDefinitionBase

class StarRocksParserDefinition : SqlParserDefinitionBase() {
    override fun createElementFactory(): SqlElementFactoryBase = StarRocksElementFactory()

    override fun createLexer(project: Project?): Lexer {
        StarRocksTokenInitializer.ensureInitialized()
        return StarRocksParserLexer()
    }

    override fun createParser(project: Project?): PsiParser = StarRocksParser()

    override fun getFileNodeType(): IFileElementType = STARROCKS_SQL_FILE

    companion object {
        val STARROCKS_SQL_FILE: IFileElementType =
            IFileElementType("STARROCKS_SQL_FILE", StarRocksDialect.INSTANCE)
    }
}

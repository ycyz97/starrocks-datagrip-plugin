package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IFileElementType
import com.intellij.sql.dialects.base.SqlElementFactoryBase
import com.intellij.sql.dialects.base.SqlParserDefinitionBase
import com.intellij.sql.dialects.mysql.MysqlElementFactory
import com.intellij.sql.psi.stubs.SqlFileElementType
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect

class StarRocksParserDefinition : SqlParserDefinitionBase() {
    override fun createElementFactory(): SqlElementFactoryBase = MysqlElementFactory()

    override fun createLexer(project: Project?): Lexer = StarRocksLexer()

    override fun createParser(project: Project?): PsiParser = StarRocksParser()

    override fun getFileNodeType(): IFileElementType = STARROCKS_SQL_FILE

    private companion object {
        val STARROCKS_SQL_FILE: IFileElementType = SqlFileElementType("STARROCKS_SQL_FILE", StarRocksDialect.INSTANCE)
    }
}

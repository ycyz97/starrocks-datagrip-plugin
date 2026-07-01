package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IFileElementType
import com.intellij.sql.dialects.base.SqlElementFactoryBase
import com.intellij.sql.dialects.base.SqlParserDefinitionBase
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect

class StarRocksParserDefinition : SqlParserDefinitionBase() {
    override fun createElementFactory(): SqlElementFactoryBase = StarRocksElementFactory()

    override fun createLexer(project: Project?): Lexer = StarRocksLexer()

    override fun createParser(project: Project?): PsiParser = StarRocksParser()

    override fun getFileNodeType(): IFileElementType = STARROCKS_SQL_FILE

    private companion object {
        const val SQL_FILE_ELEMENT_TYPE_CLASS = "com.intellij.sql.psi.stubs.SqlFileElementType"

        val STARROCKS_SQL_FILE: IFileElementType = createSqlFileElementType()

        fun createSqlFileElementType(): IFileElementType {
            return try {
                val typeClass = Class.forName(SQL_FILE_ELEMENT_TYPE_CLASS)
                val constructor = typeClass.getConstructor(String::class.java, com.intellij.lang.Language::class.java)
                constructor.newInstance("STARROCKS_SQL_FILE", StarRocksDialect.INSTANCE) as IFileElementType
            } catch (_: ReflectiveOperationException) {
                IFileElementType("STARROCKS_SQL_FILE", StarRocksDialect.INSTANCE)
            } catch (_: LinkageError) {
                IFileElementType("STARROCKS_SQL_FILE", StarRocksDialect.INSTANCE)
            }
        }
    }
}

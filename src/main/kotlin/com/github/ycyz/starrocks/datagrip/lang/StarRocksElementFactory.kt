package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.base.SqlElementFactory
import com.intellij.sql.dialects.base.SqlElementFactoryBase
import com.intellij.sql.dialects.sql92.Sql92ParserDefinition
import com.intellij.sql.psi.SqlCommonKeywords
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlTokenType
import com.intellij.sql.psi.impl.SqlCreateTableStatementImpl
import com.intellij.sql.psi.impl.SqlCreateViewStatementImpl
import com.intellij.sql.psi.impl.SqlCompositeElementImpl
import com.intellij.sql.psi.impl.SqlStatementImpl
import com.intellij.sql.psi.impl.SqlUseDatabaseStatementImpl
import com.intellij.sql.util.SqlTokenRegistry
import java.util.Locale

class StarRocksElementFactory : SqlElementFactory(), StarRocksTokens {
    private val platformElementFactory: SqlElementFactoryBase = Sql92ParserDefinition().elementFactory

    override fun getStaticInfo(): Info = INFO

    override fun createElementNode(type: IElementType): CompositeElement {
        if (type is StarRocksNamedStubElementType) {
            return StarRocksNamedStubCompositeElement(type)
        }
        if (type in REGISTERED_PLATFORM_TYPES) {
            return CompositeElement(type)
        }
        if (type in STATEMENT_TYPES) {
            return SqlStatementImpl(type)
        }
        if (type == StarRocksElementTypes.TABLE_REFERENCE_NAME) {
            return StarRocksTableReferenceNameElement(type)
        }
        if (type == StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX) {
            return StarRocksQualifiedColumnPrefixElement(type)
        }
        if (type == StarRocksElementTypes.COLUMN_REFERENCE_NAME) {
            return StarRocksColumnReferenceNameElement(type)
        }
        if (type == StarRocksElementTypes.WINDOW_REFERENCE_NAME) {
            return StarRocksWindowReferenceNameElement(type)
        }
        if (type is StarRocksElementType) {
            return SqlCompositeElementImpl(type)
        }
        return platformElementFactory.createElementNode(type) ?: SqlCompositeElementImpl(type)
    }

    override fun createCompositeElement(node: ASTNode): PsiElement {
        if (node.elementType is StarRocksNamedStubElementType) {
            return StarRocksNamedStubElement(node)
        }
        if (node.elementType == StarRocksElementTypes.TABLE_REFERENCE_NAME && node is PsiElement) {
            return node
        }
        if (node.elementType == StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX && node is PsiElement) {
            return node
        }
        if (node.elementType == StarRocksElementTypes.COLUMN_REFERENCE_NAME && node is PsiElement) {
            return node
        }
        if (node.elementType == StarRocksElementTypes.WINDOW_REFERENCE_NAME && node is PsiElement) {
            return node
        }
        if (node.elementType is StarRocksElementType) {
            return (node as? PsiElement) ?: ASTWrapperPsiElement(node)
        }
        if (node.elementType in REGISTERED_PLATFORM_TYPES) {
            return super.createCompositeElement(node)
        }
        return platformElementFactory.createCompositeElement(node) ?: ASTWrapperPsiElement(node)
    }

    companion object {
        init {
            SqlTokenRegistry.ensureInterfacesAreInitializedInOrder(StarRocksElementFactory::class.java)
        }

        @JvmStatic
        fun token(text: String): SqlTokenType {
            val commonKeyword = runCatching {
                SqlCommonKeywords::class.java
                    .getField("SQL_${text.uppercase(Locale.ROOT)}")
                    .get(null) as? SqlTokenType
            }.getOrNull()
            return commonKeyword ?: SqlTokenRegistry.getType(text)
        }

        private val INFO = Info().also { info ->
            getDefaultRegistrations(info)
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT,
                SqlCreateTableStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT,
                SqlCreateViewStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT,
                SqlCreateViewStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_USE_SCHEMA_STATEMENT,
                SqlUseDatabaseStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_USE_CATALOG_STATEMENT,
                SqlUseDatabaseStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_USE_NAMESPACE_STATEMENT,
                SqlUseDatabaseStatementImpl::class.java
            )
        }

        private val STATEMENT_TYPES = StarRocksStatementElementSets.STARROCKS_STATEMENT_TYPES

        private val REGISTERED_PLATFORM_TYPES = setOf(
            SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT,
            SqlCompositeElementTypes.SQL_USE_SCHEMA_STATEMENT,
            SqlCompositeElementTypes.SQL_USE_CATALOG_STATEMENT,
            SqlCompositeElementTypes.SQL_USE_NAMESPACE_STATEMENT
        )
    }
}

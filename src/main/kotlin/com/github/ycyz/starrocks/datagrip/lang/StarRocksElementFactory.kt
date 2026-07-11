package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.base.SqlElementFactory
import com.intellij.sql.dialects.base.SqlElementFactoryBase
import com.intellij.sql.dialects.sql92.Sql92ParserDefinition
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlTokenType
import com.intellij.sql.psi.impl.SqlAlterStatementImpl
import com.intellij.sql.psi.impl.SqlAlterTableStatementImpl
import com.intellij.sql.psi.impl.SqlCommitStatementImpl
import com.intellij.sql.psi.impl.SqlCompositeElementImpl
import com.intellij.sql.psi.impl.SqlCreateCatalogStatementImpl
import com.intellij.sql.psi.impl.SqlCreateIndexStatementImpl
import com.intellij.sql.psi.impl.SqlCreateSchemaStatementImpl
import com.intellij.sql.psi.impl.SqlCreateTableStatementImpl
import com.intellij.sql.psi.impl.SqlCreateViewStatementImpl
import com.intellij.sql.psi.impl.SqlDeleteStatementImpl
import com.intellij.sql.psi.impl.SqlExplainStatementImpl
import com.intellij.sql.psi.impl.SqlFromClauseImpl
import com.intellij.sql.psi.impl.SqlGrantStatementImpl
import com.intellij.sql.psi.impl.SqlInsertStatementImpl
import com.intellij.sql.psi.impl.SqlJoinConditionClauseImpl
import com.intellij.sql.psi.impl.SqlJoinExpressionImpl
import com.intellij.sql.psi.impl.SqlMergeStatementImpl
import com.intellij.sql.psi.impl.SqlRevokeStatementImpl
import com.intellij.sql.psi.impl.SqlRollbackStatementImpl
import com.intellij.sql.psi.impl.SqlSetStatementImpl
import com.intellij.sql.psi.impl.SqlStatementImpl
import com.intellij.sql.psi.impl.SqlTableExpressionImpl
import com.intellij.sql.psi.impl.SqlTruncateTableStatementImpl
import com.intellij.sql.psi.impl.SqlUpdateStatementImpl
import com.intellij.sql.psi.impl.SqlUseDatabaseStatementImpl
import com.intellij.sql.psi.impl.SqlUsingClauseImpl
import com.intellij.sql.util.SqlTokenRegistry

class StarRocksElementFactory : SqlElementFactory(), StarRocksTokens {
    private val platformElementFactory: SqlElementFactoryBase = Sql92ParserDefinition().elementFactory

    override fun getStaticInfo(): Info = INFO

    override fun createElementNode(type: IElementType): CompositeElement {
        if (type is StarRocksNamedStubElementType) {
            return StarRocksNamedStubCompositeElement(type)
        }
        if (type in StarRocksPlatformElementSets.registeredStarRocksTypes) {
            return super.createElementNode(type) ?: CompositeElement(type)
        }
        if (type in StarRocksPlatformElementSets.registeredPlatformTypes) {
            return CompositeElement(type)
        }
        if (type in StarRocksPlatformElementSets.genericStatementTypes) {
            return SqlStatementImpl(type)
        }
        if (type in StarRocksPlatformElementSets.statementTypes) {
            return SqlStatementImpl(type)
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
        if (node.elementType == StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX && node is PsiElement) {
            return node
        }
        if (node.elementType == StarRocksElementTypes.COLUMN_REFERENCE_NAME && node is PsiElement) {
            return node
        }
        if (node.elementType == StarRocksElementTypes.WINDOW_REFERENCE_NAME && node is PsiElement) {
            return node
        }
        if (node.elementType in StarRocksPlatformElementSets.registeredStarRocksTypes) {
            return (node as? PsiElement) ?: super.createCompositeElement(node)
        }
        if (node.elementType is StarRocksElementType) {
            return (node as? PsiElement) ?: ASTWrapperPsiElement(node)
        }
        if (node.elementType in StarRocksPlatformElementSets.registeredPlatformTypes) {
            return super.createCompositeElement(node)
        }
        if (node.elementType in StarRocksPlatformElementSets.genericStatementTypes) {
            return (node as? PsiElement) ?: ASTWrapperPsiElement(node)
        }
        if (node.elementType in StarRocksPlatformElementSets.statementTypes) {
            return (node as? PsiElement) ?: super.createCompositeElement(node)
        }
        return platformElementFactory.createCompositeElement(node) ?: ASTWrapperPsiElement(node)
    }

    companion object {
        init {
            SqlTokenRegistry.ensureInterfacesAreInitializedInOrder(StarRocksElementFactory::class.java)
        }

        @JvmStatic
        fun token(text: String): SqlTokenType = StarRocksElementTypeRegistry.token(text)

        @JvmStatic
        fun elementType(name: String): IElementType = StarRocksElementTypeRegistry.elementType(name)

        private val INFO: Info by lazy {
            Info().also { info ->
            getDefaultRegistrations(info)
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT,
                SqlCreateTableStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_INSERT_STATEMENT,
                SqlInsertStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_UPDATE_STATEMENT,
                SqlUpdateStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_DELETE_STATEMENT,
                SqlDeleteStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_MERGE_STATEMENT,
                SqlMergeStatementImpl::class.java
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
                SqlCompositeElementTypes.SQL_CREATE_CATALOG_STATEMENT,
                SqlCreateCatalogStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_CREATE_SCHEMA_STATEMENT,
                SqlCreateSchemaStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_CREATE_INDEX_STATEMENT,
                SqlCreateIndexStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_ALTER_TABLE_STATEMENT,
                SqlAlterTableStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_ALTER_SCHEMA_STATEMENT,
                SqlAlterStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_ALTER_VIEW_STATEMENT,
                SqlAlterStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_ALTER_CATALOG_STATEMENT,
                SqlAlterStatementImpl::class.java
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
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_SET_STATEMENT,
                SqlSetStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_EXPLAIN_STATEMENT,
                SqlExplainStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_GRANT_STATEMENT,
                SqlGrantStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_REVOKE_STATEMENT,
                SqlRevokeStatementImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.GRANT_STATEMENT,
                SqlGrantStatementImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.REVOKE_STATEMENT,
                SqlRevokeStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_COMMIT_STATEMENT,
                SqlCommitStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_ROLLBACK_STATEMENT,
                SqlRollbackStatementImpl::class.java
            )
            registerImplementation(
                info,
                SqlCompositeElementTypes.SQL_TRUNCATE_TABLE_STATEMENT,
                SqlTruncateTableStatementImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.SQL_FROM_CLAUSE,
                SqlFromClauseImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.SQL_TABLE_EXPRESSION,
                SqlTableExpressionImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.SQL_PARENTHESIZED_JOIN_EXPRESSION,
                SqlTableExpressionImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.SQL_JOIN_EXPRESSION,
                SqlJoinExpressionImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.SQL_JOIN_CONDITION_CLAUSE,
                SqlJoinConditionClauseImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.SQL_USING_CLAUSE,
                SqlUsingClauseImpl::class.java
            )
            }
        }

    }
}

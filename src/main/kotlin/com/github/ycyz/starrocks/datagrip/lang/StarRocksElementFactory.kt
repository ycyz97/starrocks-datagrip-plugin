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
import com.intellij.sql.psi.impl.SqlAlterTableStatementImpl
import com.intellij.sql.psi.impl.SqlCommitStatementImpl
import com.intellij.sql.psi.impl.SqlCompositeElementImpl
import com.intellij.sql.psi.impl.SqlCreateCatalogStatementImpl
import com.intellij.sql.psi.impl.SqlCreateIndexStatementImpl
import com.intellij.sql.psi.impl.SqlCreateSchemaStatementImpl
import com.intellij.sql.psi.impl.SqlCreateTableStatementImpl
import com.intellij.sql.psi.impl.SqlCreateViewStatementImpl
import com.intellij.sql.psi.impl.SqlDeleteStatementImpl
import com.intellij.sql.psi.impl.SqlExpressionImpl
import com.intellij.sql.psi.impl.SqlFromClauseImpl
import com.intellij.sql.psi.impl.SqlGrantStatementImpl
import com.intellij.sql.psi.impl.SqlInsertStatementImpl
import com.intellij.sql.psi.impl.SqlJoinConditionClauseImpl
import com.intellij.sql.psi.impl.SqlJoinExpressionImpl
import com.intellij.sql.psi.impl.SqlMergeStatementImpl
import com.intellij.sql.psi.impl.SqlRevokeStatementImpl
import com.intellij.sql.psi.impl.SqlRollbackStatementImpl
import com.intellij.sql.psi.impl.SqlStatementImpl
import com.intellij.sql.psi.impl.SqlTableExpressionImpl
import com.intellij.sql.psi.impl.SqlTruncateTableStatementImpl
import com.intellij.sql.psi.impl.SqlUpdateStatementImpl
import com.intellij.sql.psi.impl.SqlUseDatabaseStatementImpl
import com.intellij.sql.psi.impl.SqlUsingClauseImpl
import com.intellij.sql.util.SqlTokenRegistry
import java.util.Locale

class StarRocksElementFactory : SqlElementFactory(), StarRocksTokens {
    private val platformElementFactory: SqlElementFactoryBase = Sql92ParserDefinition().elementFactory

    override fun getStaticInfo(): Info = INFO

    override fun createElementNode(type: IElementType): CompositeElement {
        if (type is StarRocksNamedStubElementType) {
            return StarRocksNamedStubCompositeElement(type)
        }
        if (type in REGISTERED_STARROCKS_PLATFORM_TYPES) {
            return super.createElementNode(type) ?: CompositeElement(type)
        }
        if (type in REGISTERED_PLATFORM_TYPES) {
            return CompositeElement(type)
        }
        if (type in GENERIC_PLATFORM_STATEMENT_TYPES) {
            return SqlStatementImpl(type)
        }
        if (type in STATEMENT_TYPES) {
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
        if (node.elementType in REGISTERED_STARROCKS_PLATFORM_TYPES) {
            return (node as? PsiElement) ?: super.createCompositeElement(node)
        }
        if (node.elementType is StarRocksElementType) {
            return (node as? PsiElement) ?: ASTWrapperPsiElement(node)
        }
        if (node.elementType in REGISTERED_PLATFORM_TYPES) {
            return super.createCompositeElement(node)
        }
        if (node.elementType in GENERIC_PLATFORM_STATEMENT_TYPES) {
            return (node as? PsiElement) ?: ASTWrapperPsiElement(node)
        }
        if (node.elementType in STATEMENT_TYPES) {
            return (node as? PsiElement) ?: super.createCompositeElement(node)
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
                StarRocksElementTypes.FROM_CLAUSE,
                SqlFromClauseImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.TABLE_EXPRESSION,
                SqlTableExpressionImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.PARENTHESIZED_JOIN_EXPRESSION,
                SqlTableExpressionImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.JOIN_EXPRESSION,
                SqlJoinExpressionImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.JOIN_CONDITION_CLAUSE,
                SqlJoinConditionClauseImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.USING_CLAUSE,
                SqlUsingClauseImpl::class.java
            )
            registerImplementation(
                info,
                StarRocksElementTypes.PREDICATE_EXPRESSION,
                SqlExpressionImpl::class.java
            )
        }

        private val STATEMENT_TYPES = StarRocksStatementElementSets.STARROCKS_STATEMENT_TYPES

        private val GENERIC_PLATFORM_STATEMENT_TYPES = setOf(
            SqlCompositeElementTypes.SQL_START_TRANSACTION_STATEMENT,
            SqlCompositeElementTypes.SQL_ALTER_SCHEMA_STATEMENT,
            SqlCompositeElementTypes.SQL_ALTER_VIEW_STATEMENT,
            SqlCompositeElementTypes.SQL_ALTER_CATALOG_STATEMENT
        )

        private val REGISTERED_STARROCKS_PLATFORM_TYPES = setOf(
            StarRocksElementTypes.FROM_CLAUSE,
            StarRocksElementTypes.TABLE_EXPRESSION,
            StarRocksElementTypes.PARENTHESIZED_JOIN_EXPRESSION,
            StarRocksElementTypes.JOIN_EXPRESSION,
            StarRocksElementTypes.JOIN_CONDITION_CLAUSE,
            StarRocksElementTypes.USING_CLAUSE,
            StarRocksElementTypes.PREDICATE_EXPRESSION,
            SqlCompositeElementTypes.SQL_TABLE_REFERENCE,
            SqlCompositeElementTypes.SQL_INSERT_STATEMENT,
            SqlCompositeElementTypes.SQL_UPDATE_STATEMENT,
            SqlCompositeElementTypes.SQL_DELETE_STATEMENT,
            SqlCompositeElementTypes.SQL_MERGE_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_CATALOG_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_SCHEMA_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_INDEX_STATEMENT,
            SqlCompositeElementTypes.SQL_ALTER_TABLE_STATEMENT,
            SqlCompositeElementTypes.SQL_GRANT_STATEMENT,
            SqlCompositeElementTypes.SQL_REVOKE_STATEMENT,
            SqlCompositeElementTypes.SQL_COMMIT_STATEMENT,
            SqlCompositeElementTypes.SQL_ROLLBACK_STATEMENT,
            SqlCompositeElementTypes.SQL_TRUNCATE_TABLE_STATEMENT
        )

        private val REGISTERED_PLATFORM_TYPES = setOf(
            SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT,
            SqlCompositeElementTypes.SQL_USE_SCHEMA_STATEMENT,
            SqlCompositeElementTypes.SQL_USE_CATALOG_STATEMENT,
            SqlCompositeElementTypes.SQL_USE_NAMESPACE_STATEMENT,
            SqlCompositeElementTypes.SQL_CALL_STATEMENT
        )
    }
}

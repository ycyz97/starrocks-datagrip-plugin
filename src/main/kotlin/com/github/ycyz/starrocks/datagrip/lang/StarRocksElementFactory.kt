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
import com.intellij.sql.psi.SqlTokens
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
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

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
        private val TOKENS = ConcurrentHashMap<String, SqlTokenType>()

        init {
            SqlTokenRegistry.ensureInterfacesAreInitializedInOrder(StarRocksElementFactory::class.java)
        }

        @JvmStatic
        fun token(text: String): SqlTokenType {
            val normalized = text.uppercase(Locale.ROOT)
            return TOKENS.computeIfAbsent(normalized, ::createToken)
        }

        private fun createToken(normalized: String): SqlTokenType {
            if (normalized == "STARROCKS_PARAMETER") {
                return StarRocksHighlightTokenTypes.PARAMETER
            }
            punctuationToken(normalized)?.let { return it }
            sqlToken(normalized)?.let { return it }
            commonKeyword("SQL_$normalized")?.let { return it }
            return SqlTokenRegistry.getType(normalized)
        }

        private fun punctuationToken(text: String): SqlTokenType? {
            return when (text) {
                "(" -> SqlTokens.SQL_LEFT_PAREN
                ")" -> SqlTokens.SQL_RIGHT_PAREN
                "[" -> SqlTokens.SQL_LEFT_BRACKET
                "]" -> SqlTokens.SQL_RIGHT_BRACKET
                "{" -> SqlTokens.SQL_LEFT_BRACE
                "}" -> SqlTokens.SQL_RIGHT_BRACE
                "," -> SqlTokens.SQL_COMMA
                ";" -> SqlTokens.SQL_SEMICOLON
                "." -> SqlTokens.SQL_PERIOD
                ":" -> SqlTokens.SQL_COLON
                "+" -> SqlTokens.SQL_OP_PLUS
                "-" -> SqlTokens.SQL_OP_MINUS
                "*" -> SqlTokens.SQL_ASTERISK
                "/" -> SqlTokens.SQL_OP_DIV
                "%" -> SqlTokens.SQL_OP_MODULO
                "=" -> SqlTokens.SQL_OP_EQ
                "<" -> SqlTokens.SQL_OP_LT
                ">" -> SqlTokens.SQL_OP_GT
                "<=" -> SqlTokens.SQL_OP_LE
                ">=" -> SqlTokens.SQL_OP_GE
                "<>" -> SqlTokens.SQL_OP_NEQ
                "!=" -> SqlTokens.SQL_OP_NEQ2
                "<<" -> SqlTokens.SQL_OP_LEFT_SHIFT
                ">>" -> SqlTokens.SQL_OP_RIGHT_SHIFT
                "||" -> SqlTokens.SQL_OP_CONCAT
                "!" -> SqlTokens.SQL_OP_NOT2
                "|" -> SqlTokens.SQL_OP_BITWISE_OR
                "&" -> SqlTokens.SQL_OP_BITWISE_AND
                "?" -> StarRocksHighlightTokenTypes.PARAMETER
                else -> null
            }
        }

        @JvmStatic
        fun elementType(name: String): IElementType {
            stubElementType(name)?.let { return it }
            platformCompositeElementType(name)?.let { return it }
            return StarRocksElementType("STARROCKS_$name")
        }

        private fun sqlToken(name: String): SqlTokenType? {
            return runCatching {
                SqlTokens::class.java.getField(name).get(null) as? SqlTokenType
            }.getOrNull()
        }

        private fun commonKeyword(name: String): SqlTokenType? {
            return runCatching {
                SqlCommonKeywords::class.java.getField(name).get(null) as? SqlTokenType
            }.getOrNull()
        }

        private fun platformCompositeElementType(name: String): IElementType? {
            return runCatching {
                SqlCompositeElementTypes::class.java.getField(name).get(null) as? IElementType
            }.getOrNull()
        }

        private fun stubElementType(name: String): IElementType? {
            return when (name) {
                "COLUMN_NAME" -> StarRocksStubElementTypes.STARROCKS_COLUMN_NAME
                "CTE_COLUMN_NAME" -> StarRocksStubElementTypes.STARROCKS_CTE_COLUMN_NAME
                "TABLE_ALIAS" -> StarRocksStubElementTypes.STARROCKS_TABLE_ALIAS
                "TABLE_ALIAS_COLUMN_NAME" -> StarRocksStubElementTypes.STARROCKS_TABLE_ALIAS_COLUMN_NAME
                "WINDOW_NAME" -> StarRocksStubElementTypes.STARROCKS_WINDOW_NAME
                "SELECT_ALIAS" -> StarRocksStubElementTypes.STARROCKS_SELECT_ALIAS
                else -> null
            }
        }

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

        private val STATEMENT_TYPES by lazy {
            StarRocksStatementElementSets.STARROCKS_STATEMENT_TYPES
        }

        private val GENERIC_PLATFORM_STATEMENT_TYPES by lazy {
            setOf(
            SqlCompositeElementTypes.SQL_START_TRANSACTION_STATEMENT
            )
        }

        private val REGISTERED_STARROCKS_PLATFORM_TYPES by lazy {
            setOf(
            StarRocksElementTypes.SQL_FROM_CLAUSE,
            StarRocksElementTypes.SQL_TABLE_EXPRESSION,
            StarRocksElementTypes.SQL_PARENTHESIZED_JOIN_EXPRESSION,
            StarRocksElementTypes.SQL_JOIN_EXPRESSION,
            StarRocksElementTypes.SQL_JOIN_CONDITION_CLAUSE,
            StarRocksElementTypes.SQL_USING_CLAUSE,
            SqlCompositeElementTypes.SQL_TABLE_REFERENCE,
            StarRocksElementTypes.SQL_INSERT_STATEMENT,
            StarRocksElementTypes.SQL_UPDATE_STATEMENT,
            StarRocksElementTypes.SQL_DELETE_STATEMENT,
            StarRocksElementTypes.SQL_MERGE_STATEMENT,
            StarRocksElementTypes.SQL_CREATE_CATALOG_STATEMENT,
            StarRocksElementTypes.SQL_CREATE_SCHEMA_STATEMENT,
            StarRocksElementTypes.SQL_CREATE_INDEX_STATEMENT,
            StarRocksElementTypes.SQL_ALTER_TABLE_STATEMENT,
            StarRocksElementTypes.SQL_ALTER_SCHEMA_STATEMENT,
            StarRocksElementTypes.SQL_ALTER_VIEW_STATEMENT,
            StarRocksElementTypes.SQL_ALTER_CATALOG_STATEMENT,
            StarRocksElementTypes.SQL_SET_STATEMENT,
            StarRocksElementTypes.SQL_EXPLAIN_STATEMENT,
            SqlCompositeElementTypes.SQL_GRANT_STATEMENT,
            SqlCompositeElementTypes.SQL_REVOKE_STATEMENT,
            StarRocksElementTypes.GRANT_STATEMENT,
            StarRocksElementTypes.REVOKE_STATEMENT,
            StarRocksElementTypes.SQL_COMMIT_STATEMENT,
            StarRocksElementTypes.SQL_ROLLBACK_STATEMENT,
            StarRocksElementTypes.SQL_TRUNCATE_TABLE_STATEMENT
            )
        }

        private val REGISTERED_PLATFORM_TYPES by lazy {
            setOf(
            StarRocksElementTypes.SQL_CREATE_TABLE_STATEMENT,
            StarRocksElementTypes.SQL_CREATE_VIEW_STATEMENT,
            StarRocksElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT,
            StarRocksElementTypes.SQL_USE_SCHEMA_STATEMENT,
            SqlCompositeElementTypes.SQL_USE_CATALOG_STATEMENT,
            SqlCompositeElementTypes.SQL_USE_NAMESPACE_STATEMENT,
            StarRocksElementTypes.SQL_CALL_STATEMENT
            )
        }
    }
}

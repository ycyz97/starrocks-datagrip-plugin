package com.github.ycyz.starrocks.datagrip.dialect

import com.github.ycyz.starrocks.datagrip.StarRocksIcons
import com.github.ycyz.starrocks.datagrip.database.StarRocksDataType
import com.github.ycyz.starrocks.datagrip.database.StarRocksDbms
import com.github.ycyz.starrocks.datagrip.lang.StarRocksTokens
import com.intellij.database.Dbms
import com.intellij.database.model.ObjectName
import com.intellij.database.psi.DbDataSource
import com.intellij.database.util.TreePattern
import com.intellij.database.util.TreePatternNode
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveState
import com.intellij.sql.dialects.BuiltinFunction
import com.intellij.sql.dialects.base.SqlLanguageDialectBase
import com.intellij.sql.dialects.base.TokensHelper
import com.intellij.sql.dialects.functions.SqlFunctionsUtil
import com.intellij.sql.dialects.SqlDialectImplUtilCore
import com.intellij.sql.psi.SqlScopeProcessor
import com.intellij.psi.tree.IElementType
import javax.swing.Icon

class StarRocksDialect private constructor() : SqlLanguageDialectBase("StarRocks") {
    override fun getDbms(): Dbms = StarRocksDbms.INSTANCE
    override fun getIcon(): Icon = StarRocksIcons.Dialect
    override fun createTokensHelper(): TokensHelper = TokensHelper(
        StarRocksTokens::class.java,
        SqlFunctionsUtil.loadFunctionDefinition(this)
    )

    override fun addTypes(types: MutableMap<String, BuiltinFunction.Type>) {
        super.addTypes(types)
        StarRocksDataType.entries.forEach { type ->
            SqlFunctionsUtil.addSimpleType(types, type.sqlName, type.sqlName, this)
        }
    }
    override fun isOperatorSupported(token: IElementType?): Boolean = true
    override fun getSystemVariables(): Set<String> = emptySet()

    override fun getBaseImports(dataSource: DbDataSource?, path: Array<out ObjectName?>?): TreePattern {
        if (dataSource == null || path == null) {
            return super.getBaseImports(dataSource, path)
        }
        val defaultNamespace = getDefaultNamespace(dataSource, null)
        return if (defaultNamespace != null) {
            SqlDialectImplUtilCore.createObjectPattern(
                path,
                defaultNamespace,
                *emptyArray<TreePatternNode.Group>()
            )
        } else {
            getSchemaBaseImports(dataSource, path, false)
        }
    }

    override fun processUnqualifiedResolve(
        processor: SqlScopeProcessor,
        state: ResolveState,
        reference: PsiReference
    ): Boolean {
        return processAliases(processor, state, reference) &&
            super.processUnqualifiedResolve(processor, state, reference)
    }

    companion object {
        @JvmField
        val INSTANCE: StarRocksDialect = StarRocksDialect()
    }
}

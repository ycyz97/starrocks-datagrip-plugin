package com.github.ycyz.starrocks.datagrip.dialect

import com.github.ycyz.starrocks.datagrip.StarRocksIcons
import com.github.ycyz.starrocks.datagrip.database.StarRocksDbms
import com.github.ycyz.starrocks.datagrip.lang.StarRocksTokens
import com.intellij.database.Dbms
import com.intellij.sql.dialects.base.SqlLanguageDialectBase
import com.intellij.sql.dialects.base.TokensHelper
import com.intellij.psi.tree.IElementType
import javax.swing.Icon

class StarRocksDialect private constructor() : SqlLanguageDialectBase("StarRocks") {
    override fun getDbms(): Dbms = StarRocksDbms.INSTANCE
    override fun getIcon(): Icon = StarRocksIcons.Dialect
    override fun createTokensHelper(): TokensHelper = createTokensHelper(StarRocksTokens::class.java)
    override fun isOperatorSupported(token: IElementType): Boolean = true
    override fun getSystemVariables(): Set<String> = emptySet()

    companion object {
        @JvmField
        val INSTANCE: StarRocksDialect = StarRocksDialect()
    }
}

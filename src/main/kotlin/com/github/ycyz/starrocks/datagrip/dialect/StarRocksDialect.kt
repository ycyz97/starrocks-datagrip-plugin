package com.github.ycyz.starrocks.datagrip.dialect

import com.intellij.database.Dbms
import com.intellij.sql.dialects.base.TokensHelper
import com.intellij.sql.dialects.mysql.MysqlDialectBase
import com.intellij.sql.dialects.mysql.MysqlTokens
import com.github.ycyz.starrocks.datagrip.StarRocksIcons
import javax.swing.Icon

class StarRocksDialect private constructor() : MysqlDialectBase("StarRocks") {
    override fun getDbms(): Dbms = StarRocksDbmsHolder.INSTANCE
    override fun getIcon(): Icon = StarRocksIcons.Dialect
    override fun createTokensHelper(): TokensHelper = createTokensHelper(MysqlTokens::class.java)

    companion object {
        @JvmField
        val INSTANCE: StarRocksDialect = StarRocksDialect()
    }
}

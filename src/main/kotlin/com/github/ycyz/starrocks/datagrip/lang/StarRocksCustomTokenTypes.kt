package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.sql.psi.SqlTokenType

object StarRocksCustomTokenTypes {
    @JvmField
    val PARAMETER = SqlTokenType("STARROCKS_PARAMETER", StarRocksDialect.INSTANCE)
}

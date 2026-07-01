package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.sql.psi.SqlTokenType

object StarRocksHighlightTokenTypes {
    @JvmField
    val FUNCTION = SqlTokenType("STARROCKS_FUNCTION", StarRocksDialect.INSTANCE)

    @JvmField
    val DATA_TYPE = SqlTokenType("STARROCKS_DATA_TYPE", StarRocksDialect.INSTANCE)

    @JvmField
    val VARIABLE = SqlTokenType("STARROCKS_VARIABLE", StarRocksDialect.INSTANCE)

    @JvmField
    val PARAMETER = SqlTokenType("STARROCKS_PARAMETER", StarRocksDialect.INSTANCE)
}

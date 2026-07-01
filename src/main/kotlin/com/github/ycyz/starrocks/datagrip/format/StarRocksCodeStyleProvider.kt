package com.github.ycyz.starrocks.datagrip.format

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.sql.dialects.base.BaseSqlDialectCodeStyleProvider

class StarRocksCodeStyleProvider : BaseSqlDialectCodeStyleProvider<StarRocksCodeStyleSettings>(
    StarRocksDialect.INSTANCE,
    StarRocksCodeStyleSettings::class.java,
    "StarRocks"
)

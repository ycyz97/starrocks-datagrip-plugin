package com.github.ycyz.starrocks.datagrip.format

import com.intellij.sql.dialects.base.BaseSqlDialectCodeStyleProvider
import com.intellij.sql.dialects.mysql.MysqlCodeStyleSettings
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect

class StarRocksCodeStyleProvider : BaseSqlDialectCodeStyleProvider<MysqlCodeStyleSettings>(
    StarRocksDialect.INSTANCE,
    MysqlCodeStyleSettings::class.java,
    "StarRocks"
)

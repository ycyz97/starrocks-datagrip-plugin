package com.github.ycyz.starrocks.datagrip.database

import com.github.ycyz.starrocks.datagrip.StarRocksIcons
import com.intellij.database.Dbms

class StarRocksDbms private constructor() {
    companion object {
        @JvmField
        val INSTANCE: Dbms = Dbms.create("STARROCKS", "StarRocks", { StarRocksIcons.DataSource })
    }
}

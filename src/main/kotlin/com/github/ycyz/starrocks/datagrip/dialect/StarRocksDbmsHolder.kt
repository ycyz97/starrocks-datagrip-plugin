package com.github.ycyz.starrocks.datagrip.dialect

import com.intellij.database.Dbms
import com.github.ycyz.starrocks.datagrip.StarRocksIcons

class StarRocksDbmsHolder private constructor() {
    companion object {
        @JvmField
        val INSTANCE: Dbms = Dbms.create("STARROCKS", "StarRocks", { StarRocksIcons.DataSource })
    }
}

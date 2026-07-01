package com.github.ycyz.starrocks.datagrip.dialect

import com.github.ycyz.starrocks.datagrip.database.StarRocksDbms

@Deprecated("Use com.github.ycyz.starrocks.datagrip.database.StarRocksDbms instead.")
class StarRocksDbmsHolder private constructor() {
    companion object {
        @JvmField
        val INSTANCE = StarRocksDbms.INSTANCE
    }
}

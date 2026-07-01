package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class StarRocksTableNameIndex : StringStubIndexExtension<StarRocksNamedStubElement>() {
    override fun getKey(): StubIndexKey<String, StarRocksNamedStubElement> = KEY

    override fun getVersion(): Int = VERSION

    companion object {
        const val INDEX_NAME = "starrocks.table.name"
        private const val VERSION = 1

        @JvmField
        val KEY: StubIndexKey<String, StarRocksNamedStubElement> = StubIndexKey.createIndexKey(INDEX_NAME)
    }
}

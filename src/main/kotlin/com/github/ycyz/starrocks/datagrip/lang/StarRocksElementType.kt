package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.psi.tree.IElementType

class StarRocksElementType(debugName: String) : IElementType(debugName, StarRocksDialect.INSTANCE)


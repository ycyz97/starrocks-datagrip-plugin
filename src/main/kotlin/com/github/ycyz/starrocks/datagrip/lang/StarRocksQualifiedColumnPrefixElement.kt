package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.resolve.StarRocksTableAliasReference
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.impl.SqlCompositeElementImpl

class StarRocksQualifiedColumnPrefixElement(type: IElementType) : SqlCompositeElementImpl(type) {
    override fun getReference(): PsiReference {
        return StarRocksTableAliasReference(this)
    }
}

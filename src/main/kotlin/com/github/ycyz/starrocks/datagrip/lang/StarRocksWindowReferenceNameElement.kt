package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.resolve.StarRocksWindowReference
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.impl.SqlCompositeElementImpl

class StarRocksWindowReferenceNameElement(type: IElementType) : SqlCompositeElementImpl(type) {
    override fun getReference(): PsiReference {
        return StarRocksWindowReference(this)
    }
}

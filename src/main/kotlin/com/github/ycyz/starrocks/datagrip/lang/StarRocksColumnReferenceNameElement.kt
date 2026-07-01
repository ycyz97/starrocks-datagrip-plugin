package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.resolve.StarRocksColumnReference
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.impl.SqlCompositeElementImpl

class StarRocksColumnReferenceNameElement(type: IElementType) : SqlCompositeElementImpl(type) {
    override fun getReference(): PsiReference {
        return StarRocksColumnReference(this)
    }
}

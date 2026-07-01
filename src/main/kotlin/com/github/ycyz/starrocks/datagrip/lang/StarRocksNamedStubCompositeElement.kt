package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.CompositeElement

class StarRocksNamedStubCompositeElement(
    type: StarRocksNamedStubElementType
) : CompositeElement(type) {
    override fun createPsiNoLock(): PsiElement {
        return StarRocksNamedStubElement(this)
    }
}

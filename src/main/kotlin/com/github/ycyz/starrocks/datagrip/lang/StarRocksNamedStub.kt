package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.psi.stubs.NamedStub
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class StarRocksNamedStub(
    parent: StubElement<*>?,
    elementType: StarRocksNamedStubElementType,
    private val name: String
) : StubBase<StarRocksNamedStubElement>(parent, elementType), NamedStub<StarRocksNamedStubElement> {
    override fun getName(): String = name
}

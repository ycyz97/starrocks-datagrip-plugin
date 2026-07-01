package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement
import com.intellij.util.IncorrectOperationException

class StarRocksNamedStubElement :
    StubBasedPsiElementBase<StarRocksNamedStub>,
    StubBasedPsiElement<StarRocksNamedStub>,
    PsiNameIdentifierOwner {
    constructor(node: ASTNode) : super(node)

    constructor(stub: StarRocksNamedStub, elementType: StarRocksNamedStubElementType) : super(stub, elementType)

    override fun getName(): String {
        return stub?.name ?: normalizeName(text)
    }

    override fun getNameIdentifier(): PsiElement {
        var child = firstChild
        while (child != null && child.text.isBlank()) {
            child = child.nextSibling
        }
        return child ?: this
    }

    override fun setName(name: String): PsiElement {
        throw IncorrectOperationException("StarRocks rename is not implemented yet.")
    }

    companion object {
        fun normalizeName(text: String?): String {
            val trimmed = text?.trim().orEmpty()
            if (trimmed.isBlank()) {
                return ""
            }
            return StarRocksStubIndexKeys.splitQualifiedIdentifier(trimmed)
                .joinToString(".") { normalizeIdentifierSegment(it) }
        }

        private fun normalizeIdentifierSegment(text: String): String {
            val trimmed = text.trim()
            if (trimmed.length >= 2 && trimmed.first() == '`' && trimmed.last() == '`') {
                return trimmed.substring(1, trimmed.length - 1).replace("``", "`")
            }
            return trimmed
        }
    }
}

package com.github.ycyz.starrocks.datagrip.resolve

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import java.util.Locale

class StarRocksWindowReference(element: PsiElement) :
    PsiReferenceBase<PsiElement>(element, TextRange(0, element.textLength), true) {
    override fun resolve(): PsiElement? {
        val referenceName = StarRocksNamedStubElement.normalizeName(element.text).lowercase(Locale.ROOT)
        if (referenceName.isBlank()) {
            return null
        }
        val queryScope = containingQueryScope(element) ?: element.containingFile ?: return null
        val candidates = mutableListOf<PsiElement>()
        collectWindowNames(queryScope, queryScope, referenceName, candidates)
        return candidates.firstOrNull()
    }

    override fun getVariants(): Array<Any> = emptyArray()

    private fun collectWindowNames(
        root: PsiElement,
        current: PsiElement,
        referenceName: String,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.WINDOW_NAME && matchesName(current, referenceName)) {
            result += current
            return
        }
        current.children.forEach { collectWindowNames(root, it, referenceName, result) }
    }

    private fun containingQueryScope(element: PsiElement): PsiElement? {
        var current: PsiElement? = element
        while (current != null) {
            if (current.node?.elementType in QUERY_SCOPE_TYPES) {
                return current
            }
            current = current.parent
        }
        return null
    }

    private fun matchesName(
        candidate: PsiElement,
        referenceName: String
    ): Boolean {
        return StarRocksNamedStubElement.normalizeName(candidate.text).lowercase(Locale.ROOT) == referenceName
    }

    private companion object {
        private val QUERY_SCOPE_TYPES = setOf(
            StarRocksElementTypes.QUERY_STATEMENT,
            StarRocksElementTypes.DML_STATEMENT,
            StarRocksElementTypes.AS_SELECT_QUERY,
            StarRocksElementTypes.CTE_QUERY,
            StarRocksElementTypes.SUBQUERY_EXPRESSION
        )
    }
}

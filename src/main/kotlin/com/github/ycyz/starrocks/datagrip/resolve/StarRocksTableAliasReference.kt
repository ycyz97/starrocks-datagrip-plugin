package com.github.ycyz.starrocks.datagrip.resolve

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import java.util.Locale
import kotlin.math.abs

class StarRocksTableAliasReference(element: PsiElement) :
    PsiReferenceBase<PsiElement>(element, TextRange(0, element.textLength), true) {
    override fun resolve(): PsiElement? {
        val referenceName = StarRocksNamedStubElement.normalizeName(element.text).lowercase(Locale.ROOT)
        if (referenceName.isBlank()) {
            return null
        }
        val scopeRoot = containingStatement(element) ?: element.containingFile ?: return null
        val candidates = mutableListOf<PsiElement>()
        collectAliases(scopeRoot, referenceName, candidates)
        return candidates.maxWithOrNull(
            compareBy<PsiElement> { commonAncestorDepth(element, it) }
                .thenBy { -abs(it.textRange.startOffset - element.textRange.startOffset) }
        )
    }

    override fun getVariants(): Array<Any> = emptyArray()

    private fun collectAliases(
        element: PsiElement,
        referenceName: String,
        result: MutableList<PsiElement>
    ) {
        if (element.node?.elementType == StarRocksElementTypes.TABLE_ALIAS && matchesAlias(element.text, referenceName)) {
            result += element
        }
        element.children.forEach { collectAliases(it, referenceName, result) }
    }

    private fun containingStatement(element: PsiElement): PsiElement? {
        var current: PsiElement? = element
        while (current != null) {
            if (current.node?.elementType in STATEMENT_TYPES) {
                return current
            }
            current = current.parent
        }
        return null
    }

    private fun matchesAlias(
        candidateName: String,
        referenceName: String
    ): Boolean {
        return StarRocksNamedStubElement.normalizeName(candidateName).lowercase(Locale.ROOT) == referenceName
    }

    private fun commonAncestorDepth(
        left: PsiElement,
        right: PsiElement
    ): Int {
        val leftAncestors = generateSequence(left) { it.parent }.toList().asReversed()
        val rightAncestors = generateSequence(right) { it.parent }.toList().asReversed()
        var depth = 0
        while (
            depth < leftAncestors.size &&
            depth < rightAncestors.size &&
            leftAncestors[depth] == rightAncestors[depth]
        ) {
            depth++
        }
        return depth
    }

    private companion object {
        private val STATEMENT_TYPES = setOf(
            StarRocksElementTypes.QUERY_STATEMENT,
            StarRocksElementTypes.DML_STATEMENT,
            StarRocksElementTypes.TABLE_DDL_STATEMENT,
            StarRocksElementTypes.VIEW_STATEMENT,
            StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT,
            StarRocksElementTypes.CATALOG_STATEMENT,
            StarRocksElementTypes.RESOURCE_STATEMENT,
            StarRocksElementTypes.LOAD_STATEMENT,
            StarRocksElementTypes.ROUTINE_LOAD_STATEMENT,
            StarRocksElementTypes.TASK_STATEMENT,
            StarRocksElementTypes.EXPORT_STATEMENT,
            StarRocksElementTypes.BACKUP_RESTORE_STATEMENT,
            StarRocksElementTypes.ADMIN_STATEMENT,
            StarRocksElementTypes.UNKNOWN_STATEMENT
        )
    }
}

package com.github.ycyz.starrocks.datagrip.resolve

import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStubIndexKeys
import com.github.ycyz.starrocks.datagrip.lang.StarRocksTableNameIndex
import com.intellij.openapi.util.TextRange
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import java.util.Locale

class StarRocksTableReference(element: PsiElement) :
    PsiReferenceBase<PsiElement>(element, TextRange(0, element.textLength), true) {
    override fun resolve(): PsiElement? {
        val referenceName = StarRocksNamedStubElement.normalizeName(element.text)
        if (referenceName.isBlank()) {
            return null
        }
        val containingFile = element.containingFile ?: return null
        val localScope = GlobalSearchScope.fileScope(containingFile)
        return resolveLocalCte(referenceName)
            ?: resolveInScope(referenceName, localScope)
            ?: resolveInScope(referenceName, GlobalSearchScope.projectScope(element.project))
    }

    override fun getVariants(): Array<Any> = emptyArray()

    private fun resolveInScope(
        referenceName: String,
        scope: GlobalSearchScope
    ): StarRocksNamedStubElement? {
        val candidates = lookup(referenceName, scope)
            .filter { matchesTableName(it.name, referenceName) }
        val containingFile = element.containingFile
        val referenceOffset = element.textRange.startOffset
        return candidates
            .filter { it.containingFile == containingFile && it.textRange.startOffset < referenceOffset }
            .maxByOrNull { it.textRange.startOffset }
            ?: candidates.firstOrNull { it.containingFile == containingFile }
            ?: candidates.firstOrNull()
    }

    private fun lookup(
        referenceName: String,
        scope: GlobalSearchScope
    ): List<StarRocksNamedStubElement> {
        val result = linkedSetOf<StarRocksNamedStubElement>()
        StarRocksStubIndexKeys.tableKeys(referenceName).forEach { key ->
            result += StubIndex.getElements(
                StarRocksTableNameIndex.KEY,
                key,
                element.project,
                scope,
                StarRocksNamedStubElement::class.java
            )
        }
        return result.toList()
    }

    private fun resolveLocalCte(referenceName: String): PsiElement? {
        val containingFile = element.containingFile ?: return null
        val referenceOffset = element.textRange.startOffset
        val candidates = mutableListOf<PsiElement>()
        collectCteNames(containingFile.node, candidates)
        return candidates
            .filter { it.textRange.startOffset < referenceOffset && matchesTableName(it.text, referenceName) }
            .maxByOrNull { it.textRange.startOffset }
    }

    private fun collectCteNames(
        node: ASTNode?,
        result: MutableList<PsiElement>
    ) {
        if (node == null) {
            return
        }
        if (node.elementType == StarRocksElementTypes.CTE_NAME) {
            result += node.psi
        }
        node.getChildren(null).forEach { collectCteNames(it, result) }
    }

    private fun matchesTableName(
        candidateName: String,
        referenceName: String
    ): Boolean {
        val normalizedCandidate = StarRocksNamedStubElement.normalizeName(candidateName).lowercase(Locale.ROOT)
        val normalizedReference = StarRocksNamedStubElement.normalizeName(referenceName).lowercase(Locale.ROOT)
        return if ("." in normalizedReference) {
            normalizedCandidate == normalizedReference || normalizedCandidate.endsWith(".$normalizedReference")
        } else {
            normalizedCandidate == normalizedReference || normalizedCandidate.substringAfterLast(".") == normalizedReference
        }
    }
}

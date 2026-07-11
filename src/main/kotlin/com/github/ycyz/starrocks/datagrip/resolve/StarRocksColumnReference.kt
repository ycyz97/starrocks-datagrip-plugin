package com.github.ycyz.starrocks.datagrip.resolve

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.sql.psi.SqlCompositeElementTypes
import java.util.Locale

class StarRocksColumnReference(element: PsiElement) :
    PsiReferenceBase<PsiElement>(element, TextRange(0, element.textLength), true) {
    override fun resolve(): PsiElement? {
        val columnName = StarRocksNamedStubElement.normalizeName(element.text).lowercase(Locale.ROOT)
        if (columnName.isBlank()) {
            return null
        }
        return resolveQualifiedColumn(columnName)
            ?: resolveSelectAlias(columnName)
            ?: resolveUnqualifiedColumn(columnName)
    }

    override fun getVariants(): Array<Any> = emptyArray()

    private fun resolveQualifiedColumn(columnName: String): PsiElement? {
        val alias = resolveQualifierAlias() ?: return null
        val tableReferenceScope = containingElement(alias, SqlCompositeElementTypes.SQL_TABLE_REFERENCE) ?: return null
        val tableReference = nearestTableReferenceBeforeAlias(alias)
        val tableTarget = tableReference?.let(::resolveLocalTableReferenceTarget)
        return resolveColumnOnTableAlias(alias, columnName)
            ?: tableTarget?.let { resolveColumnOnTable(it, columnName) }
            ?: resolveColumnOnDerivedTable(tableReferenceScope, columnName)
    }

    private fun resolveUnqualifiedColumn(columnName: String): PsiElement? {
        val queryScope = containingQueryScope(element) ?: element.containingFile ?: return null
        val tableReferences = mutableListOf<PsiElement>()
        collectTopLevelFromTableReferences(queryScope, queryScope, tableReferences)
        val tableTargets = tableReferences
            .mapNotNull(::resolveLocalTableReferenceTarget)
            .mapNotNull { resolveColumnOnTable(it, columnName) }
        val derivedTableScopes = mutableListOf<PsiElement>()
        collectTopLevelFromTableReferenceScopes(queryScope, queryScope, derivedTableScopes)
        val aliasColumnTargets = derivedTableScopes
            .flatMap(::tableAliasColumns)
            .filter { matchesName(it, columnName) }
        val derivedTargets = derivedTableScopes
            .mapNotNull { resolveColumnOnDerivedTable(it, columnName) }
        return (tableTargets + aliasColumnTargets + derivedTargets)
            .distinctBy { "${it.containingFile?.virtualFile?.path.orEmpty()}:${it.textRange.startOffset}" }
            .singleOrNull()
    }

    private fun resolveSelectAlias(referenceName: String): PsiElement? {
        if (containingElement(element, StarRocksElementTypes.SQL_ORDER_BY_CLAUSE) == null) {
            return null
        }
        val queryScope = containingQueryScope(element) ?: element.containingFile ?: return null
        val candidates = mutableListOf<PsiElement>()
        collectTopLevelSelectAliases(queryScope, queryScope, candidates)
        return candidates.firstOrNull { matchesName(it, referenceName) }
    }

    private fun resolveQualifierAlias(): PsiElement? {
        val qualifier = previousQualifiedColumnPrefix(element) ?: return null
        return qualifier.references.firstOrNull()?.resolve()
    }

    private fun nearestTableReferenceBeforeAlias(alias: PsiElement): PsiElement? {
        val tableReferenceScope = containingElement(alias, SqlCompositeElementTypes.SQL_TABLE_REFERENCE) ?: return null
        val candidates = mutableListOf<PsiElement>()
        collectTableReferencesWithoutNestedQueries(tableReferenceScope, tableReferenceScope, candidates)
        return candidates
            .filter { it.textRange.startOffset < alias.textRange.startOffset }
            .maxByOrNull { it.textRange.startOffset }
    }

    private fun resolveColumnOnTable(tableTarget: PsiElement, columnName: String): PsiElement? {
        if (isCteTarget(tableTarget)) {
            return resolveColumnOnCte(tableTarget, columnName)
        }
        val tableStatement = containingStatement(tableTarget) ?: tableTarget.containingFile ?: return null
        return collectElements(tableStatement, StarRocksElementTypes.COLUMN_NAME)
            .firstOrNull { matchesName(it, columnName) }
    }

    private fun resolveColumnOnCte(cteTarget: PsiElement, columnName: String): PsiElement? {
        val cteDefinition = if (isCteTarget(cteTarget)) {
            cteTarget
        } else {
            containingElement(cteTarget, StarRocksElementTypes.SQL_NAMED_QUERY_DEFINITION)
        } ?: return null
        val explicitColumns = collectCteColumnNames(cteDefinition)
        if (explicitColumns.isNotEmpty()) {
            return explicitColumns.firstOrNull { matchesName(it, columnName) }
        }
        val outputs = mutableListOf<PsiElement>()
        collectDerivedTableSelectOutputs(cteDefinition, cteDefinition, outputs)
        return outputs.firstOrNull { matchesName(it, columnName) }
    }

    private fun resolveColumnOnDerivedTable(tableReferenceScope: PsiElement, columnName: String): PsiElement? {
        tableAliasColumns(tableReferenceScope)
            .firstOrNull { matchesName(it, columnName) }
            ?.let { return it }
        val candidates = mutableListOf<PsiElement>()
        collectDerivedTableSelectOutputs(tableReferenceScope, tableReferenceScope, candidates)
        return candidates.firstOrNull { matchesName(it, columnName) }
    }

    private fun resolveColumnOnTableAlias(alias: PsiElement, columnName: String): PsiElement? =
        immediateTableAliasColumns(alias).firstOrNull { matchesName(it, columnName) }

    private fun previousQualifiedColumnPrefix(element: PsiElement): PsiElement? {
        var current = element.prevSibling
        while (current != null) {
            if (current.node?.elementType == StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX) {
                return current
            }
            if (!current.text.isBlank() && current.text != ".") {
                return null
            }
            current = current.prevSibling
        }
        return null
    }
}

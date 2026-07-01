package com.github.ycyz.starrocks.datagrip.resolve

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.tree.IElementType
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
        val tableReferenceScope = containingElement(alias, StarRocksElementTypes.TABLE_REFERENCE) ?: return null
        val tableReference = nearestTableReferenceBeforeAlias(alias)
        val tableTarget = tableReference?.references?.firstOrNull()?.resolve()
        return tableTarget?.let { resolveColumnOnTable(it, columnName) }
            ?: resolveColumnOnDerivedTable(tableReferenceScope, columnName)
    }

    private fun resolveUnqualifiedColumn(columnName: String): PsiElement? {
        val queryScope = containingQueryScope(element) ?: element.containingFile ?: return null
        val tableReferences = mutableListOf<PsiElement>()
        collectTopLevelFromTableReferences(queryScope, queryScope, tableReferences)
        val tableTargets = tableReferences
            .mapNotNull { it.references.firstOrNull()?.resolve() }
            .mapNotNull { resolveColumnOnTable(it, columnName) }
        val derivedTableScopes = mutableListOf<PsiElement>()
        collectTopLevelFromTableReferenceScopes(queryScope, queryScope, derivedTableScopes)
        val derivedTargets = derivedTableScopes
            .mapNotNull { resolveColumnOnDerivedTable(it, columnName) }
        val targets = (tableTargets + derivedTargets)
            .distinctBy { "${it.containingFile?.virtualFile?.path.orEmpty()}:${it.textRange.startOffset}" }
        return targets.singleOrNull()
    }

    private fun resolveSelectAlias(referenceName: String): PsiElement? {
        if (containingElement(element, StarRocksElementTypes.ORDER_BY_CLAUSE) == null) {
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
        val tableReferenceScope = containingElement(alias, StarRocksElementTypes.TABLE_REFERENCE) ?: return null
        val candidates = mutableListOf<PsiElement>()
        collectTableReferencesWithoutNestedQueries(tableReferenceScope, tableReferenceScope, candidates)
        return candidates
            .filter { it.textRange.startOffset < alias.textRange.startOffset }
            .maxByOrNull { it.textRange.startOffset }
    }

    private fun resolveColumnOnTable(
        tableTarget: PsiElement,
        columnName: String
    ): PsiElement? {
        val tableStatement = containingStatement(tableTarget) ?: tableTarget.containingFile ?: return null
        val candidates = mutableListOf<PsiElement>()
        collectElements(tableStatement, StarRocksElementTypes.COLUMN_NAME, candidates)
        return candidates.firstOrNull { matchesName(it, columnName) }
    }

    private fun resolveColumnOnDerivedTable(
        tableReferenceScope: PsiElement,
        columnName: String
    ): PsiElement? {
        val candidates = mutableListOf<PsiElement>()
        collectDerivedTableSelectOutputs(tableReferenceScope, tableReferenceScope, candidates)
        return candidates.firstOrNull { matchesName(it, columnName) }
    }

    private fun previousQualifiedColumnPrefix(element: PsiElement): PsiElement? {
        var current = element.prevSibling
        while (current != null) {
            val type = current.node?.elementType
            if (type == StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX) {
                return current
            }
            if (!current.text.isBlank() && current.text != ".") {
                return null
            }
            current = current.prevSibling
        }
        return null
    }

    private fun containingElement(
        element: PsiElement,
        elementType: IElementType
    ): PsiElement? {
        var current: PsiElement? = element
        while (current != null) {
            if (current.node?.elementType == elementType) {
                return current
            }
            current = current.parent
        }
        return null
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

    private fun collectTopLevelFromTableReferences(
        root: PsiElement,
        current: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.FROM_CLAUSE) {
            collectTableReferencesWithoutNestedQueries(current, current, result)
            return
        }
        current.children.forEach { collectTopLevelFromTableReferences(root, it, result) }
    }

    private fun collectTopLevelFromTableReferenceScopes(
        root: PsiElement,
        current: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.FROM_CLAUSE) {
            collectTableReferenceScopesWithoutNestedQueries(current, current, result)
            return
        }
        current.children.forEach { collectTopLevelFromTableReferenceScopes(root, it, result) }
    }

    private fun collectTopLevelSelectAliases(
        root: PsiElement,
        current: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.SELECT_CLAUSE) {
            collectSelectAliasesWithoutNestedQueries(current, current, result)
            return
        }
        current.children.forEach { collectTopLevelSelectAliases(root, it, result) }
    }

    private fun collectSelectAliasesWithoutNestedQueries(
        root: PsiElement,
        current: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.SELECT_ALIAS) {
            result += current
        }
        current.children.forEach { collectSelectAliasesWithoutNestedQueries(root, it, result) }
    }

    private fun collectTableReferencesWithoutNestedQueries(
        root: PsiElement,
        current: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.TABLE_REFERENCE_NAME) {
            result += current
        }
        current.children.forEach { collectTableReferencesWithoutNestedQueries(root, it, result) }
    }

    private fun collectTableReferenceScopesWithoutNestedQueries(
        root: PsiElement,
        current: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.TABLE_REFERENCE) {
            result += current
            return
        }
        current.children.forEach { collectTableReferenceScopesWithoutNestedQueries(root, it, result) }
    }

    private fun collectDerivedTableSelectOutputs(
        root: PsiElement,
        current: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES && current.parent != root) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.SELECT_CLAUSE) {
            collectSelectOutputsWithoutNestedQueries(current, current, result)
            return
        }
        current.children.forEach { collectDerivedTableSelectOutputs(root, it, result) }
    }

    private fun collectSelectOutputsWithoutNestedQueries(
        root: PsiElement,
        current: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.SELECT_ITEM) {
            selectOutputElement(current)?.let(result::add)
            return
        }
        current.children.forEach { collectSelectOutputsWithoutNestedQueries(root, it, result) }
    }

    private fun selectOutputElement(selectItem: PsiElement): PsiElement? {
        val aliases = mutableListOf<PsiElement>()
        collectSelectAliasesWithoutNestedQueries(selectItem, selectItem, aliases)
        if (aliases.isNotEmpty()) {
            return aliases.last()
        }
        val columnReferences = mutableListOf<PsiElement>()
        collectColumnReferencesWithoutNestedQueries(selectItem, selectItem, columnReferences)
        return columnReferences.singleOrNull()
    }

    private fun collectColumnReferencesWithoutNestedQueries(
        root: PsiElement,
        current: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.COLUMN_REFERENCE_NAME) {
            result += current
        }
        current.children.forEach { collectColumnReferencesWithoutNestedQueries(root, it, result) }
    }

    private fun collectElements(
        element: PsiElement,
        elementType: IElementType,
        result: MutableList<PsiElement>
    ) {
        if (element.node?.elementType == elementType) {
            result += element
        }
        element.children.forEach { collectElements(it, elementType, result) }
    }

    private fun matchesName(
        candidate: PsiElement,
        referenceName: String
    ): Boolean {
        val candidateName = when (candidate) {
            is StarRocksNamedStubElement -> candidate.name
            else -> candidate.text
        }
        return StarRocksNamedStubElement.normalizeName(candidateName).lowercase(Locale.ROOT) == referenceName
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
        private val QUERY_SCOPE_TYPES = setOf(
            StarRocksElementTypes.QUERY_STATEMENT,
            StarRocksElementTypes.DML_STATEMENT,
            StarRocksElementTypes.AS_SELECT_QUERY,
            StarRocksElementTypes.CTE_QUERY,
            StarRocksElementTypes.SUBQUERY_EXPRESSION
        )
    }
}

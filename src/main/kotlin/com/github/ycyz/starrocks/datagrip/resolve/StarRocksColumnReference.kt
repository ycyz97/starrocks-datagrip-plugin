package com.github.ycyz.starrocks.datagrip.resolve

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStatementElementSets
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlDefinition
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
        val tableTarget = tableReference?.let { resolveLocalTableReferenceTarget(it) }
        return resolveColumnOnTableAlias(alias, columnName)
            ?: tableTarget?.let { resolveColumnOnTable(it, columnName) }
            ?: resolveColumnOnDerivedTable(tableReferenceScope, columnName)
    }

    private fun resolveUnqualifiedColumn(columnName: String): PsiElement? {
        val queryScope = containingQueryScope(element) ?: element.containingFile ?: return null
        val tableReferences = mutableListOf<PsiElement>()
        collectTopLevelFromTableReferences(queryScope, queryScope, tableReferences)
        val tableTargets = tableReferences
            .mapNotNull { resolveLocalTableReferenceTarget(it) }
            .mapNotNull { resolveColumnOnTable(it, columnName) }
        val derivedTableScopes = mutableListOf<PsiElement>()
        collectTopLevelFromTableReferenceScopes(queryScope, queryScope, derivedTableScopes)
        val aliasColumnTargets = derivedTableScopes
            .flatMap { tableReferenceScope -> tableAliasColumns(tableReferenceScope) }
            .filter { matchesName(it, columnName) }
        val derivedTargets = derivedTableScopes
            .mapNotNull { resolveColumnOnDerivedTable(it, columnName) }
        val targets = (tableTargets + aliasColumnTargets + derivedTargets)
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
        if (isCteTarget(tableTarget)) {
            return resolveColumnOnCte(tableTarget, columnName)
        }
        val tableStatement = containingStatement(tableTarget) ?: tableTarget.containingFile ?: return null
        val candidates = mutableListOf<PsiElement>()
        collectElements(tableStatement, StarRocksElementTypes.COLUMN_NAME, candidates)
        return candidates.firstOrNull { matchesName(it, columnName) }
    }

    private fun resolveLocalTableReferenceTarget(tableReference: PsiElement): PsiElement? {
        val referenceName = StarRocksNamedStubElement.normalizeName(tableReference.text)
        if (referenceName.isBlank()) {
            return null
        }
        val containingFile = tableReference.containingFile ?: return null
        tableReference.reference
            ?.resolve()
            ?.takeIf { it.containingFile == containingFile }
            ?.let { return it }
        val candidates = mutableListOf<PsiElement>()
        collectElements(containingFile, StarRocksElementTypes.CTE_DEFINITION, candidates)
        collectLocalTableTargets(containingFile, candidates)
        val referenceOffset = tableReference.textRange.startOffset
        return candidates
            .filter { it.textRange.startOffset < referenceOffset && matchesTableName(it, referenceName) }
            .maxByOrNull { it.textRange.startOffset }
    }

    private fun resolveColumnOnCte(
        cteTarget: PsiElement,
        columnName: String
    ): PsiElement? {
        val cteDefinition = if (cteTarget.node?.elementType == StarRocksElementTypes.CTE_DEFINITION) {
            cteTarget
        } else {
            containingElement(cteTarget, StarRocksElementTypes.CTE_DEFINITION)
        } ?: return null
        val explicitColumns = mutableListOf<PsiElement>()
        collectCteColumnNames(cteDefinition, cteDefinition, explicitColumns)
        if (explicitColumns.isNotEmpty()) {
            return explicitColumns.firstOrNull { matchesName(it, columnName) }
        }
        val outputs = mutableListOf<PsiElement>()
        collectDerivedTableSelectOutputs(cteDefinition, cteDefinition, outputs)
        return outputs.firstOrNull { matchesName(it, columnName) }
    }

    private fun isCteTarget(element: PsiElement): Boolean {
        val type = element.node?.elementType
        return type == StarRocksElementTypes.CTE_DEFINITION
    }

    private fun resolveColumnOnDerivedTable(
        tableReferenceScope: PsiElement,
        columnName: String
    ): PsiElement? {
        tableAliasColumns(tableReferenceScope)
            .firstOrNull { matchesName(it, columnName) }
            ?.let { return it }
        val candidates = mutableListOf<PsiElement>()
        collectDerivedTableSelectOutputs(tableReferenceScope, tableReferenceScope, candidates)
        return candidates.firstOrNull { matchesName(it, columnName) }
    }

    private fun resolveColumnOnTableAlias(
        alias: PsiElement,
        columnName: String
    ): PsiElement? {
        return immediateTableAliasColumns(alias)
            .firstOrNull { matchesName(it, columnName) }
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
        if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES && !isPrimaryQueryScope(root, current)) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.SELECT_CLAUSE) {
            collectSelectOutputsWithoutNestedQueries(current, current, result)
            return
        }
        current.children.forEach { collectDerivedTableSelectOutputs(root, it, result) }
    }

    private fun isPrimaryQueryScope(root: PsiElement, current: PsiElement): Boolean {
        val type = current.node?.elementType
        if (type == StarRocksElementTypes.CTE_QUERY || type == StarRocksElementTypes.SUBQUERY_EXPRESSION) {
            return current.parent == root
        }
        if (type == SqlCompositeElementTypes.SQL_SELECT_STATEMENT) {
            val parentType = current.parent?.node?.elementType
            return parentType == StarRocksElementTypes.CTE_QUERY || parentType == StarRocksElementTypes.SUBQUERY_EXPRESSION
        }
        return current.parent == root
    }

    private fun tableAliasColumns(tableReferenceScope: PsiElement): List<PsiElement> {
        val columns = mutableListOf<PsiElement>()
        collectElements(tableReferenceScope, StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME, columns)
        return columns
    }

    private fun immediateTableAliasColumns(alias: PsiElement): List<PsiElement> {
        var current = alias.nextSibling
        while (current != null && current.text.isBlank()) {
            current = current.nextSibling
        }
        if (current?.node?.elementType != StarRocksElementTypes.TABLE_ALIAS_COLUMN_LIST) {
            return emptyList()
        }
        val columns = mutableListOf<PsiElement>()
        collectElements(current, StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME, columns)
        return columns
    }

    private fun collectCteColumnNames(
        root: PsiElement,
        current: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (current != root && current.node?.elementType == StarRocksElementTypes.CTE_QUERY) {
            return
        }
        if (current.node?.elementType == StarRocksElementTypes.CTE_COLUMN_NAME) {
            result += current
        }
        current.children.forEach { collectCteColumnNames(root, it, result) }
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

    private fun collectLocalTableTargets(
        element: PsiElement,
        result: MutableList<PsiElement>
    ) {
        if (element is SqlDefinition && element.node?.elementType in LOCAL_TABLE_DEFINITION_TYPES) {
            result += element
        }
        element.children.forEach { collectLocalTableTargets(it, result) }
    }

    private fun matchesName(
        candidate: PsiElement,
        referenceName: String
    ): Boolean {
        val candidateName = when (candidate) {
            is SqlDefinition -> sqlDefinitionName(candidate)
            is StarRocksNamedStubElement -> candidate.name
            else -> candidate.text
        }
        return StarRocksNamedStubElement.normalizeName(candidateName).lowercase(Locale.ROOT) == referenceName
    }

    private fun matchesTableName(
        candidate: PsiElement,
        referenceName: String
    ): Boolean {
        val candidateName = when (candidate) {
            is SqlDefinition -> sqlDefinitionName(candidate)
            is StarRocksNamedStubElement -> candidate.name
            else -> candidate.text
        }
        val normalizedCandidate = StarRocksNamedStubElement.normalizeName(candidateName).lowercase(Locale.ROOT)
        val normalizedReference = StarRocksNamedStubElement.normalizeName(referenceName).lowercase(Locale.ROOT)
        return if ("." in normalizedReference) {
            normalizedCandidate == normalizedReference || normalizedCandidate.endsWith(".$normalizedReference")
        } else {
            normalizedCandidate == normalizedReference || normalizedCandidate.substringAfterLast(".") == normalizedReference
        }
    }

    private fun sqlDefinitionName(definition: SqlDefinition): String {
        val nameElementText = definition.nameElement?.text
        val normalizedNameElement = StarRocksNamedStubElement.normalizeName(nameElementText.orEmpty())
        return normalizedNameElement.takeIf { it.isNotBlank() } ?: definition.name.orEmpty()
    }

    private companion object {
        private val LOCAL_TABLE_DEFINITION_TYPES = setOf(
            SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT
        )

        private val STATEMENT_TYPES = StarRocksStatementElementSets.STATEMENT_TYPES
        private val QUERY_SCOPE_TYPES = StarRocksStatementElementSets.QUERY_SCOPE_TYPES
    }
}

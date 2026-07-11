package com.github.ycyz.starrocks.datagrip.resolve

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStatementElementSets
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlDefinition
import java.util.Locale

internal fun containingElement(element: PsiElement, elementType: IElementType): PsiElement? {
    var current: PsiElement? = element
    while (current != null) {
        if (current.node?.elementType == elementType) {
            return current
        }
        current = current.parent
    }
    return null
}

internal fun containingStatement(element: PsiElement): PsiElement? {
    var current: PsiElement? = element
    while (current != null) {
        if (current.node?.elementType in STATEMENT_TYPES) {
            return current
        }
        current = current.parent
    }
    return null
}

internal fun containingQueryScope(element: PsiElement): PsiElement? {
    var current: PsiElement? = element
    while (current != null) {
        if (current.node?.elementType in QUERY_SCOPE_TYPES) {
            return current
        }
        current = current.parent
    }
    return null
}

internal fun collectTopLevelFromTableReferences(
    root: PsiElement,
    current: PsiElement,
    result: MutableList<PsiElement>
) {
    if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
        return
    }
    if (current.node?.elementType == StarRocksElementTypes.SQL_FROM_CLAUSE) {
        collectTableReferencesWithoutNestedQueries(current, current, result)
        return
    }
    current.children.forEach { collectTopLevelFromTableReferences(root, it, result) }
}

internal fun collectTableReferencesWithoutNestedQueries(
    root: PsiElement,
    current: PsiElement,
    result: MutableList<PsiElement>
) {
    if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
        return
    }
    if (current.node?.elementType == SqlCompositeElementTypes.SQL_TABLE_REFERENCE) {
        result += current
    }
    current.children.forEach { collectTableReferencesWithoutNestedQueries(root, it, result) }
}

internal fun collectTopLevelFromTableReferenceScopes(
    root: PsiElement,
    current: PsiElement,
    result: MutableList<PsiElement>
) {
    if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
        return
    }
    if (current.node?.elementType == StarRocksElementTypes.SQL_FROM_CLAUSE) {
        collectTableReferenceScopesWithoutNestedQueries(current, current, result)
        return
    }
    current.children.forEach { collectTopLevelFromTableReferenceScopes(root, it, result) }
}

internal fun collectTableReferenceScopesWithoutNestedQueries(
    root: PsiElement,
    current: PsiElement,
    result: MutableList<PsiElement>
) {
    if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
        return
    }
    if (current.node?.elementType == SqlCompositeElementTypes.SQL_TABLE_REFERENCE) {
        result += current
        return
    }
    current.children.forEach { collectTableReferenceScopesWithoutNestedQueries(root, it, result) }
}

internal fun collectTopLevelSelectAliases(
    root: PsiElement,
    current: PsiElement,
    result: MutableList<PsiElement>
) {
    if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES) {
        return
    }
    if (current.node?.elementType == StarRocksElementTypes.SQL_SELECT_CLAUSE) {
        collectSelectAliasesWithoutNestedQueries(current, current, result)
        return
    }
    current.children.forEach { collectTopLevelSelectAliases(root, it, result) }
}

internal fun collectSelectAliasesWithoutNestedQueries(
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

internal fun collectDerivedTableSelectOutputs(
    root: PsiElement,
    current: PsiElement,
    result: MutableList<PsiElement>
) {
    if (current != root && current.node?.elementType in QUERY_SCOPE_TYPES && !isPrimaryQueryScope(root, current)) {
        return
    }
    if (current.node?.elementType == StarRocksElementTypes.SQL_SELECT_CLAUSE) {
        collectSelectOutputsWithoutNestedQueries(current, current, result)
        return
    }
    current.children.forEach { collectDerivedTableSelectOutputs(root, it, result) }
}

private fun isPrimaryQueryScope(root: PsiElement, current: PsiElement): Boolean {
    val type = current.node?.elementType
    if (type == StarRocksElementTypes.SQL_PARENTHESIZED_QUERY_EXPRESSION) {
        return current.parent == root
    }
    if (type == StarRocksElementTypes.SQL_SELECT_STATEMENT) {
        return current.parent?.node?.elementType == StarRocksElementTypes.SQL_PARENTHESIZED_QUERY_EXPRESSION
    }
    return current.parent == root
}

internal fun tableAliasColumns(tableReferenceScope: PsiElement): List<PsiElement> =
    collectElements(tableReferenceScope, StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME)

internal fun immediateTableAliasColumns(alias: PsiElement): List<PsiElement> {
    var current = alias.nextSibling
    while (current != null && current.text.isBlank()) {
        current = current.nextSibling
    }
    if (current?.node?.elementType != StarRocksElementTypes.TABLE_ALIAS_COLUMN_LIST) {
        return emptyList()
    }
    return collectElements(current, StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME)
}

internal fun collectCteColumnNames(root: PsiElement): List<PsiElement> {
    val result = mutableListOf<PsiElement>()
    collectCteColumnNames(root, root, result)
    return result
}

internal fun collectCteColumnNames(
    root: PsiElement,
    current: PsiElement,
    result: MutableList<PsiElement>
) {
    if (current != root && current.node?.elementType == StarRocksElementTypes.SQL_PARENTHESIZED_QUERY_EXPRESSION) {
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

internal fun collectElements(file: PsiFile, elementType: IElementType): List<PsiElement> =
    collectElements(file as PsiElement, elementType)

internal fun collectElements(element: PsiElement, elementType: IElementType): List<PsiElement> {
    val result = mutableListOf<PsiElement>()
    collectElements(element, elementType, result)
    return result
}

internal fun collectElements(
    element: PsiElement,
    elementType: IElementType,
    result: MutableList<PsiElement>
) {
    if (element.node?.elementType == elementType) {
        result += element
    }
    element.children.forEach { collectElements(it, elementType, result) }
}

internal fun collectLocalTableTargets(element: PsiElement): List<PsiElement> {
    val result = mutableListOf<PsiElement>()
    collectLocalTableTargets(element, result)
    return result
}

internal fun collectLocalTableTargets(element: PsiElement, result: MutableList<PsiElement>) {
    if (element is SqlDefinition && element.node?.elementType in LOCAL_TABLE_DEFINITION_TYPES) {
        result += element
    }
    element.children.forEach { collectLocalTableTargets(it, result) }
}

internal fun normalizedName(element: PsiElement): String = when (element) {
    is StarRocksNamedStubElement -> element.name
    is SqlDefinition -> sqlDefinitionName(element)
    else -> StarRocksNamedStubElement.normalizeName(element.text)
}

internal fun matchesName(candidate: PsiElement, referenceName: String): Boolean =
    normalizedName(candidate).lowercase(Locale.ROOT) == referenceName

internal fun matchesTableName(candidate: PsiElement, referenceName: String): Boolean {
    val normalizedCandidate = normalizedName(candidate).lowercase(Locale.ROOT)
    val normalizedReference = StarRocksNamedStubElement.normalizeName(referenceName).lowercase(Locale.ROOT)
    return if ("." in normalizedReference) {
        normalizedCandidate == normalizedReference || normalizedCandidate.endsWith(".$normalizedReference")
    } else {
        normalizedCandidate == normalizedReference || normalizedCandidate.substringAfterLast(".") == normalizedReference
    }
}

internal fun resolveLocalTableReferenceTarget(tableReference: PsiElement): PsiElement? {
    val referenceName = normalizedName(tableReference)
    if (referenceName.isBlank()) {
        return null
    }
    val containingFile = tableReference.containingFile ?: return null
    tableReference.reference
        ?.resolve()
        ?.takeIf { it.containingFile == containingFile }
        ?.let { return it }
    val candidates = collectElements(containingFile, StarRocksElementTypes.SQL_NAMED_QUERY_DEFINITION) +
        collectLocalTableTargets(containingFile)
    val referenceOffset = tableReference.textRange.startOffset
    return candidates
        .filter { it.textRange.startOffset < referenceOffset && matchesTableName(it, referenceName) }
        .maxByOrNull { it.textRange.startOffset }
}

internal fun isCteTarget(element: PsiElement): Boolean =
    element.node?.elementType == StarRocksElementTypes.SQL_NAMED_QUERY_DEFINITION

private fun sqlDefinitionName(definition: SqlDefinition): String {
    val normalizedNameElement = StarRocksNamedStubElement.normalizeName(definition.nameElement?.text.orEmpty())
    return normalizedNameElement.takeIf { it.isNotBlank() } ?: definition.name.orEmpty()
}

private val LOCAL_TABLE_DEFINITION_TYPES = setOf(
    StarRocksElementTypes.SQL_CREATE_TABLE_STATEMENT,
    StarRocksElementTypes.SQL_CREATE_VIEW_STATEMENT,
    StarRocksElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT
)

private val STATEMENT_TYPES = StarRocksStatementElementSets.STATEMENT_TYPES
private val QUERY_SCOPE_TYPES = StarRocksStatementElementSets.QUERY_SCOPE_TYPES

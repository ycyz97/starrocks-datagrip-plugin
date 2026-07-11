package com.github.ycyz.starrocks.datagrip.completion

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.resolve.collectCteColumnNames
import com.github.ycyz.starrocks.datagrip.resolve.collectDerivedTableSelectOutputs
import com.github.ycyz.starrocks.datagrip.resolve.collectElements
import com.github.ycyz.starrocks.datagrip.resolve.collectLocalTableTargets
import com.github.ycyz.starrocks.datagrip.resolve.collectTopLevelFromTableReferenceScopes
import com.github.ycyz.starrocks.datagrip.resolve.collectTopLevelFromTableReferences
import com.github.ycyz.starrocks.datagrip.resolve.collectTopLevelSelectAliases
import com.github.ycyz.starrocks.datagrip.resolve.containingElement
import com.github.ycyz.starrocks.datagrip.resolve.containingQueryScope
import com.github.ycyz.starrocks.datagrip.resolve.containingStatement
import com.github.ycyz.starrocks.datagrip.resolve.isCteTarget
import com.github.ycyz.starrocks.datagrip.resolve.normalizedName
import com.github.ycyz.starrocks.datagrip.resolve.resolveLocalTableReferenceTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

internal object StarRocksCompletionScope {
    fun tableNames(file: PsiFile): Set<String> = buildSet {
        collectLocalTableTargets(file).forEach { tableName ->
            val normalized = normalizedName(tableName)
            if (normalized.isNotBlank()) {
                add(normalized)
                normalized.substringAfterLast(".").takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }

    fun columnNames(position: PsiElement, file: PsiFile): Set<String> {
        val queryScope = containingQueryScope(position) ?: file
        val tableReferences = mutableListOf<PsiElement>()
        collectTopLevelFromTableReferences(queryScope, queryScope, tableReferences)
        val tableColumns = tableReferences
            .mapNotNull(::resolveLocalTableReferenceTarget)
            .flatMap(::columnNamesForTable)
        val derivedTableScopes = mutableListOf<PsiElement>()
        collectTopLevelFromTableReferenceScopes(queryScope, queryScope, derivedTableScopes)
        val aliasColumns = derivedTableScopes.flatMap(::tableAliasColumnNames)
        val derivedColumns = derivedTableScopes.flatMap(::derivedTableColumnNames)
        return (tableColumns + aliasColumns + derivedColumns).toCollection(linkedSetOf())
    }

    fun selectAliasNames(position: PsiElement, file: PsiFile): Set<String> {
        val queryScope = containingQueryScope(position) ?: file
        val aliases = mutableListOf<PsiElement>()
        collectTopLevelSelectAliases(queryScope, queryScope, aliases)
        return aliases.mapNotNullTo(linkedSetOf()) { alias ->
            normalizedName(alias).takeIf { it.isNotBlank() }
        }
    }

    private fun columnNamesForTable(tableTarget: PsiElement): Set<String> {
        if (isCteTarget(tableTarget)) {
            return columnNamesForCte(tableTarget)
        }
        val tableStatement = containingStatement(tableTarget) ?: tableTarget.containingFile ?: return emptySet()
        return collectElements(tableStatement, StarRocksElementTypes.COLUMN_NAME)
            .mapNotNullTo(linkedSetOf()) { normalizedName(it).takeIf(String::isNotBlank) }
    }

    private fun columnNamesForCte(cteTarget: PsiElement): Set<String> {
        val cteDefinition = if (isCteTarget(cteTarget)) {
            cteTarget
        } else {
            containingElement(cteTarget, StarRocksElementTypes.SQL_NAMED_QUERY_DEFINITION)
        } ?: return emptySet()
        val explicitColumns = collectCteColumnNames(cteDefinition)
            .mapNotNullTo(linkedSetOf()) { normalizedName(it).takeIf(String::isNotBlank) }
        if (explicitColumns.isNotEmpty()) {
            return explicitColumns
        }
        val outputs = mutableListOf<PsiElement>()
        collectDerivedTableSelectOutputs(cteDefinition, cteDefinition, outputs)
        return outputs.mapNotNullTo(linkedSetOf()) { normalizedName(it).takeIf(String::isNotBlank) }
    }

    private fun derivedTableColumnNames(tableReferenceScope: PsiElement): Set<String> {
        val aliasColumns = tableAliasColumnNames(tableReferenceScope)
        if (aliasColumns.isNotEmpty()) {
            return aliasColumns
        }
        val outputs = mutableListOf<PsiElement>()
        collectDerivedTableSelectOutputs(tableReferenceScope, tableReferenceScope, outputs)
        return outputs.mapNotNullTo(linkedSetOf()) { normalizedName(it).takeIf(String::isNotBlank) }
    }

    private fun tableAliasColumnNames(tableReferenceScope: PsiElement): Set<String> =
        collectElements(tableReferenceScope, StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME)
            .mapNotNullTo(linkedSetOf()) { normalizedName(it).takeIf(String::isNotBlank) }
}

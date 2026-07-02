package com.github.ycyz.starrocks.datagrip.completion

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStatementElementSets
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlDefinition
import com.intellij.util.ProcessingContext
import java.util.Locale

class StarRocksCompletionContributor : CompletionContributor(), DumbAware {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(StarRocksDialect.INSTANCE),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val completionResult = result.caseInsensitive()
                    if (isTableCompletionContext(parameters)) {
                        addTableCompletions(parameters, completionResult)
                    }
                    if (isColumnCompletionContext(parameters)) {
                        addColumnCompletions(parameters, completionResult)
                    }
                    if (isOrderByCompletionContext(parameters)) {
                        addSelectAliasCompletions(parameters, completionResult)
                    }
                    addKeywordCompletions(completionResult)
                    addTypeCompletions(completionResult)
                    addFunctionCompletions(completionResult)
                    if (isPropertyCompletionContext(parameters)) {
                        addPropertyCompletions(completionResult)
                    }
                    addSnippetCompletions(completionResult)
                }
            }
        )
    }

    private fun addTableCompletions(
        parameters: CompletionParameters,
        result: CompletionResultSet
    ) {
        val tableNames = linkedSetOf<String>()
        collectLocalTableTargets(parameters.originalFile)
            .forEach { tableName ->
                val normalizedName = normalizedName(tableName)
                if (normalizedName.isBlank()) {
                    return@forEach
                }
                tableNames += normalizedName
                val shortName = normalizedName.substringAfterLast(".")
                if (shortName.isNotBlank()) {
                    tableNames += shortName
                }
            }
        tableNames.forEach { tableName ->
            result.addElement(
                LookupElementBuilder.create(tableName)
                    .withTypeText("table", true)
            )
        }
    }

    private fun addColumnCompletions(
        parameters: CompletionParameters,
        result: CompletionResultSet
    ) {
        visibleColumnNames(parameters)
            .forEach { columnName ->
                result.addElement(
                    LookupElementBuilder.create(columnName)
                        .withTypeText("column", true)
                )
            }
    }

    private fun addSelectAliasCompletions(
        parameters: CompletionParameters,
        result: CompletionResultSet
    ) {
        visibleSelectAliasNames(parameters)
            .forEach { aliasName ->
                result.addElement(
                    LookupElementBuilder.create(aliasName)
                        .withTypeText("alias", true)
                )
            }
    }

    private fun addKeywordCompletions(result: CompletionResultSet) {
        StarRocksCompletionCatalog.KEYWORDS.forEach { keyword ->
            result.addElement(
                LookupElementBuilder.create(keyword)
                    .bold()
                    .withTypeText("keyword", true)
            )
        }
    }

    private fun addTypeCompletions(result: CompletionResultSet) {
        StarRocksCompletionCatalog.DATA_TYPES.forEach { type ->
            result.addElement(
                LookupElementBuilder.create(type)
                    .withTypeText("type", true)
            )
        }
    }

    private fun addFunctionCompletions(result: CompletionResultSet) {
        StarRocksCompletionCatalog.FUNCTIONS.forEach { function ->
            result.addElement(
                LookupElementBuilder.create(function.lowercase())
                    .withLookupString(function)
                    .withTailText("()", true)
                    .withTypeText("function", true)
            )
        }
    }

    private fun addPropertyCompletions(result: CompletionResultSet) {
        StarRocksCompletionCatalog.PROPERTIES.forEach { property ->
            result.addElement(
                LookupElementBuilder.create(property)
                    .withTypeText("property", true)
            )
        }
    }

    private fun addSnippetCompletions(result: CompletionResultSet) {
        StarRocksCompletionCatalog.SNIPPETS.forEach { snippet ->
            result.addElement(
                LookupElementBuilder.create(snippet.lookup)
                    .withTypeText("snippet", true)
                    .withInsertHandler(snippetInsertHandler(snippet.insertText))
            )
        }
    }

    private fun visibleColumnNames(parameters: CompletionParameters): Set<String> {
        val queryScope = containingQueryScope(parameters.position) ?: parameters.originalFile
        val tableReferences = mutableListOf<PsiElement>()
        collectTopLevelFromTableReferences(queryScope, queryScope, tableReferences)
        val tableColumns = tableReferences
            .mapNotNull { resolveLocalTableReferenceTarget(it) }
            .flatMap { tableTarget -> columnNamesForTable(tableTarget) }
        val derivedTableScopes = mutableListOf<PsiElement>()
        collectTopLevelFromTableReferenceScopes(queryScope, queryScope, derivedTableScopes)
        val aliasColumns = derivedTableScopes.flatMap { tableAliasColumnNames(it) }
        val derivedColumns = derivedTableScopes.flatMap { derivedTableColumnNames(it) }
        return (tableColumns + aliasColumns + derivedColumns).toCollection(linkedSetOf())
    }

    private fun visibleSelectAliasNames(parameters: CompletionParameters): Set<String> {
        val queryScope = containingQueryScope(parameters.position) ?: parameters.originalFile
        val aliases = mutableListOf<PsiElement>()
        collectTopLevelSelectAliases(queryScope, queryScope, aliases)
        return aliases
            .mapNotNullTo(linkedSetOf()) { alias ->
                normalizedName(alias).takeIf { it.isNotBlank() }
            }
    }

    private fun columnNamesForTable(tableTarget: PsiElement): Set<String> {
        if (isCteTarget(tableTarget)) {
            return columnNamesForCte(tableTarget)
        }
        val tableStatement = containingStatement(tableTarget) ?: tableTarget.containingFile ?: return emptySet()
        return collectElements(tableStatement, StarRocksElementTypes.COLUMN_NAME)
            .mapNotNullTo(linkedSetOf()) { column ->
                normalizedName(column).takeIf { it.isNotBlank() }
            }
    }

    private fun columnNamesForCte(cteTarget: PsiElement): Set<String> {
        val cteDefinition = if (cteTarget.node?.elementType == StarRocksElementTypes.CTE_DEFINITION) {
            cteTarget
        } else {
            containingElement(cteTarget, StarRocksElementTypes.CTE_DEFINITION)
        } ?: return emptySet()
        val explicitColumns = collectCteColumnNames(cteDefinition)
            .mapNotNullTo(linkedSetOf()) { column ->
                normalizedName(column).takeIf { it.isNotBlank() }
            }
        if (explicitColumns.isNotEmpty()) {
            return explicitColumns
        }
        val outputs = mutableListOf<PsiElement>()
        collectDerivedTableSelectOutputs(cteDefinition, cteDefinition, outputs)
        return outputs.mapNotNullTo(linkedSetOf()) { output ->
            normalizedName(output).takeIf { it.isNotBlank() }
        }
    }

    private fun resolveLocalTableReferenceTarget(tableReference: PsiElement): PsiElement? {
        val referenceName = normalizedName(tableReference)
        if (referenceName.isBlank()) {
            return null
        }
        val containingFile = tableReference.containingFile ?: return null
        tableReference.reference
            ?.resolve()
            ?.takeIf { it.containingFile == containingFile }
            ?.let { return it }
        val candidates = collectElements(containingFile, StarRocksElementTypes.CTE_DEFINITION) +
            collectLocalTableTargets(containingFile)
        val referenceOffset = tableReference.textRange.startOffset
        return candidates
            .filter { it.textRange.startOffset < referenceOffset && matchesTableName(it, referenceName) }
            .maxByOrNull { it.textRange.startOffset }
    }

    private fun isCteTarget(element: PsiElement): Boolean {
        val type = element.node?.elementType
        return type == StarRocksElementTypes.CTE_DEFINITION
    }

    private fun isTableCompletionContext(parameters: CompletionParameters): Boolean {
        val text = textBeforeCompletion(parameters)
        val previousWord = previousSqlWordBeforePrefix(text)
        return previousWord in TABLE_COMPLETION_PREVIOUS_WORDS
    }

    private fun isColumnCompletionContext(parameters: CompletionParameters): Boolean {
        if (isTableCompletionContext(parameters)) {
            return false
        }
        if (isPropertyCompletionContext(parameters)) {
            return false
        }
        val text = textBeforeCompletion(parameters)
        val previousClause = previousQueryClause(text)
        return previousClause in COLUMN_COMPLETION_CLAUSES
    }

    private fun isOrderByCompletionContext(parameters: CompletionParameters): Boolean {
        val text = textBeforeCompletion(parameters)
        return previousQueryClause(text) == "ORDER BY"
    }

    private fun isPropertyCompletionContext(parameters: CompletionParameters): Boolean {
        return containingElement(parameters.position, StarRocksElementTypes.PROPERTIES_CLAUSE) != null
    }

    private fun textBeforeCompletion(parameters: CompletionParameters): String {
        val documentText = parameters.editor.document.charsSequence
        return documentText.subSequence(0, parameters.offset.coerceAtMost(documentText.length)).toString()
    }

    private fun previousSqlWordBeforePrefix(text: String): String? {
        val withoutPrefix = text.replace(Regex("[A-Za-z_][A-Za-z0-9_]*$"), "")
        return Regex("[A-Za-z_][A-Za-z0-9_]*")
            .findAll(withoutPrefix)
            .lastOrNull()
            ?.value
            ?.uppercase(Locale.ROOT)
    }

    private fun previousQueryClause(text: String): String? {
        val withoutPrefix = text.replace(Regex("[A-Za-z_][A-Za-z0-9_]*$"), "")
        return Regex("\\b(WITH|SELECT|FROM|JOIN|ON|WHERE|GROUP\\s+BY|HAVING|QUALIFY|ORDER\\s+BY|LIMIT|VALUES|SET)\\b", RegexOption.IGNORE_CASE)
            .findAll(withoutPrefix)
            .lastOrNull()
            ?.value
            ?.uppercase(Locale.ROOT)
            ?.replace(Regex("\\s+"), " ")
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

    private fun derivedTableColumnNames(tableReferenceScope: PsiElement): Set<String> {
        val aliasColumns = tableAliasColumnNames(tableReferenceScope)
        if (aliasColumns.isNotEmpty()) {
            return aliasColumns
        }
        val outputs = mutableListOf<PsiElement>()
        collectDerivedTableSelectOutputs(tableReferenceScope, tableReferenceScope, outputs)
        return outputs.mapNotNullTo(linkedSetOf()) { output ->
            normalizedName(output).takeIf { it.isNotBlank() }
        }
    }

    private fun tableAliasColumnNames(tableReferenceScope: PsiElement): Set<String> {
        return collectElements(tableReferenceScope, StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME)
            .mapNotNullTo(linkedSetOf()) { column ->
                normalizedName(column).takeIf { it.isNotBlank() }
            }
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

    private fun collectCteColumnNames(root: PsiElement): List<PsiElement> {
        val result = mutableListOf<PsiElement>()
        collectCteColumnNames(root, root, result)
        return result
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
        file: PsiFile,
        elementType: IElementType
    ): List<PsiElement> {
        return collectElements(file as PsiElement, elementType)
    }

    private fun collectElements(
        element: PsiElement,
        elementType: IElementType
    ): List<PsiElement> {
        val result = mutableListOf<PsiElement>()
        collectElements(element, elementType, result)
        return result
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

    private fun collectLocalTableTargets(element: PsiElement): List<PsiElement> {
        val result = mutableListOf<PsiElement>()
        collectLocalTableTargets(element, result)
        return result
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

    private fun normalizedName(element: PsiElement): String {
        return when (element) {
            is StarRocksNamedStubElement -> element.name
            is SqlDefinition -> sqlDefinitionName(element)
            else -> StarRocksNamedStubElement.normalizeName(element.text)
        }
    }

    private fun sqlDefinitionName(definition: SqlDefinition): String {
        val nameElementText = definition.nameElement?.text
        val normalizedNameElement = StarRocksNamedStubElement.normalizeName(nameElementText.orEmpty())
        return normalizedNameElement.takeIf { it.isNotBlank() } ?: definition.name.orEmpty()
    }

    private fun matchesTableName(
        candidate: PsiElement,
        referenceName: String
    ): Boolean {
        val normalizedCandidate = normalizedName(candidate).lowercase(Locale.ROOT)
        val normalizedReference = StarRocksNamedStubElement.normalizeName(referenceName).lowercase(Locale.ROOT)
        return if ("." in normalizedReference) {
            normalizedCandidate == normalizedReference || normalizedCandidate.endsWith(".$normalizedReference")
        } else {
            normalizedCandidate == normalizedReference || normalizedCandidate.substringAfterLast(".") == normalizedReference
        }
    }

    private fun snippetInsertHandler(insertText: String): InsertHandler<LookupElement> {
        return InsertHandler { context, _ ->
            context.document.replaceString(context.startOffset, context.tailOffset, insertText)
            context.editor.caretModel.moveToOffset(context.startOffset + insertText.length)
        }
    }

    private companion object {
        private val LOCAL_TABLE_DEFINITION_TYPES = setOf(
            SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT
        )

        private val TABLE_COMPLETION_PREVIOUS_WORDS = setOf(
            "FROM",
            "JOIN",
            "INTO",
            "UPDATE"
        )

        private val COLUMN_COMPLETION_CLAUSES = setOf(
            "SELECT",
            "ON",
            "WHERE",
            "GROUP BY",
            "HAVING",
            "QUALIFY",
            "ORDER BY",
            "SET"
        )

        private val STATEMENT_TYPES = StarRocksStatementElementSets.STATEMENT_TYPES

        private val QUERY_SCOPE_TYPES = StarRocksStatementElementSets.QUERY_SCOPE_TYPES
    }
}

package com.github.ycyz.starrocks.datagrip.completion

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.resolve.containingElement
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
        StarRocksCompletionScope.tableNames(parameters.originalFile).forEach { tableName ->
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
        StarRocksCompletionScope.columnNames(parameters.position, parameters.originalFile)
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
        StarRocksCompletionScope.selectAliasNames(parameters.position, parameters.originalFile)
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

    private fun snippetInsertHandler(insertText: String): InsertHandler<LookupElement> {
        return InsertHandler { context, _ ->
            context.document.replaceString(context.startOffset, context.tailOffset, insertText)
            context.editor.caretModel.moveToOffset(context.startOffset + insertText.length)
        }
    }

    private companion object {
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

    }
}

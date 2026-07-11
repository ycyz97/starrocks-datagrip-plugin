package com.github.ycyz.starrocks.datagrip.completion

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
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
        return StarRocksCompletionContext.isTable(parameters.position)
    }

    private fun isColumnCompletionContext(parameters: CompletionParameters): Boolean {
        return StarRocksCompletionContext.isColumn(parameters.position)
    }

    private fun isOrderByCompletionContext(parameters: CompletionParameters): Boolean {
        return StarRocksCompletionContext.isOrderBy(parameters.position)
    }

    private fun isPropertyCompletionContext(parameters: CompletionParameters): Boolean {
        return StarRocksCompletionContext.isProperty(parameters.position)
    }

    private fun snippetInsertHandler(insertText: String): InsertHandler<LookupElement> {
        return InsertHandler { context, _ ->
            context.document.replaceString(context.startOffset, context.tailOffset, insertText)
            context.editor.caretModel.moveToOffset(context.startOffset + insertText.length)
        }
    }

}

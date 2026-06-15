package com.github.ycyz.starrocks.datagrip.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.Document
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.github.ycyz.starrocks.datagrip.dialect.ContextAwareDialectResolver

class StarRocksFunctionCompletionContributor : CompletionContributor() {
    private val resolver = ContextAwareDialectResolver()

    init {
        extend(
            CompletionType.BASIC,
            psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                ) {
                    if (!isStarRocksContext(parameters)) return
                    if (!isFunctionCompletionPosition(parameters.position)) return

                    if (isPropertiesBlock(parameters)) {
                        addPropertyCompletions(result)
                        return
                    }

                    addKeywordCompletions(result)
                    addSnippetCompletions(result)
                    addFunctionCompletions(result)
                }
            }
        )
    }

    private fun addFunctionCompletions(result: CompletionResultSet) {
        StarRocksFunctionMetadataProvider.functions()
            .asSequence()
            .sortedBy { it.name }
            .forEach { function ->
                result.addElement(
                    LookupElementBuilder.create(function.name)
                        .withTypeText(function.category, true)
                        .withTailText(function.signature?.let { " $it" } ?: "", true)
                        .withInsertHandler { insertionContext, _ ->
                            val document = insertionContext.document
                            val offset = insertionContext.tailOffset
                            if (offset >= document.textLength || document.charsSequence[offset] != '(') {
                                document.insertString(offset, "()")
                                insertionContext.editor.caretModel.moveToOffset(offset + 1)
                            }
                        }
                )
            }
    }

    private fun addKeywordCompletions(result: CompletionResultSet) {
        QUERY_KEYWORDS.forEach { keyword ->
            result.addElement(
                LookupElementBuilder.create(keyword)
                    .withTypeText("StarRocks keyword", true)
            )
        }
    }

    private fun addSnippetCompletions(result: CompletionResultSet) {
        QUERY_SNIPPETS.forEach { snippet ->
            result.addElement(
                LookupElementBuilder.create(snippet.lookup)
                    .withPresentableText(snippet.presentableText)
                    .withTailText(" ${snippet.tailText}", true)
                    .withTypeText("StarRocks snippet", true)
                    .withInsertHandler { context, _ ->
                        replaceCompletionText(context.document, context.startOffset, context.tailOffset, snippet.insertText)
                        context.editor.caretModel.moveToOffset(context.startOffset + snippet.caretOffset)
                    }
            )
        }
    }

    private fun addPropertyCompletions(result: CompletionResultSet) {
        PROPERTY_COMPLETIONS.forEach { property ->
            result.addElement(
                LookupElementBuilder.create(property.key)
                    .withPresentableText(property.key)
                    .withTailText(" = \"${property.exampleValue}\"", true)
                    .withTypeText(property.category, true)
                    .withInsertHandler { context, _ ->
                        val insertText = "\"${property.key}\" = \"${property.exampleValue}\""
                        val valueStart = insertText.length - property.exampleValue.length - 1
                        replaceCompletionText(context.document, context.startOffset, context.tailOffset, insertText)
                        context.editor.caretModel.moveToOffset(context.startOffset + valueStart)
                    }
            )
        }
    }

    private fun replaceCompletionText(document: Document, startOffset: Int, tailOffset: Int, text: String) {
        document.replaceString(startOffset, tailOffset, text)
    }

    private fun isPropertiesBlock(parameters: CompletionParameters): Boolean {
        val offset = parameters.offset
        val text = parameters.originalFile.text
        if (offset !in 0..text.length) return false
        val propertiesIndex = findLastWordBeforeOffset(text, offset, "PROPERTIES") ?: return false
        val openParenIndex = text.indexOf('(', propertiesIndex + "PROPERTIES".length)
        if (openParenIndex < 0 || openParenIndex >= offset) return false
        return isInsideParenBlock(text, openParenIndex, offset)
    }

    private fun findLastWordBeforeOffset(text: String, offset: Int, word: String): Int? {
        var index = offset.coerceAtMost(text.length)
        while (index >= 0) {
            val found = text.lastIndexOf(word, index, ignoreCase = true)
            if (found < 0) return null
            if (isWordBoundary(text, found - 1) && isWordBoundary(text, found + word.length)) return found
            index = found - 1
        }
        return null
    }

    private fun isInsideParenBlock(text: String, openParenIndex: Int, offset: Int): Boolean {
        var depth = 0
        var quote: Char? = null
        var index = openParenIndex
        while (index < offset) {
            val char = text[index]
            if (quote != null) {
                if (char == quote && !isEscaped(text, index)) quote = null
                index++
                continue
            }
            when (char) {
                '\'', '"', '`' -> quote = char
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth <= 0 && index < offset - 1) return false
                }
            }
            index++
        }
        return depth > 0
    }

    private fun isEscaped(text: String, index: Int): Boolean {
        var slashCount = 0
        var cursor = index - 1
        while (cursor >= 0 && text[cursor] == '\\') {
            slashCount++
            cursor--
        }
        return slashCount % 2 == 1
    }

    private fun isWordBoundary(text: String, index: Int): Boolean =
        index !in text.indices || !text[index].isLetterOrDigit() && text[index] != '_'

    private fun isStarRocksContext(parameters: CompletionParameters): Boolean {
        val file = parameters.originalFile
        if (file.language.id.equals("StarRocks", ignoreCase = true)) return true
        return resolver.shouldEnableStarRocksEnhancement(file.project, file.virtualFile)
    }

    private fun isFunctionCompletionPosition(position: PsiElement): Boolean {
        if (position is PsiComment) return false
        val text = position.text
        if (text.startsWith("'") || text.startsWith("\"")) return false
        val parentText = position.parent?.text.orEmpty()
        if (parentText.startsWith("'") || parentText.startsWith("\"")) return false
        return true
    }

    private data class SnippetCompletion(
        val lookup: String,
        val presentableText: String,
        val tailText: String,
        val insertText: String,
        val caretOffset: Int,
    )

    private data class PropertyCompletion(
        val key: String,
        val exampleValue: String,
        val category: String,
    )

    private companion object {
        val QUERY_KEYWORDS = listOf(
            "QUALIFY",
            "ROLLUP",
            "CUBE",
            "GROUPING SETS",
            "UNNEST",
            "LATERAL"
        )

        val QUERY_SNIPPETS = listOf(
            SnippetCompletion(
                lookup = "UNNEST table function",
                presentableText = "UNNEST(...)",
                tailText = "table function",
                insertText = "UNNEST()",
                caretOffset = "UNNEST(".length
            ),
            SnippetCompletion(
                lookup = "CROSS JOIN LATERAL UNNEST",
                presentableText = "CROSS JOIN LATERAL UNNEST(...)",
                tailText = "table function join",
                insertText = "CROSS JOIN LATERAL UNNEST()",
                caretOffset = "CROSS JOIN LATERAL UNNEST(".length
            ),
            SnippetCompletion(
                lookup = "LEFT JOIN UNNEST",
                presentableText = "LEFT JOIN UNNEST(...) ON ...",
                tailText = "table function join",
                insertText = "LEFT JOIN UNNEST() ON ",
                caretOffset = "LEFT JOIN UNNEST(".length
            ),
            SnippetCompletion(
                lookup = "QUALIFY row_number",
                presentableText = "QUALIFY row_number() OVER (...) = 1",
                tailText = "latest row filter",
                insertText = "QUALIFY row_number() OVER () = 1",
                caretOffset = "QUALIFY row_number() OVER (".length
            ),
            SnippetCompletion(
                lookup = "GROUP BY ROLLUP",
                presentableText = "GROUP BY ROLLUP (...)",
                tailText = "grouping extension",
                insertText = "GROUP BY ROLLUP ()",
                caretOffset = "GROUP BY ROLLUP (".length
            ),
            SnippetCompletion(
                lookup = "GROUP BY CUBE",
                presentableText = "GROUP BY CUBE (...)",
                tailText = "grouping extension",
                insertText = "GROUP BY CUBE ()",
                caretOffset = "GROUP BY CUBE (".length
            ),
            SnippetCompletion(
                lookup = "GROUP BY GROUPING SETS",
                presentableText = "GROUP BY GROUPING SETS (...)",
                tailText = "grouping extension",
                insertText = "GROUP BY GROUPING SETS ()",
                caretOffset = "GROUP BY GROUPING SETS (".length
            )
        )

        val PROPERTY_COMPLETIONS = listOf(
            PropertyCompletion("replication_num", "3", "StarRocks table property"),
            PropertyCompletion("storage_format", "DEFAULT", "StarRocks table property"),
            PropertyCompletion("compression", "LZ4", "StarRocks table property"),
            PropertyCompletion("partition_live_number", "-1", "StarRocks partition property"),
            PropertyCompletion("dynamic_partition.enable", "true", "StarRocks partition property"),
            PropertyCompletion("dynamic_partition.time_unit", "DAY", "StarRocks partition property"),
            PropertyCompletion("dynamic_partition.start", "-7", "StarRocks partition property"),
            PropertyCompletion("dynamic_partition.end", "3", "StarRocks partition property"),
            PropertyCompletion("dynamic_partition.prefix", "p", "StarRocks partition property"),
            PropertyCompletion("dynamic_partition.buckets", "8", "StarRocks partition property"),
            PropertyCompletion("replicated_storage", "true", "StarRocks table property"),
            PropertyCompletion("bucket_size", "1073741824", "StarRocks distribution property"),
            PropertyCompletion("partition_ttl_number", "-1", "StarRocks materialized view property"),
            PropertyCompletion("partition_refresh_number", "1", "StarRocks materialized view property"),
            PropertyCompletion("auto_refresh_partitions_limit", "1", "StarRocks materialized view property"),
            PropertyCompletion("excluded_trigger_tables", "", "StarRocks materialized view property"),
            PropertyCompletion("resource_group", "default_mv_wg", "StarRocks materialized view property")
        )
    }
}

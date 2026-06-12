package com.github.ycyz.starrocks.datagrip.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
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
            }
        )
    }

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
}

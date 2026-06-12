package com.github.ycyz.starrocks.datagrip.highlight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.github.ycyz.starrocks.datagrip.dialect.ContextAwareDialectResolver
import com.github.ycyz.starrocks.datagrip.dialect.DefaultStarRocksDialectRules

class StarRocksSqlAnnotator : Annotator {
    private val resolver = ContextAwareDialectResolver()
    private val rules = DefaultStarRocksDialectRules()

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.firstChild != null) return

        val tokenText = element.text
        if (!isIdentifier(tokenText)) return

        val upper = tokenText.uppercase()
        val isAdditionalKeyword = rules.isAdditionalKeyword(upper)
        val isSyntaxKeyword = rules.isStarRocksSyntaxKeyword(upper) && hasStarRocksSyntaxContext(element, upper)
        val isFunctionCall = !isAdditionalKeyword &&
            !isSyntaxKeyword &&
            rules.isFunctionName(upper) &&
            !rules.isFunctionLikeButNotCall(upper) &&
            hasCallParenAhead(element)

        if (!isAdditionalKeyword && !isSyntaxKeyword && !isFunctionCall) return

        val file = element.containingFile ?: return
        val vFile = file.virtualFile ?: return
        val project = file.project
        if (!isStarRocksContext(element, file, project, vFile)) return

        if (isAdditionalKeyword || isSyntaxKeyword) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .textAttributes(DefaultLanguageHighlighterColors.KEYWORD)
                .create()
            return
        }

        if (isFunctionCall) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_CALL)
                .create()
        }
    }

    private fun isIdentifier(text: String): Boolean {
        if (text.isBlank() || text.length > 128) return false
        return text[0].isLetter() || text[0] == '_'
    }

    private fun hasCallParenAhead(element: PsiElement): Boolean {
        var next = PsiTreeUtil.nextLeaf(element)
        while (next is PsiWhiteSpace) next = next.nextSibling
        return next?.text == "("
    }

    private fun isStarRocksContext(
        element: PsiElement,
        file: PsiFile,
        project: Project,
        vFile: VirtualFile,
    ): Boolean {
        return element.language.id.equals("StarRocks", ignoreCase = true) ||
            file.language.id.equals("StarRocks", ignoreCase = true) ||
            resolver.shouldEnableStarRocksEnhancement(project, vFile)
    }

    private fun hasStarRocksSyntaxContext(element: PsiElement, tokenText: String): Boolean {
        val previous = previousWord(element)
        val next = nextWord(element)
        return when (tokenText) {
            "AGGREGATE", "DUPLICATE", "PRIMARY", "UNIQUE" -> next == "KEY"
            "BUCKETS" -> previous == ")" || previous == "HASH"
            "CATALOG" -> previous in setOf("CREATE", "EXTERNAL", "ALTER", "DROP", "SHOW") || next == "PROPERTIES"
            "COMMENT" -> previous in setOf("CATALOG", "RESOURCE") || next != null
            "CUBE" -> previous == "BY" || next == "("
            "DEFERRED" -> previous == "REFRESH" || next in setOf("ASYNC", "MANUAL", "SCHEDULE")
            "DISTRIBUTED" -> next == "BY"
            "ENGINE" -> next == "=" || next == "OLAP"
            "EVERY" -> previous in setOf("SCHEDULE", "ASYNC") || next == "("
            "EXPORT" -> next == "TABLE" || next == "DATABASE" || previous == null
            "FULL" -> next == "JOIN" || next == "OUTER"
            "GROUPING" -> previous == "BY" || next == "SETS"
            "HASH" -> previous == "BY" || next == "("
            "IMMEDIATE" -> previous == "REFRESH" || next in setOf("ASYNC", "MANUAL", "SCHEDULE")
            "JOIN" -> previous == "FULL" || previous == "OUTER"
            "LABEL" -> previous == "LOAD" || previous == "CANCEL"
            "LATERAL" -> previous == "JOIN" && next == "UNNEST"
            "LOAD" -> next == "LABEL"
            "MANUAL" -> previous == "REFRESH" || previous in setOf("IMMEDIATE", "DEFERRED")
            "MATERIALIZED" -> next == "VIEW"
            "OLAP" -> previous == "ENGINE" || previous == "="
            "OUTER" -> previous == "FULL" && next == "JOIN"
            "OVERWRITE" -> previous == "INSERT"
            "PIPE" -> previous in setOf("CREATE", "ALTER", "DROP", "SHOW", "PAUSE", "RESUME", "STOP")
            "PARTITION" -> next == "BY"
            "PROPERTIES" -> next == "("
            "QUALIFY" -> previous != null
            "RANDOM" -> previous == "BY" || previous == "DISTRIBUTED"
            "REFRESH" -> next == "MANUAL" || next == "ASYNC" || next == "MATERIALIZED"
            "RESOURCE" -> previous in setOf("CREATE", "ALTER", "DROP", "SHOW")
            "ROLLUP" -> previous == "BY" || next == "("
            "SCHEDULE" -> previous == "REFRESH" || previous in setOf("IMMEDIATE", "DEFERRED")
            "SET" -> previous in setOf("CATALOG", "RESOURCE") || next == "PROPERTIES"
            "SETS" -> previous == "GROUPING"
            "START" -> previous == "SCHEDULE" || next == "("
            "TASK" -> previous == "SUBMIT" || previous == "CREATE"
            "UNSET" -> previous in setOf("CATALOG", "RESOURCE") || next == "PROPERTIES"
            else -> false
        }
    }

    private fun previousWord(element: PsiElement): String? {
        var previous = PsiTreeUtil.prevLeaf(element)
        while (previous != null) {
            if (previous !is PsiWhiteSpace) {
                val text = previous.text
                if (isIdentifier(text) || text == ")" || text == "=") return text.uppercase()
            }
            previous = PsiTreeUtil.prevLeaf(previous)
        }
        return null
    }

    private fun nextWord(element: PsiElement): String? {
        var next = PsiTreeUtil.nextLeaf(element)
        while (next != null) {
            if (next !is PsiWhiteSpace) {
                val text = next.text
                if (isIdentifier(text) || text == "(" || text == "=") return text.uppercase()
            }
            next = PsiTreeUtil.nextLeaf(next)
        }
        return null
    }
}

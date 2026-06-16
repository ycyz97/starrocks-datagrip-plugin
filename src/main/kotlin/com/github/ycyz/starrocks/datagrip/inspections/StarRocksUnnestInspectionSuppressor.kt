package com.github.ycyz.starrocks.datagrip.inspections

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.sql.inspections.suppression.SqlInspectionSuppressorDelegate
import com.github.ycyz.starrocks.datagrip.dialect.ContextAwareDialectResolver

class StarRocksUnnestInspectionSuppressor : SqlInspectionSuppressorDelegate {
    private val resolver = ContextAwareDialectResolver()

    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (toolId != SQL_RESOLVE_INSPECTION_ID) return false

        val file = element.containingFile ?: return false
        if (!isStarRocksContext(element, file)) return false

        if (isStarRocksCastTargetType(element)) return true
        if (!element.text.equals("unnest", ignoreCase = true)) return false

        val statementText = statementText(element)
        return statementText.contains(UNNEST_TABLE_FUNCTION)
    }

    private fun isStarRocksContext(element: PsiElement, file: PsiFile): Boolean =
        element.language.id.equals("StarRocks", ignoreCase = true) ||
            file.language.id.equals("StarRocks", ignoreCase = true) ||
            resolver.shouldEnableStarRocksEnhancement(file.project, file.virtualFile)

    private fun isStarRocksCastTargetType(element: PsiElement): Boolean {
        if (element.text.uppercase() !in STARROCKS_CAST_TYPES) return false
        if (previousSignificantText(element) != "AS") return false
        return statementText(element).contains(CAST_EXPRESSION)
    }

    private fun previousSignificantText(element: PsiElement): String? {
        var previous = PsiTreeUtil.prevLeaf(element)
        while (previous is PsiWhiteSpace) previous = PsiTreeUtil.prevLeaf(previous)
        return previous?.text?.uppercase()
    }

    private fun statementText(element: PsiElement): String {
        val text = element.containingFile?.text ?: return ""
        val range = element.textRange ?: return text
        val start = text.lastIndexOf(';', (range.startOffset - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val end = text.indexOf(';', range.endOffset).let { if (it < 0) text.length else it }
        return text.substring(start, end).uppercase()
    }

    private companion object {
        const val SQL_RESOLVE_INSPECTION_ID = "SqlResolve"
        val CAST_EXPRESSION = Regex("\\bCAST\\s*\\(")
        val UNNEST_TABLE_FUNCTION = Regex("\\bUNNEST\\s*\\(")
        val STARROCKS_CAST_TYPES = setOf(
            "ARRAY",
            "BIGINT",
            "BITMAP",
            "BOOLEAN",
            "CHAR",
            "DATE",
            "DATETIME",
            "DECIMAL",
            "DECIMAL32",
            "DECIMAL64",
            "DECIMAL128",
            "DECIMALV2",
            "DOUBLE",
            "FLOAT",
            "HLL",
            "INT",
            "INTEGER",
            "JSON",
            "LARGEINT",
            "MAP",
            "SMALLINT",
            "STRING",
            "STRUCT",
            "TEXT",
            "TIME",
            "TINYINT",
            "VARCHAR"
        )
    }
}

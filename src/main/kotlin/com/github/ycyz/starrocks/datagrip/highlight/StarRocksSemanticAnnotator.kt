package com.github.ycyz.starrocks.datagrip.highlight

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.sql.editor.SqlColors

class StarRocksSemanticAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val attributesKey = when (element.node?.elementType) {
            StarRocksElementTypes.TABLE_REFERENCE_NAME -> SqlColors.SQL_TABLE
            StarRocksElementTypes.COLUMN_NAME,
            StarRocksElementTypes.CTE_COLUMN_NAME,
            StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME -> SqlColors.SQL_COLUMN
            StarRocksElementTypes.COLUMN_REFERENCE_NAME -> SqlColors.SQL_COLUMN
            StarRocksElementTypes.TABLE_ALIAS,
            StarRocksElementTypes.SELECT_ALIAS,
            StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX,
            StarRocksElementTypes.WINDOW_NAME,
            StarRocksElementTypes.WINDOW_REFERENCE_NAME -> SqlColors.SQL_LOCAL_ALIAS
            StarRocksElementTypes.PROPERTY_KEY -> SqlColors.SQL_PARAMETER
            else -> return
        }
        highlight(element, holder, attributesKey)
    }

    private fun highlight(
        element: PsiElement,
        holder: AnnotationHolder,
        attributesKey: TextAttributesKey
    ) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element)
            .textAttributes(attributesKey)
            .create()
    }

}

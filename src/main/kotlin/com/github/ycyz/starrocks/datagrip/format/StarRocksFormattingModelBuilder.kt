package com.github.ycyz.starrocks.datagrip.format

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.psi.PsiDocumentManager
import com.intellij.sql.formatter.SqlFormattingModelBuilder

class StarRocksFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val file = formattingContext.psiElement.containingFile
        val project = file.project
        val document = PsiDocumentManager.getInstance(project).getDocument(file)
        val modelDocument = document ?: DocumentImpl(file.text, true)
        return SqlFormattingModelBuilder.createModel(
            project,
            StarRocksDialect.INSTANCE,
            modelDocument.charsSequence,
            modelDocument,
            formattingContext.codeStyleSettings
        )
    }
}

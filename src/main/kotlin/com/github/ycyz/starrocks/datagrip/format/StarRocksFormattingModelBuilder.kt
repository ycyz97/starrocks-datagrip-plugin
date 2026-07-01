package com.github.ycyz.starrocks.datagrip.format

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingDocumentModel
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.sql.dialects.generic.GenericDialect
import com.intellij.sql.formatter.SqlFormattingModelBuilder

class StarRocksFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val file = formattingContext.psiElement.containingFile
        val project = file.project
        val document = PsiDocumentManager.getInstance(project).getDocument(file)
        val modelDocument = document ?: DocumentImpl(file.text, true)
        if (StarRocksFormattingProfile.requiresSafeFormatter(modelDocument.charsSequence)) {
            return StarRocksSafeFormattingModel(file, modelDocument)
        }
        return SqlFormattingModelBuilder.createModel(
            project,
            GenericDialect.INSTANCE,
            modelDocument.charsSequence,
            modelDocument,
            formattingContext.codeStyleSettings
        )
    }
}

private class StarRocksSafeFormattingModel(
    file: PsiFile,
    document: Document
) : FormattingModel {
    private val rootBlock: Block = StarRocksSafeBlock(file.textLength)
    private val documentModel: FormattingDocumentModel = FormattingDocumentModelImpl(document, file)

    override fun getRootBlock(): Block = rootBlock

    override fun getDocumentModel(): FormattingDocumentModel = documentModel

    override fun replaceWhiteSpace(textRange: TextRange, whiteSpace: String): TextRange = textRange

    override fun shiftIndentInsideRange(node: ASTNode, range: TextRange, indent: Int): TextRange = range

    override fun commitChanges() = Unit
}

private class StarRocksSafeBlock(
    textLength: Int
) : Block {
    private val textRange = TextRange(0, textLength)

    override fun getTextRange(): TextRange = textRange

    override fun getSubBlocks(): List<Block> = emptyList()

    override fun getWrap(): Wrap? = null

    override fun getIndent(): Indent = Indent.getNoneIndent()

    override fun getAlignment(): Alignment? = null

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = null

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        return ChildAttributes(Indent.getNoneIndent(), null)
    }

    override fun isIncomplete(): Boolean = false

    override fun isLeaf(): Boolean = true
}

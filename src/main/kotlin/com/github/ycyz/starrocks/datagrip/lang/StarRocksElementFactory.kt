package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.base.SqlElementFactory
import com.intellij.sql.psi.impl.SqlCompositeElementImpl

class StarRocksElementFactory : SqlElementFactory() {
    override fun createElementNode(type: IElementType): CompositeElement {
        if (type is StarRocksNamedStubElementType) {
            return StarRocksNamedStubCompositeElement(type)
        }
        if (type == StarRocksElementTypes.TABLE_REFERENCE_NAME) {
            return StarRocksTableReferenceNameElement(type)
        }
        if (type == StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX) {
            return StarRocksQualifiedColumnPrefixElement(type)
        }
        if (type == StarRocksElementTypes.COLUMN_REFERENCE_NAME) {
            return StarRocksColumnReferenceNameElement(type)
        }
        if (type == StarRocksElementTypes.WINDOW_REFERENCE_NAME) {
            return StarRocksWindowReferenceNameElement(type)
        }
        if (type is StarRocksElementType) {
            return SqlCompositeElementImpl(type)
        }
        return super.createElementNode(type) ?: SqlCompositeElementImpl(type)
    }

    override fun createCompositeElement(node: ASTNode): PsiElement {
        if (node.elementType is StarRocksNamedStubElementType) {
            return StarRocksNamedStubElement(node)
        }
        if (node.elementType == StarRocksElementTypes.TABLE_REFERENCE_NAME && node is PsiElement) {
            return node
        }
        if (node.elementType == StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX && node is PsiElement) {
            return node
        }
        if (node.elementType == StarRocksElementTypes.COLUMN_REFERENCE_NAME && node is PsiElement) {
            return node
        }
        if (node.elementType == StarRocksElementTypes.WINDOW_REFERENCE_NAME && node is PsiElement) {
            return node
        }
        if (node.elementType is StarRocksElementType) {
            return (node as? PsiElement) ?: ASTWrapperPsiElement(node)
        }
        return super.createCompositeElement(node)
    }
}

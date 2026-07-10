package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException

class StarRocksNamedStubElement :
    StubBasedPsiElementBase<StarRocksNamedStub>,
    StubBasedPsiElement<StarRocksNamedStub>,
    PsiNameIdentifierOwner {
    constructor(node: ASTNode) : super(node)

    constructor(stub: StarRocksNamedStub, elementType: StarRocksNamedStubElementType) : super(stub, elementType)

    override fun getName(): String {
        return stub?.name ?: normalizeName(text)
    }

    override fun getNameIdentifier(): PsiElement {
        var child = firstChild
        while (child != null && child.text.isBlank()) {
            child = child.nextSibling
        }
        return child ?: this
    }

    override fun setName(name: String): PsiElement {
        val replacement = createReplacementElement(name)
        return PostprocessReformattingAspect.getInstance(project)
            .disablePostprocessFormattingInside(Computable {
                replace(replacement)
            })
    }

    private fun createReplacementElement(name: String): StarRocksNamedStubElement {
        val type = node.elementType
        val identifier = quoteIdentifierIfNeeded(name)
        val text = sampleSql(type, identifier)
        val file = PsiFileFactory.getInstance(project)
            .createFileFromText("rename.sql", StarRocksDialect.INSTANCE, text)
        return findNamedElement(file, type)
            ?: throw IncorrectOperationException("Cannot rename StarRocks element ${type.debugName} to $name.")
    }

    companion object {
        fun normalizeName(text: String?): String {
            val trimmed = text?.trim().orEmpty()
            if (trimmed.isBlank()) {
                return ""
            }
            return StarRocksStubIndexKeys.splitQualifiedIdentifier(trimmed)
                .joinToString(".") { normalizeIdentifierSegment(it) }
        }

        private fun normalizeIdentifierSegment(text: String): String {
            val trimmed = text.trim()
            if (trimmed.length >= 2 && trimmed.first() == '`' && trimmed.last() == '`') {
                return trimmed.substring(1, trimmed.length - 1).replace("``", "`")
            }
            return trimmed
        }

        private fun quoteIdentifierIfNeeded(name: String): String {
            val normalized = normalizeName(name)
            if (normalized.isBlank()) {
                throw IncorrectOperationException("StarRocks name must not be blank.")
            }
            val alreadyQuoted = name.trim().length >= 2 && name.trim().first() == '`' && name.trim().last() == '`'
            if (alreadyQuoted) {
                return name.trim()
            }
            if (SIMPLE_IDENTIFIER.matches(normalized) && !StarRocksKeywordCatalog.isKeyword(normalized)) {
                return normalized
            }
            return "`" + normalized.replace("`", "``") + "`"
        }

        private fun sampleSql(type: IElementType, identifier: String): String {
            return when (type) {
                StarRocksElementTypes.COLUMN_NAME -> "CREATE TABLE rename_probe ($identifier BIGINT);"
                StarRocksElementTypes.CTE_COLUMN_NAME -> "WITH rename_probe($identifier) AS (SELECT 1) SELECT * FROM rename_probe;"
                StarRocksElementTypes.TABLE_ALIAS -> "SELECT * FROM rename_probe AS $identifier;"
                StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME -> "SELECT * FROM UNNEST([1]) AS rename_probe($identifier);"
                StarRocksElementTypes.WINDOW_NAME -> "SELECT row_number() OVER $identifier FROM rename_probe WINDOW $identifier AS ();"
                StarRocksElementTypes.SELECT_ALIAS -> "SELECT 1 AS $identifier;"
                else -> throw IncorrectOperationException("StarRocks rename is not supported for ${type.debugName}.")
            }
        }

        private fun findNamedElement(element: PsiElement, type: IElementType): StarRocksNamedStubElement? {
            if (element.node?.elementType == type && element is StarRocksNamedStubElement) {
                return element
            }
            element.children.forEach { child ->
                findNamedElement(child, type)?.let { return it }
            }
            return null
        }

        private val SIMPLE_IDENTIFIER = Regex("[A-Za-z_][A-Za-z0-9_$]*")
    }
}

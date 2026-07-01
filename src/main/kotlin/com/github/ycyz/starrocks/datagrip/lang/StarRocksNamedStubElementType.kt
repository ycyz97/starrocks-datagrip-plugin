package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

class StarRocksNamedStubElementType(
    externalName: String
) : IStubElementType<StarRocksNamedStub, StarRocksNamedStubElement>(externalName, StarRocksDialect.INSTANCE) {
    override fun getExternalId(): String = "sql.${super.toString()}"

    override fun createPsi(stub: StarRocksNamedStub): StarRocksNamedStubElement {
        return StarRocksNamedStubElement(stub, this)
    }

    override fun createStub(
        psi: StarRocksNamedStubElement,
        parentStub: StubElement<out PsiElement>
    ): StarRocksNamedStub {
        return StarRocksNamedStub(parentStub, this, psi.name)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        return StarRocksNamedStubElement.normalizeName(node.text).isNotBlank()
    }

    override fun serialize(stub: StarRocksNamedStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): StarRocksNamedStub {
        return StarRocksNamedStub(parentStub, this, dataStream.readNameString().orEmpty())
    }

    override fun indexStub(stub: StarRocksNamedStub, sink: IndexSink) {
        when (this) {
            StarRocksStubElementTypes.STARROCKS_TABLE_NAME -> {
                StarRocksStubIndexKeys.tableKeys(stub.name)
                    .forEach { sink.occurrence(StarRocksTableNameIndex.KEY, it) }
            }
            StarRocksStubElementTypes.STARROCKS_COLUMN_NAME -> {
                StarRocksStubIndexKeys.nameKeys(stub.name)
                    .forEach { sink.occurrence(StarRocksColumnNameIndex.KEY, it) }
            }
        }
    }
}

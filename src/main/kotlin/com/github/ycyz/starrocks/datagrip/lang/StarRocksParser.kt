package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class StarRocksParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        while (!builder.eof()) {
            if (builder.tokenText == ";") {
                builder.advanceLexer()
                continue
            }
            parseStatement(builder)
        }
        rootMarker.done(root)
        return builder.treeBuilt
    }

    private fun parseStatement(builder: PsiBuilder) {
        val marker = builder.mark()
        val family = StarRocksStatementClassifier.classify(builder)
        val boundaries = StarRocksClauseBoundaryScanner.scan(builder, family)
        val statementEndOffset = statementEndOffset(builder)
        var parenDepth = 0
        var nextBoundaryIndex = 0
        var segmentMarker: PsiBuilder.Marker? = null
        var segmentType: IElementType = StarRocksElementTypes.STATEMENT_SEGMENT
        val firstBoundaryOffset = boundaries.firstOrNull()?.startOffset ?: statementEndOffset
        if (builder.currentOffset < firstBoundaryOffset) {
            val headerMarker = builder.mark()
            StarRocksSegmentParser.parseHeader(builder, family, firstBoundaryOffset)
            headerMarker.done(StarRocksElementTypes.STATEMENT_HEADER)
        }
        while (!builder.eof()) {
            if (parenDepth == 0 && nextBoundaryIndex < boundaries.size && builder.currentOffset == boundaries[nextBoundaryIndex].startOffset) {
                segmentMarker?.done(segmentType)
                segmentType = boundaries[nextBoundaryIndex].elementType
                segmentMarker = builder.mark()
                val endOffset = boundaries.getOrNull(nextBoundaryIndex + 1)?.startOffset ?: statementEndOffset
                nextBoundaryIndex++
                StarRocksSegmentParser.parseSegment(builder, segmentType, endOffset)
                continue
            }
            when (builder.tokenText) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth--
                ";" -> if (parenDepth == 0) break
            }
            builder.advanceLexer()
        }
        segmentMarker?.done(segmentType)
        marker.done(StarRocksElementTypes.statementType(family))
    }

    private fun statementEndOffset(builder: PsiBuilder): Int {
        val marker = builder.mark()
        var parenDepth = 0
        var endOffset = builder.currentOffset
        while (!builder.eof()) {
            when (builder.tokenText) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth--
                ";" -> if (parenDepth == 0) break
            }
            endOffset = builder.currentOffset + (builder.tokenText?.length ?: 0)
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return endOffset
    }
}

package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType

object StarRocksSegmentParser {
    fun parseHeader(builder: PsiBuilder, family: StarRocksStatementFamily?, endOffset: Int) {
        when (family) {
            StarRocksStatementFamily.TABLE_DDL,
            StarRocksStatementFamily.VIEW,
            StarRocksStatementFamily.MATERIALIZED_VIEW -> parseCreateObjectHeader(builder, endOffset)
            else -> advanceTo(builder, endOffset)
        }
    }

    fun parseSegment(builder: PsiBuilder, segmentType: IElementType, endOffset: Int) {
        when (segmentType) {
            StarRocksElementTypes.SELECT_CLAUSE -> parseCommaSeparatedItems(
                builder,
                endOffset,
                StarRocksElementTypes.SELECT_ITEM,
                skipLeadingWords = 1,
                parseItemBody = true,
                itemBodyKind = ItemBodyKind.SELECT_ITEM
            )
            StarRocksElementTypes.GROUP_BY_CLAUSE -> parseCommaSeparatedItems(
                builder,
                endOffset,
                StarRocksElementTypes.GROUPING_ITEM,
                skipLeadingWords = 2,
                parseItemBody = true
            )
            StarRocksElementTypes.FROM_CLAUSE -> parseCommaSeparatedItems(
                builder,
                endOffset,
                StarRocksElementTypes.TABLE_REFERENCE,
                skipLeadingWords = 1,
                parseItemBody = true,
                itemBodyKind = ItemBodyKind.TABLE_REFERENCE
            )
            StarRocksElementTypes.WHERE_CLAUSE,
            StarRocksElementTypes.HAVING_CLAUSE -> parseWrappedRemainder(
                builder,
                endOffset,
                StarRocksElementTypes.PREDICATE_EXPRESSION,
                skipLeadingWords = 1,
                parseExpressionBody = true
            )
            StarRocksElementTypes.QUALIFY_CLAUSE -> parseWrappedRemainder(
                builder,
                endOffset,
                StarRocksElementTypes.QUALIFY_EXPRESSION,
                skipLeadingWords = 1,
                parseExpressionBody = true
            )
            StarRocksElementTypes.ORDER_BY_CLAUSE -> parseCommaSeparatedItems(
                builder,
                endOffset,
                StarRocksElementTypes.ORDERING_ITEM,
                skipLeadingWords = 2,
                parseItemBody = true
            )
            StarRocksElementTypes.LIMIT_CLAUSE -> parseWrappedRemainder(
                builder,
                endOffset,
                StarRocksElementTypes.LIMIT_EXPRESSION,
                skipLeadingWords = 1,
                parseExpressionBody = true
            )
            StarRocksElementTypes.SET_OPERATION_CLAUSE -> parseSetOperationClause(builder, endOffset)
            StarRocksElementTypes.WINDOW_CLAUSE -> parseCommaSeparatedItems(
                builder,
                endOffset,
                StarRocksElementTypes.WINDOW_DEFINITION,
                skipLeadingWords = 1,
                parseItemBody = true,
                itemBodyKind = ItemBodyKind.WINDOW_DEFINITION
            )
            StarRocksElementTypes.PROPERTIES_CLAUSE -> parseCommaSeparatedItems(
                builder,
                endOffset,
                StarRocksElementTypes.PROPERTY_PAIR,
                skipLeadingWords = 1,
                listMode = ListMode.PARENTHESIZED,
                parseItemBody = true,
                itemBodyKind = ItemBodyKind.PROPERTY_PAIR
            )
            StarRocksElementTypes.TABLE_COLUMN_LIST -> parseCommaSeparatedItems(
                builder,
                endOffset,
                StarRocksElementTypes.COLUMN_DEFINITION,
                skipLeadingWords = 0,
                listMode = ListMode.PARENTHESIZED,
                parseItemBody = true,
                itemBodyKind = ItemBodyKind.COLUMN_DEFINITION
            )
            StarRocksElementTypes.WITH_CLAUSE -> parseWithClause(builder, endOffset)
            StarRocksElementTypes.AS_SELECT_CLAUSE -> parseAsSelectClause(builder, endOffset)
            StarRocksElementTypes.KEY_MODEL_CLAUSE -> parseKeyModelClause(builder, endOffset)
            StarRocksElementTypes.PARTITION_CLAUSE -> parsePartitionClause(builder, endOffset)
            StarRocksElementTypes.DISTRIBUTION_CLAUSE -> parseDistributionClause(builder, endOffset)
            StarRocksElementTypes.INSERT_TARGET_CLAUSE -> parseDmlTargetClause(builder, endOffset)
            StarRocksElementTypes.SET_CLAUSE -> parseCommaSeparatedItems(
                builder,
                endOffset,
                StarRocksElementTypes.SET_ASSIGNMENT,
                skipLeadingWords = 1,
                parseItemBody = true
            )
            StarRocksElementTypes.VALUES_CLAUSE -> parseValuesClause(builder, endOffset)
            else -> advanceTo(builder, endOffset)
        }
    }

    private fun parseKeyModelClause(builder: PsiBuilder, endOffset: Int) {
        advanceToOpeningParenthesis(builder, endOffset)
        parseCommaSeparatedItems(
            builder,
            endOffset,
            StarRocksElementTypes.KEY_COLUMN,
            skipLeadingWords = 0,
            listMode = ListMode.PARENTHESIZED
        )
    }

    private fun parsePartitionClause(builder: PsiBuilder, endOffset: Int) {
        skipWords(builder, endOffset, 2)
        if (builder.eof() || builder.currentOffset >= endOffset) {
            return
        }
        val marker = builder.mark()
        StarRocksExpressionParser.parseExpressionRange(builder, endOffset)
        marker.done(StarRocksElementTypes.PARTITION_EXPRESSION)
    }

    private fun parseDistributionClause(builder: PsiBuilder, endOffset: Int) {
        skipWords(builder, endOffset, 2)
        val distributionEndOffset = topLevelWordOffset(builder, endOffset, "BUCKETS") ?: endOffset
        if (!builder.eof() && builder.currentOffset < distributionEndOffset) {
            val marker = builder.mark()
            StarRocksExpressionParser.parseExpressionRange(builder, distributionEndOffset)
            marker.done(StarRocksElementTypes.DISTRIBUTION_EXPRESSION)
        }
        if (!builder.eof() && builder.currentOffset < endOffset) {
            val marker = builder.mark()
            advanceTo(builder, endOffset)
            marker.done(StarRocksElementTypes.BUCKETS_CLAUSE)
        }
    }

    private fun parseCreateObjectHeader(builder: PsiBuilder, endOffset: Int) {
        while (!builder.eof() && builder.currentOffset < endOffset) {
            val text = builder.tokenText?.uppercase()
            if (text in CREATE_HEADER_SKIP_WORDS || builder.tokenText == ".") {
                builder.advanceLexer()
                continue
            }
            if (isIdentifierToken(builder)) {
                val marker = builder.mark()
                consumeQualifiedIdentifier(builder, endOffset)
                marker.done(StarRocksElementTypes.TABLE_NAME)
                continue
            }
            builder.advanceLexer()
        }
    }

    private fun parseDmlTargetClause(builder: PsiBuilder, endOffset: Int) {
        val firstWord = builder.tokenText?.uppercase()
        val targetStarters = when (firstWord) {
            "INSERT" -> setOf("INTO", "OVERWRITE")
            "UPDATE" -> emptySet()
            "DELETE" -> setOf("FROM")
            "MERGE" -> setOf("INTO")
            else -> emptySet()
        }
        var targetCanStart = firstWord == "UPDATE"
        while (!builder.eof() && builder.currentOffset < endOffset) {
            val text = builder.tokenText?.uppercase()
            if (text in targetStarters) {
                targetCanStart = true
                builder.advanceLexer()
                continue
            }
            if (!targetCanStart || text in DML_HEADER_SKIP_WORDS) {
                builder.advanceLexer()
                continue
            }
            if (isIdentifierToken(builder)) {
                val targetMarker = builder.mark()
                val nameMarker = builder.mark()
                consumeQualifiedIdentifier(builder, endOffset)
                nameMarker.done(StarRocksElementTypes.TABLE_REFERENCE_NAME)
                targetMarker.done(StarRocksElementTypes.DML_TARGET_TABLE)
                return
            }
            builder.advanceLexer()
        }
    }

    private fun parseWithClause(builder: PsiBuilder, endOffset: Int) {
        skipWords(builder, endOffset, 1)
        parseCommaSeparatedItems(
            builder,
            endOffset,
            StarRocksElementTypes.CTE_DEFINITION,
            skipLeadingWords = 0,
            parseItemBody = true,
            itemBodyKind = ItemBodyKind.CTE_DEFINITION
        )
    }

    private fun parseAsSelectClause(builder: PsiBuilder, endOffset: Int) {
        skipWords(builder, endOffset, 1)
        if (builder.eof() || builder.currentOffset >= endOffset) {
            return
        }
        val marker = builder.mark()
        parseQueryClauses(builder, endOffset)
        marker.done(StarRocksElementTypes.AS_SELECT_QUERY)
    }

    private fun parseValuesClause(builder: PsiBuilder, endOffset: Int) {
        skipWords(builder, endOffset, 1)
        while (!builder.eof() && builder.currentOffset < endOffset) {
            if (builder.tokenText == "(") {
                val rowEndOffset = matchingRightParenthesisOffset(builder, endOffset)
                val marker = builder.mark()
                builder.advanceLexer()
                StarRocksExpressionParser.parseExpressionRange(builder, rowEndOffset)
                if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == ")") {
                    builder.advanceLexer()
                }
                marker.done(StarRocksElementTypes.VALUES_ROW)
                continue
            }
            builder.advanceLexer()
        }
    }

    private fun parseSetOperationClause(builder: PsiBuilder, endOffset: Int) {
        if (builder.eof() || builder.currentOffset >= endOffset) {
            return
        }
        val marker = builder.mark()
        builder.advanceLexer()
        if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText?.uppercase() in SET_OPERATION_MODIFIERS) {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.SET_OPERATOR)
        advanceTo(builder, endOffset)
    }

    fun parseQueryClauses(builder: PsiBuilder, endOffset: Int) {
        while (!builder.eof() && builder.currentOffset < endOffset) {
            val segmentType = nestedQueryClauseType(builder)
            if (segmentType == null) {
                builder.advanceLexer()
                continue
            }
            val segmentEndOffset = nestedQueryClauseEndOffset(builder, endOffset)
            val marker = builder.mark()
            parseSegment(builder, segmentType, segmentEndOffset)
            marker.done(segmentType)
        }
    }

    private fun nestedQueryClauseType(builder: PsiBuilder): IElementType? {
        return when (builder.tokenText?.uppercase()) {
            "WITH" -> StarRocksElementTypes.WITH_CLAUSE
            "SELECT" -> StarRocksElementTypes.SELECT_CLAUSE
            "FROM" -> StarRocksElementTypes.FROM_CLAUSE
            "WHERE" -> StarRocksElementTypes.WHERE_CLAUSE
            "GROUP" -> if (nextWord(builder) == "BY") StarRocksElementTypes.GROUP_BY_CLAUSE else null
            "HAVING" -> StarRocksElementTypes.HAVING_CLAUSE
            "WINDOW" -> if (nextNamedWindowWord(builder) == "AS") StarRocksElementTypes.WINDOW_CLAUSE else null
            "QUALIFY" -> StarRocksElementTypes.QUALIFY_CLAUSE
            "ORDER" -> if (nextWord(builder) == "BY") StarRocksElementTypes.ORDER_BY_CLAUSE else null
            "LIMIT" -> StarRocksElementTypes.LIMIT_CLAUSE
            in SET_OPERATION_WORDS -> StarRocksElementTypes.SET_OPERATION_CLAUSE
            else -> null
        }
    }

    private fun nestedQueryClauseEndOffset(builder: PsiBuilder, endOffset: Int): Int {
        val marker = builder.mark()
        var parenDepth = 0
        builder.advanceLexer()
        var result = endOffset
        while (!builder.eof() && builder.currentOffset < endOffset) {
            when (builder.tokenText) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth--
            }
            if (parenDepth == 0 && nestedQueryClauseType(builder) != null) {
                result = builder.currentOffset
                break
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun parseCommaSeparatedItems(
        builder: PsiBuilder,
        endOffset: Int,
        itemType: IElementType,
        skipLeadingWords: Int,
        listMode: ListMode = ListMode.PLAIN,
        parseItemBody: Boolean = false,
        itemBodyKind: ItemBodyKind = ItemBodyKind.EXPRESSION
    ) {
        skipWords(builder, endOffset, skipLeadingWords)
        if (listMode == ListMode.PARENTHESIZED) {
            advanceToOpeningParenthesis(builder, endOffset)
        }
        var parenDepth = 0
        var itemMarker: PsiBuilder.Marker? = null
        while (!builder.eof() && builder.currentOffset < endOffset) {
            if (listMode == ListMode.PARENTHESIZED && builder.tokenText == "(" && itemMarker == null) {
                builder.advanceLexer()
                continue
            }
            if (listMode == ListMode.PARENTHESIZED && builder.tokenText == ")" && parenDepth == 0) {
                itemMarker?.done(itemType)
                itemMarker = null
                builder.advanceLexer()
                break
            }
            if (itemMarker == null && isItemToken(builder)) {
                itemMarker = builder.mark()
            }
            when (builder.tokenText) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth--
                "," -> if (parenDepth == 0) {
                    itemMarker?.done(itemType)
                    builder.advanceLexer()
                    itemMarker = null
                    continue
                }
            }
            if (parseItemBody) {
                parseItemBody(builder, itemEndOffset(builder, endOffset), itemBodyKind)
            } else {
                builder.advanceLexer()
            }
        }
        itemMarker?.done(itemType)
    }

    private fun advanceToOpeningParenthesis(builder: PsiBuilder, endOffset: Int) {
        while (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText != "(") {
            builder.advanceLexer()
        }
    }

    private fun parseWrappedRemainder(
        builder: PsiBuilder,
        endOffset: Int,
        expressionType: IElementType,
        skipLeadingWords: Int,
        parseExpressionBody: Boolean = false
    ) {
        skipWords(builder, endOffset, skipLeadingWords)
        if (builder.eof() || builder.currentOffset >= endOffset) {
            return
        }
        val marker = builder.mark()
        if (parseExpressionBody) {
            StarRocksExpressionParser.parseExpressionRange(builder, endOffset)
        } else {
            advanceTo(builder, endOffset)
        }
        marker.done(expressionType)
    }

    private fun itemEndOffset(builder: PsiBuilder, endOffset: Int): Int {
        val marker = builder.mark()
        var parenDepth = 0
        var result = endOffset
        while (!builder.eof() && builder.currentOffset < endOffset) {
            when (builder.tokenText) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) {
                    parenDepth--
                } else {
                    result = builder.currentOffset
                    break
                }
                "," -> if (parenDepth == 0) {
                    result = builder.currentOffset
                    break
                }
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun parseItemBody(builder: PsiBuilder, endOffset: Int, kind: ItemBodyKind) {
        when (kind) {
            ItemBodyKind.EXPRESSION -> StarRocksExpressionParser.parseExpressionRange(builder, endOffset)
            ItemBodyKind.SELECT_ITEM -> parseSelectItemBody(builder, endOffset)
            ItemBodyKind.COLUMN_DEFINITION -> parseColumnDefinitionBody(builder, endOffset)
            ItemBodyKind.CTE_DEFINITION -> parseCteDefinitionBody(builder, endOffset)
            ItemBodyKind.TABLE_REFERENCE -> parseTableReferenceBody(builder, endOffset)
            ItemBodyKind.PROPERTY_PAIR -> parsePropertyPairBody(builder, endOffset)
            ItemBodyKind.WINDOW_DEFINITION -> parseWindowDefinitionBody(builder, endOffset)
        }
    }

    private fun parseSelectItemBody(builder: PsiBuilder, endOffset: Int) {
        val alias = selectAliasCandidate(builder, endOffset)
        val expressionEndOffset = alias?.expressionEndOffset ?: endOffset
        if (builder.currentOffset < expressionEndOffset) {
            StarRocksExpressionParser.parseExpressionRange(builder, expressionEndOffset)
        }
        if (alias == null) {
            advanceTo(builder, endOffset)
            return
        }
        while (!builder.eof() && builder.currentOffset < alias.nameStartOffset) {
            builder.advanceLexer()
        }
        if (!builder.eof() && builder.currentOffset < endOffset && builder.currentOffset == alias.nameStartOffset) {
            val marker = builder.mark()
            builder.advanceLexer()
            marker.done(StarRocksElementTypes.SELECT_ALIAS)
        }
        advanceTo(builder, endOffset)
    }

    private fun selectAliasCandidate(builder: PsiBuilder, endOffset: Int): SelectAliasCandidate? {
        val tokens = topLevelMeaningfulTokens(builder, endOffset)
        val explicitAlias = tokens
            .asSequence()
            .filter { it.text.equals("AS", ignoreCase = true) }
            .mapNotNull { asToken ->
                val alias = tokens.firstOrNull { token ->
                    token.startOffset >= asToken.endOffset && isAliasIdentifier(token.text)
                }
                alias?.let {
                    SelectAliasCandidate(
                        expressionEndOffset = asToken.startOffset,
                        nameStartOffset = it.startOffset
                    )
                }
            }
            .lastOrNull()
        if (explicitAlias != null) {
            return explicitAlias
        }

        val candidate = tokens.lastOrNull() ?: return null
        if (!isAliasIdentifier(candidate.text) || StarRocksKeywordCatalog.isKeyword(candidate.text.uppercase())) {
            return null
        }
        val previous = tokens.getOrNull(tokens.lastIndex - 1) ?: return null
        if (previous.text == "." || previous.text.equals("INTERVAL", ignoreCase = true)) {
            return null
        }
        return SelectAliasCandidate(
            expressionEndOffset = candidate.startOffset,
            nameStartOffset = candidate.startOffset
        )
    }

    private fun topLevelMeaningfulTokens(builder: PsiBuilder, endOffset: Int): List<MeaningfulToken> {
        val marker = builder.mark()
        val result = mutableListOf<MeaningfulToken>()
        var parenDepth = 0
        var angleDepth = 0
        while (!builder.eof() && builder.currentOffset < endOffset) {
            val text = builder.tokenText
            if (text == ")" && parenDepth > 0) {
                parenDepth--
            }
            if (text == ">" && angleDepth > 0) {
                angleDepth--
            }
            if (!text.isNullOrBlank() && parenDepth == 0 && angleDepth == 0) {
                result += MeaningfulToken(text, builder.currentOffset, builder.currentOffset + text.length)
            }
            if (text == "(") {
                parenDepth++
            }
            if (text == "<") {
                angleDepth++
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun isAliasIdentifier(text: String): Boolean {
        return text.isNotBlank() &&
            text != "." &&
            text.firstOrNull()?.let { it == '_' || it == '`' || it.isLetter() } == true
    }

    private fun parseWindowDefinitionBody(builder: PsiBuilder, endOffset: Int) {
        parseWindowName(builder, endOffset)
        while (!builder.eof() && builder.currentOffset < endOffset) {
            if (builder.tokenText?.uppercase() == "AS" && nextMeaningfulTokenText(builder) == "(") {
                builder.advanceLexer()
                skipNoise(builder, endOffset)
                val specEndOffset = matchingRightParenthesisOffset(builder, endOffset)
                if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == "(") {
                    builder.advanceLexer()
                    StarRocksExpressionParser.parseExpressionRange(builder, specEndOffset)
                    if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == ")") {
                        builder.advanceLexer()
                    }
                }
                continue
            }
            builder.advanceLexer()
        }
    }

    private fun parseWindowName(builder: PsiBuilder, endOffset: Int) {
        while (!builder.eof() && builder.currentOffset < endOffset) {
            if (isIdentifierToken(builder)) {
                val marker = builder.mark()
                builder.advanceLexer()
                marker.done(StarRocksElementTypes.WINDOW_NAME)
                return
            }
            builder.advanceLexer()
        }
    }

    private fun parsePropertyPairBody(builder: PsiBuilder, endOffset: Int) {
        skipNoise(builder, endOffset)
        if (builder.eof() || builder.currentOffset >= endOffset || !isPropertyKeyStart(builder)) {
            advanceTo(builder, endOffset)
            return
        }
        val keyMarker = builder.mark()
        consumePropertyKey(builder, endOffset)
        keyMarker.done(StarRocksElementTypes.PROPERTY_KEY)
        while (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText != "=") {
            builder.advanceLexer()
        }
        if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == "=") {
            builder.advanceLexer()
        }
        skipNoise(builder, endOffset)
        if (!builder.eof() && builder.currentOffset < endOffset) {
            val valueMarker = builder.mark()
            advanceTo(builder, endOffset)
            valueMarker.done(StarRocksElementTypes.PROPERTY_VALUE)
        }
    }

    private fun parseTableReferenceBody(builder: PsiBuilder, endOffset: Int) {
        var expectTableFactor = true
        var parenDepth = 0
        while (!builder.eof() && builder.currentOffset < endOffset) {
            val text = builder.tokenText
            val upperText = text?.uppercase()
            if (expectTableFactor && upperText in TABLE_FACTOR_PREFIX_WORDS) {
                builder.advanceLexer()
                continue
            }
            if (expectTableFactor && text == "(") {
                parseParenthesizedTableFactor(builder, endOffset)
                parseOptionalTableAlias(builder, endOffset)
                expectTableFactor = false
                continue
            }
            if (expectTableFactor && isIdentifierToken(builder)) {
                parseTableFactorName(builder, endOffset)
                parseOptionalTableAlias(builder, endOffset)
                expectTableFactor = false
                continue
            }
            when (text) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth--
            }
            if (parenDepth == 0 && upperText == "JOIN") {
                builder.advanceLexer()
                expectTableFactor = true
                continue
            }
            if (parenDepth == 0 && upperText == "ON") {
                builder.advanceLexer()
                StarRocksExpressionParser.parseExpressionRange(builder, topLevelJoinConditionEndOffset(builder, endOffset))
                continue
            }
            builder.advanceLexer()
        }
    }

    private fun parseOptionalTableAlias(builder: PsiBuilder, endOffset: Int) {
        if (builder.eof() || builder.currentOffset >= endOffset) {
            return
        }
        if (builder.tokenText?.uppercase() == "AS") {
            builder.advanceLexer()
        }
        if (builder.eof() || builder.currentOffset >= endOffset) {
            return
        }
        if (!isIdentifierToken(builder) || builder.tokenText?.uppercase() in TABLE_ALIAS_STOP_WORDS) {
            return
        }
        val marker = builder.mark()
        builder.advanceLexer()
        marker.done(StarRocksElementTypes.TABLE_ALIAS)
    }

    private fun parseParenthesizedTableFactor(builder: PsiBuilder, endOffset: Int) {
        val closeOffset = matchingRightParenthesisOffset(builder, endOffset)
        if (nextMeaningfulWord(builder) in QUERY_START_WORDS) {
            val marker = builder.mark()
            builder.advanceLexer()
            parseQueryClauses(builder, closeOffset)
            if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == ")") {
                builder.advanceLexer()
            }
            marker.done(StarRocksElementTypes.SUBQUERY_EXPRESSION)
            return
        }
        while (!builder.eof() && builder.currentOffset < endOffset && builder.currentOffset <= closeOffset) {
            builder.advanceLexer()
        }
    }

    private fun parseTableFactorName(builder: PsiBuilder, endOffset: Int) {
        if (nextTokenAfterQualifiedIdentifier(builder, endOffset) == "(") {
            parseTableFunctionCall(builder, endOffset)
            return
        }
        val marker = builder.mark()
        consumeQualifiedIdentifier(builder, endOffset)
        marker.done(StarRocksElementTypes.TABLE_REFERENCE_NAME)
    }

    private fun parseTableFunctionCall(builder: PsiBuilder, endOffset: Int) {
        val marker = builder.mark()
        consumeQualifiedIdentifier(builder, endOffset)
        if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == "(") {
            val closeOffset = matchingRightParenthesisOffset(builder, endOffset)
            builder.advanceLexer()
            StarRocksExpressionParser.parseExpressionRange(builder, closeOffset)
            if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == ")") {
                builder.advanceLexer()
            }
        }
        marker.done(StarRocksElementTypes.FUNCTION_CALL)
    }

    private fun parseCteDefinitionBody(builder: PsiBuilder, endOffset: Int) {
        parseCteName(builder, endOffset)
        while (!builder.eof() && builder.currentOffset < endOffset) {
            if (builder.tokenText?.uppercase() == "AS" && nextMeaningfulTokenText(builder) == "(") {
                builder.advanceLexer()
                val queryEndOffset = matchingRightParenthesisOffset(builder, endOffset)
                if (builder.tokenText == "(") {
                    builder.advanceLexer()
                }
                val marker = builder.mark()
                parseQueryClauses(builder, queryEndOffset)
                marker.done(StarRocksElementTypes.CTE_QUERY)
                if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == ")") {
                    builder.advanceLexer()
                }
                continue
            }
            builder.advanceLexer()
        }
    }

    private fun parseCteName(builder: PsiBuilder, endOffset: Int) {
        while (!builder.eof() && builder.currentOffset < endOffset) {
            if (isIdentifierToken(builder)) {
                val marker = builder.mark()
                builder.advanceLexer()
                marker.done(StarRocksElementTypes.CTE_NAME)
                return
            }
            builder.advanceLexer()
        }
    }

    private fun parseColumnDefinitionBody(builder: PsiBuilder, endOffset: Int) {
        parseColumnName(builder, endOffset)
        if (builder.eof() || builder.currentOffset >= endOffset) {
            return
        }
        val marker = builder.mark()
        val typeStartOffset = builder.currentOffset
        var angleDepth = 0
        var parenDepth = 0
        while (!builder.eof() && builder.currentOffset < endOffset) {
            val text = builder.tokenText?.uppercase()
            if (angleDepth == 0 && parenDepth == 0 && text in COLUMN_ATTRIBUTE_STARTERS) {
                break
            }
            when (builder.tokenText) {
                "<" -> angleDepth++
                ">" -> if (angleDepth > 0) angleDepth--
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth--
            }
            builder.advanceLexer()
        }
        if (builder.currentOffset > typeStartOffset) {
            marker.done(StarRocksElementTypes.DATA_TYPE)
        } else {
            marker.drop()
        }
        advanceTo(builder, endOffset)
    }

    private fun parseColumnName(builder: PsiBuilder, endOffset: Int) {
        while (!builder.eof() && builder.currentOffset < endOffset) {
            if (isIdentifierToken(builder)) {
                val marker = builder.mark()
                builder.advanceLexer()
                marker.done(StarRocksElementTypes.COLUMN_NAME)
                return
            }
            builder.advanceLexer()
        }
    }

    private fun skipWords(builder: PsiBuilder, endOffset: Int, count: Int) {
        var skipped = 0
        while (!builder.eof() && builder.currentOffset < endOffset && skipped < count) {
            val text = builder.tokenText
            builder.advanceLexer()
            if (text != null && text.firstOrNull()?.isLetter() == true) {
                skipped++
            }
        }
    }

    private fun skipNoise(builder: PsiBuilder, endOffset: Int) {
        while (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText.isNullOrBlank()) {
            builder.advanceLexer()
        }
    }

    private fun advanceTo(builder: PsiBuilder, endOffset: Int) {
        while (!builder.eof() && builder.currentOffset < endOffset) {
            builder.advanceLexer()
        }
    }

    private fun nextWord(builder: PsiBuilder): String? {
        val marker = builder.mark()
        builder.advanceLexer()
        var result: String? = null
        while (!builder.eof()) {
            val text = builder.tokenText
            if (text != null && text.firstOrNull()?.isLetter() == true) {
                result = text.uppercase()
                break
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun nextMeaningfulTokenText(builder: PsiBuilder): String? {
        val marker = builder.mark()
        builder.advanceLexer()
        var result: String? = null
        while (!builder.eof()) {
            val text = builder.tokenText
            if (!text.isNullOrBlank()) {
                result = text
                break
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun nextMeaningfulWord(builder: PsiBuilder): String? {
        val marker = builder.mark()
        builder.advanceLexer()
        var result: String? = null
        while (!builder.eof()) {
            val text = builder.tokenText
            if (text != null && text.firstOrNull()?.isLetter() == true) {
                result = text.uppercase()
                break
            }
            if (!text.isNullOrBlank() && text != "(") {
                break
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun nextNamedWindowWord(builder: PsiBuilder): String? {
        val marker = builder.mark()
        builder.advanceLexer()
        var seenWindowName = false
        var result: String? = null
        while (!builder.eof()) {
            val text = builder.tokenText
            if (text != null && text.firstOrNull()?.let { it == '_' || it == '`' || it.isLetter() } == true) {
                if (!seenWindowName) {
                    seenWindowName = true
                } else {
                    result = text.uppercase()
                    break
                }
            } else if (text == "(" || text == ")" || text == "," || text == ";") {
                break
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun matchingRightParenthesisOffset(builder: PsiBuilder, endOffset: Int): Int {
        val marker = builder.mark()
        var parenDepth = 0
        var result = endOffset
        while (!builder.eof() && builder.currentOffset < endOffset) {
            when (builder.tokenText) {
                "(" -> parenDepth++
                ")" -> {
                    parenDepth--
                    if (parenDepth == 0) {
                        result = builder.currentOffset
                        break
                    }
                }
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun topLevelWordOffset(builder: PsiBuilder, endOffset: Int, word: String): Int? {
        val marker = builder.mark()
        var angleDepth = 0
        var parenDepth = 0
        var result: Int? = null
        while (!builder.eof() && builder.currentOffset < endOffset) {
            when (builder.tokenText) {
                "<" -> angleDepth++
                ">" -> if (angleDepth > 0) angleDepth--
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth--
            }
            if (angleDepth == 0 && parenDepth == 0 && builder.tokenText.equals(word, ignoreCase = true)) {
                result = builder.currentOffset
                break
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun topLevelJoinConditionEndOffset(builder: PsiBuilder, endOffset: Int): Int {
        val marker = builder.mark()
        var parenDepth = 0
        var result = endOffset
        while (!builder.eof() && builder.currentOffset < endOffset) {
            when (builder.tokenText) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth--
            }
            if (parenDepth == 0 && builder.tokenText?.uppercase() in JOIN_CONDITION_END_WORDS) {
                result = builder.currentOffset
                break
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun isItemToken(builder: PsiBuilder): Boolean {
        val text = builder.tokenText
        return text != null && text != "," && text != ")" && !text.isBlank()
    }

    private fun isIdentifierToken(builder: PsiBuilder): Boolean {
        val text = builder.tokenText
        return text != null &&
            !text.isBlank() &&
            text != "." &&
            text.firstOrNull()?.let { it == '_' || it == '`' || it.isLetter() } == true
    }

    private fun isPropertyKeyStart(builder: PsiBuilder): Boolean {
        val text = builder.tokenText
        return text != null && !text.isBlank() && text !in PROPERTY_PAIR_BOUNDARY_TOKENS
    }

    private fun consumePropertyKey(builder: PsiBuilder, endOffset: Int) {
        if (builder.tokenText?.firstOrNull() in PROPERTY_KEY_QUOTE_CHARS) {
            builder.advanceLexer()
            return
        }
        var consumed = false
        while (!builder.eof() && builder.currentOffset < endOffset) {
            val text = builder.tokenText
            if (text == null || text.isBlank() || text in PROPERTY_PAIR_BOUNDARY_TOKENS) {
                break
            }
            builder.advanceLexer()
            consumed = true
        }
        if (!consumed && !builder.eof() && builder.currentOffset < endOffset) {
            builder.advanceLexer()
        }
    }

    private fun consumeQualifiedIdentifier(builder: PsiBuilder, endOffset: Int) {
        var expectPart = true
        while (!builder.eof() && builder.currentOffset < endOffset) {
            val text = builder.tokenText
            when {
                expectPart && isIdentifierToken(builder) -> {
                    builder.advanceLexer()
                    expectPart = false
                }
                !expectPart && text == "." -> {
                    builder.advanceLexer()
                    expectPart = true
                }
                else -> return
            }
        }
    }

    private fun nextTokenAfterQualifiedIdentifier(builder: PsiBuilder, endOffset: Int): String? {
        val marker = builder.mark()
        consumeQualifiedIdentifier(builder, endOffset)
        val result = builder.tokenText
        marker.rollbackTo()
        return result
    }

    private enum class ListMode {
        PLAIN,
        PARENTHESIZED
    }

    private enum class ItemBodyKind {
        EXPRESSION,
        SELECT_ITEM,
        COLUMN_DEFINITION,
        CTE_DEFINITION,
        TABLE_REFERENCE,
        PROPERTY_PAIR,
        WINDOW_DEFINITION
    }

    private data class MeaningfulToken(
        val text: String,
        val startOffset: Int,
        val endOffset: Int
    )

    private data class SelectAliasCandidate(
        val expressionEndOffset: Int,
        val nameStartOffset: Int
    )

    private val QUERY_START_WORDS = setOf("SELECT", "WITH")

    private val TABLE_FACTOR_PREFIX_WORDS = setOf(
        "LATERAL",
        "TABLE"
    )

    private val TABLE_ALIAS_STOP_WORDS = setOf(
        "JOIN",
        "LEFT",
        "RIGHT",
        "INNER",
        "FULL",
        "CROSS",
        "SEMI",
        "ANTI",
        "OUTER",
        "NATURAL",
        "ON",
        "USING",
        "WHERE",
        "GROUP",
        "HAVING",
        "QUALIFY",
        "WINDOW",
        "ORDER",
        "LIMIT",
        "UNION",
        "INTERSECT",
        "EXCEPT",
        "MINUS"
    )

    private val JOIN_CONDITION_END_WORDS = setOf(
        "JOIN",
        "LEFT",
        "RIGHT",
        "INNER",
        "FULL",
        "CROSS",
        "SEMI",
        "ANTI",
        "OUTER",
        "NATURAL"
    )

    private val COLUMN_ATTRIBUTE_STARTERS = setOf(
        "NULL",
        "NOT",
        "DEFAULT",
        "COMMENT",
        "AUTO_INCREMENT",
        "AGGREGATE",
        "KEY"
    )

    private val CREATE_HEADER_SKIP_WORDS = setOf(
        "CREATE",
        "TABLE",
        "MATERIALIZED",
        "VIEW",
        "IF",
        "NOT",
        "EXISTS",
        "OR",
        "REPLACE",
        "TEMPORARY",
        "EXTERNAL"
    )

    private val DML_HEADER_SKIP_WORDS = setOf(
        "INSERT",
        "INTO",
        "OVERWRITE",
        "UPDATE",
        "DELETE",
        "FROM",
        "MERGE",
        "TABLE"
    )

    private val SET_OPERATION_WORDS = setOf("UNION", "INTERSECT", "EXCEPT", "MINUS")

    private val SET_OPERATION_MODIFIERS = setOf("ALL", "DISTINCT")

    private val PROPERTY_PAIR_BOUNDARY_TOKENS = setOf("=", ",", ")", "(")

    private val PROPERTY_KEY_QUOTE_CHARS = setOf('"', '\'')
}

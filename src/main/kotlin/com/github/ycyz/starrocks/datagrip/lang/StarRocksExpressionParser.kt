package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder

object StarRocksExpressionParser {
    fun parseExpressionRange(builder: PsiBuilder, endOffset: Int) {
        var skipNextIdentifier = false
        while (!builder.eof() && builder.currentOffset < endOffset) {
            val word = builder.tokenText
            val upperWord = word?.uppercase()
            if (skipNextIdentifier && word.isNullOrBlank()) {
                builder.advanceLexer()
            } else if (skipNextIdentifier && isIdentifierToken(builder)) {
                builder.advanceLexer()
                skipNextIdentifier = false
            } else if (upperWord == "OVER") {
                parseOverClause(builder, endOffset)
                skipNextIdentifier = false
            } else if (upperWord in PREFIXED_NON_COLUMN_WORDS) {
                builder.advanceLexer()
                skipNextIdentifier = true
            } else if (upperWord in NON_COLUMN_WORDS) {
                builder.advanceLexer()
                skipNextIdentifier = false
            } else if (word == "(" && nextMeaningfulWord(builder) in QUERY_START_WORDS) {
                parseSubqueryExpression(builder, endOffset)
                skipNextIdentifier = false
            } else if (upperWord in SUBQUERY_PREFIX_WORDS && nextTokenText(builder) == "(" && nextMeaningfulWord(builder) in QUERY_START_WORDS) {
                parsePrefixedSubqueryExpression(builder, endOffset)
                skipNextIdentifier = false
            } else if (isIdentifierToken(builder) && hasQualifiedIdentifierTail(builder)) {
                if (nextTokenAfterQualifiedIdentifier(builder, endOffset) == "(") {
                    parseFunctionCall(builder, endOffset)
                } else {
                    parseQualifiedIdentifierReference(builder, endOffset)
                }
                skipNextIdentifier = false
            } else if (isIdentifierToken(builder) && nextTokenText(builder) == "(") {
                if (word.equals("CAST", ignoreCase = true)) {
                    parseCastExpression(builder, endOffset)
                } else {
                    parseFunctionCall(builder, endOffset)
                }
                skipNextIdentifier = false
            } else if (isIdentifierToken(builder)) {
                parseColumnReference(builder)
                skipNextIdentifier = false
            } else {
                builder.advanceLexer()
                skipNextIdentifier = false
            }
        }
    }

    private fun parseSubqueryExpression(builder: PsiBuilder, endOffset: Int) {
        val marker = builder.mark()
        parseParenthesizedQuery(builder, endOffset)
        marker.done(StarRocksElementTypes.SUBQUERY_EXPRESSION)
    }

    private fun parsePrefixedSubqueryExpression(builder: PsiBuilder, endOffset: Int) {
        val marker = builder.mark()
        builder.advanceLexer()
        if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == "(") {
            parseParenthesizedQuery(builder, endOffset)
        }
        marker.done(StarRocksElementTypes.SUBQUERY_EXPRESSION)
    }

    private fun parseParenthesizedQuery(builder: PsiBuilder, endOffset: Int) {
        val closeOffset = matchingRightParenthesisOffset(builder, endOffset)
        builder.advanceLexer()
        StarRocksSegmentParser.parseQueryClauses(builder, closeOffset)
        if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == ")") {
            builder.advanceLexer()
        }
    }

    private fun parseFunctionCall(builder: PsiBuilder, endOffset: Int) {
        val marker = builder.mark()
        consumeQualifiedIdentifier(builder, endOffset)
        if (builder.tokenText == "(") {
            val closeOffset = matchingRightParenthesisOffset(builder, endOffset)
            builder.advanceLexer()
            parseExpressionRange(builder, closeOffset)
            if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == ")") {
                builder.advanceLexer()
            }
        }
        marker.done(StarRocksElementTypes.FUNCTION_CALL)
    }

    private fun parseQualifiedIdentifierReference(builder: PsiBuilder, endOffset: Int) {
        val prefixMarker = builder.mark()
        builder.advanceLexer()
        prefixMarker.done(StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX)
        while (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == ".") {
            builder.advanceLexer()
            if (builder.eof() || builder.currentOffset >= endOffset) {
                return
            }
            if (builder.tokenText == "*") {
                builder.advanceLexer()
                continue
            }
            if (!isIdentifierToken(builder)) {
                return
            }
            if (nextTokenText(builder) == ".") {
                builder.advanceLexer()
                continue
            }
            val columnMarker = builder.mark()
            builder.advanceLexer()
            columnMarker.done(StarRocksElementTypes.COLUMN_REFERENCE_NAME)
        }
    }

    private fun parseColumnReference(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer()
        marker.done(StarRocksElementTypes.COLUMN_REFERENCE_NAME)
    }

    private fun parseOverClause(builder: PsiBuilder, endOffset: Int) {
        builder.advanceLexer()
        skipNoise(builder, endOffset)
        if (builder.eof() || builder.currentOffset >= endOffset) {
            return
        }
        if (builder.tokenText == "(") {
            val closeOffset = matchingRightParenthesisOffset(builder, endOffset)
            builder.advanceLexer()
            parseExpressionRange(builder, closeOffset)
            if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == ")") {
                builder.advanceLexer()
            }
            return
        }
        if (isIdentifierToken(builder)) {
            val marker = builder.mark()
            builder.advanceLexer()
            marker.done(StarRocksElementTypes.WINDOW_REFERENCE_NAME)
        }
    }

    private fun parseCastExpression(builder: PsiBuilder, endOffset: Int) {
        val marker = builder.mark()
        builder.advanceLexer()
        if (builder.tokenText != "(") {
            marker.done(StarRocksElementTypes.FUNCTION_CALL)
            return
        }
        builder.advanceLexer()
        val asOffset = topLevelCastAsOffset(builder, endOffset)
        if (asOffset != null) {
            parseExpressionRange(builder, asOffset)
            if (!builder.eof() && builder.currentOffset == asOffset) {
                builder.advanceLexer()
                parseCastType(builder, endOffset)
            }
        }
        while (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText != ")") {
            builder.advanceLexer()
        }
        if (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.FUNCTION_CALL)
    }

    private fun parseCastType(builder: PsiBuilder, endOffset: Int) {
        skipNoise(builder, endOffset)
        if (builder.eof() || builder.currentOffset >= endOffset || builder.tokenText == ")") {
            return
        }
        val marker = builder.mark()
        var angleDepth = 0
        var parenDepth = 0
        while (!builder.eof() && builder.currentOffset < endOffset) {
            when (builder.tokenText) {
                "<" -> angleDepth++
                ">" -> if (angleDepth > 0) angleDepth--
                "(" -> parenDepth++
                ")" -> if (angleDepth == 0 && parenDepth == 0) break else if (parenDepth > 0) parenDepth--
                "," -> if (angleDepth == 0 && parenDepth == 0) break
            }
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.CAST_TYPE)
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

    private fun topLevelCastAsOffset(builder: PsiBuilder, endOffset: Int): Int? {
        val marker = builder.mark()
        var parenDepth = 1
        var result: Int? = null
        while (!builder.eof() && builder.currentOffset < endOffset && parenDepth > 0) {
            when (builder.tokenText?.uppercase()) {
                "(" -> parenDepth++
                ")" -> parenDepth--
                "AS" -> if (parenDepth == 1) {
                    result = builder.currentOffset
                    break
                }
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun nextTokenText(builder: PsiBuilder): String? {
        val marker = builder.mark()
        builder.advanceLexer()
        val result = builder.tokenText
        marker.rollbackTo()
        return result
    }

    private fun hasQualifiedIdentifierTail(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        var result = false
        if (isIdentifierToken(builder)) {
            builder.advanceLexer()
            if (!builder.eof() && builder.tokenText == ".") {
                builder.advanceLexer()
                result = !builder.eof() && (isIdentifierToken(builder) || builder.tokenText == "*")
            }
        }
        marker.rollbackTo()
        return result
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

    private fun skipNoise(builder: PsiBuilder, endOffset: Int) {
        while (!builder.eof() && builder.currentOffset < endOffset && builder.tokenText.isNullOrBlank()) {
            builder.advanceLexer()
        }
    }

    private fun isIdentifierToken(builder: PsiBuilder): Boolean {
        val text = builder.tokenText
        return text != null &&
            !text.isBlank() &&
            text != "." &&
            text.firstOrNull()?.let { it == '_' || it == '`' || it.isLetter() } == true
    }

    private val QUERY_START_WORDS = setOf("SELECT", "WITH")
    private val SUBQUERY_PREFIX_WORDS = setOf("EXISTS", "IN")
    private val PREFIXED_NON_COLUMN_WORDS = setOf("AS")
    private val NON_COLUMN_WORDS = setOf(
        "ALL",
        "AND",
        "ASC",
        "BETWEEN",
        "BY",
        "CASE",
        "CURRENT",
        "DESC",
        "DISTINCT",
        "ELSE",
        "END",
        "EXISTS",
        "FALSE",
        "FIRST",
        "FOLLOWING",
        "IN",
        "INTERVAL",
        "IS",
        "LAST",
        "LIKE",
        "NOT",
        "NULL",
        "NULLS",
        "OR",
        "ORDER",
        "PARTITION",
        "PRECEDING",
        "RANGE",
        "REGEXP",
        "RLIKE",
        "ROW",
        "ROWS",
        "THEN",
        "TRUE",
        "UNBOUNDED",
        "WHEN"
    )

}

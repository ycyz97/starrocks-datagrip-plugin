package com.github.ycyz.starrocks.datagrip.lang

import com.github.ycyz.starrocks.datagrip.database.StarRocksTypeSystem
import com.intellij.lang.PsiBuilder
import com.intellij.sql.psi.SqlCompositeElementTypes

object StarRocksExpressionParsing {
    @JvmStatic
    fun value_expression(builder: PsiBuilder, level: Int): Boolean {
        return value_expression(builder, level, EXPRESSION_BOUNDARIES)
    }

    @JvmStatic
    fun evaluable_expression(builder: PsiBuilder, level: Int): Boolean {
        return value_expression(builder, level)
    }

    @JvmStatic
    fun value_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        StarRocksParsingUtil.skipNoise(builder)
        if (isBoundary(builder, stopWords)) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        if (!or_expression(builder, level + 1, stopWords)) {
            marker.rollbackTo()
            return false
        }
        return if (builder.currentOffset > before) {
            marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun analytic_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "OVER")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        when {
            StarRocksParsingUtil.isIdentifier(builder) -> {
                val name = builder.mark()
                builder.advanceLexer()
                name.done(StarRocksElementTypes.WINDOW_REFERENCE_NAME)
            }
            builder.tokenText == "(" -> {
                builder.advanceLexer()
                consumeWindowSpecification(builder, level + 1)
                if (builder.tokenText == ")") {
                    builder.advanceLexer()
                }
            }
            else -> return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.ANALYTIC_CLAUSE)
        return true
    }

    @JvmStatic
    fun cast_type(builder: PsiBuilder, level: Int): Boolean {
        val word = StarRocksParsingUtil.word(builder)
        if (word !in DATA_TYPE_WORDS) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        consumeTypeElement(builder)
        return if (builder.currentOffset > before) {
            marker.done(StarRocksElementTypes.CAST_TYPE)
            true
        } else {
            marker.drop()
            false
        }
    }

    @JvmStatic
    fun cast_expression(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "CAST")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (builder.tokenText != "(") {
            return marker.rollbackFalse()
        }
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        value_expression(builder, level + 1, setOf("AS"))
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "AS")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!cast_type(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(SqlCompositeElementTypes.SQL_FUNCTION_CALL)
        return true
    }

    private fun or_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        return binaryExpression(builder, level, stopWords, setOf("OR"), ::and_expression)
    }

    private fun and_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        return binaryExpression(builder, level, stopWords, setOf("AND"), ::not_expression)
    }

    private fun not_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "NOT")) {
            return comparison_expression(builder, level + 1, stopWords)
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        comparison_expression(builder, level + 1, stopWords)
        marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
        return true
    }

    private fun comparison_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        val marker = builder.mark()
        val before = builder.currentOffset
        if (!additive_expression(builder, level + 1, stopWords)) {
            marker.rollbackTo()
            return false
        }
        var parsedOperator = false
        while (!builder.eof() && !isBoundary(builder, stopWords)) {
            StarRocksParsingUtil.skipNoise(builder)
            when {
                StarRocksParsingUtil.word(builder) == "IS" -> {
                    builder.advanceLexer()
                    StarRocksParsingUtil.skipNoise(builder)
                    StarRocksParsingUtil.consumeWord(builder, "NOT")
                    StarRocksParsingUtil.skipNoise(builder)
                    additive_expression(builder, level + 1, stopWords)
                    parsedOperator = true
                }
                StarRocksParsingUtil.word(builder) == "NOT" && nextMeaningfulWordIn(builder, NEGATED_COMPARISON_WORDS) -> {
                    builder.advanceLexer()
                    StarRocksParsingUtil.skipNoise(builder)
                    consumeComparisonTail(builder, level + 1, stopWords)
                    parsedOperator = true
                }
                StarRocksParsingUtil.word(builder) in COMPARISON_WORDS || builder.tokenText in COMPARISON_SYMBOLS -> {
                    consumeComparisonTail(builder, level + 1, stopWords)
                    parsedOperator = true
                }
                else -> break
            }
        }
        return if (builder.currentOffset > before) {
            if (parsedOperator) {
                marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
            } else {
                marker.drop()
            }
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun consumeComparisonTail(builder: PsiBuilder, level: Int, stopWords: Set<String>) {
        val operator = StarRocksParsingUtil.word(builder) ?: builder.tokenText
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        when (operator) {
            "BETWEEN" -> {
                additive_expression(builder, level + 1, stopWords + "AND")
                StarRocksParsingUtil.skipNoise(builder)
                StarRocksParsingUtil.consumeWord(builder, "AND")
                StarRocksParsingUtil.skipNoise(builder)
                additive_expression(builder, level + 1, stopWords)
            }
            "IN" -> in_list_or_subquery(builder, level + 1, stopWords)
            else -> additive_expression(builder, level + 1, stopWords)
        }
    }

    private fun additive_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        return binaryExpression(builder, level, stopWords, ADDITIVE_OPERATORS, ::multiplicative_expression)
    }

    private fun multiplicative_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        return binaryExpression(builder, level, stopWords, MULTIPLICATIVE_OPERATORS, ::unary_expression)
    }

    private fun unary_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        if (builder.tokenText !in UNARY_OPERATORS && StarRocksParsingUtil.word(builder) !in UNARY_WORDS) {
            return postfix_expression(builder, level + 1, stopWords)
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        postfix_expression(builder, level + 1, stopWords)
        marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
        return true
    }

    private fun postfix_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        if (!primary_expression(builder, level + 1, stopWords)) {
            return false
        }
        while (!builder.eof() && !isBoundary(builder, stopWords)) {
            val before = builder.currentOffset
            when {
                analytic_clause(builder, level + 1) -> Unit
                array_access_tail(builder, level + 1) -> Unit
                field_access_tail(builder) -> Unit
                else -> return true
            }
            if (builder.currentOffset == before) {
                return true
            }
        }
        return true
    }

    private fun primary_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        StarRocksParsingUtil.skipNoise(builder)
        if (isBoundary(builder, stopWords)) {
            return false
        }
        return cast_expression(builder, level + 1) ||
            case_expression(builder, level + 1, stopWords) ||
            exists_expression(builder, level + 1, stopWords) ||
            interval_expression(builder, level + 1, stopWords) ||
            typed_literal_expression(builder) ||
            function_call(builder, level + 1, stopWords) ||
            parenthesized_expression(builder, level + 1, stopWords) ||
            StarRocksDmlParsing.table_function_call(builder, level + 1) ||
            StarRocksDmlParsing.column_reference(builder, level + 1) ||
            literal_expression(builder)
    }

    private fun binaryExpression(
        builder: PsiBuilder,
        level: Int,
        stopWords: Set<String>,
        operators: Set<String>,
        next: (PsiBuilder, Int, Set<String>) -> Boolean
    ): Boolean {
        val marker = builder.mark()
        val before = builder.currentOffset
        if (!next(builder, level + 1, stopWords)) {
            marker.rollbackTo()
            return false
        }
        var parsedOperator = false
        while (!builder.eof() && !isBoundary(builder, stopWords)) {
            StarRocksParsingUtil.skipNoise(builder)
            val operator = StarRocksParsingUtil.word(builder) ?: builder.tokenText
            if (operator !in operators) {
                break
            }
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
            if (!next(builder, level + 1, stopWords)) {
                break
            }
            parsedOperator = true
        }
        return if (builder.currentOffset > before) {
            if (parsedOperator) {
                marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
            } else {
                marker.drop()
            }
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun function_call(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        if (!looksLikeFunctionCall(builder)) {
            return false
        }
        val marker = builder.mark()
        if (!consumeQualifiedName(builder)) {
            marker.rollbackTo()
            return false
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (builder.tokenText != "(") {
            marker.rollbackTo()
            return false
        }
        parseArgumentList(builder, level + 1, stopWords)
        marker.done(SqlCompositeElementTypes.SQL_FUNCTION_CALL)
        return true
    }

    private fun exists_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "EXISTS")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksDmlParsing.parenthesized_query_expression(builder, level + 1)) {
            parenthesized_expression(builder, level + 1, stopWords)
        }
        marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
        return true
    }

    private fun interval_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "INTERVAL")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!value_expression(builder, level + 1, stopWords + INTERVAL_UNITS)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.word(builder) in INTERVAL_UNITS) {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
        return true
    }

    private fun typed_literal_expression(builder: PsiBuilder): Boolean {
        if (StarRocksParsingUtil.word(builder) !in TYPED_LITERAL_PREFIXES) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        return if (isLiteralToken(builder.tokenText)) {
            builder.advanceLexer()
            marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
            true
        } else {
            marker.rollbackFalse()
        }
    }

    private fun parseArgumentList(builder: PsiBuilder, level: Int, stopWords: Set<String>) {
        if (builder.tokenText != "(") {
            return
        }
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        while (!builder.eof() && builder.tokenText != ")") {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                StarRocksDmlParsing.parenthesized_query_expression(builder, level + 1) -> Unit
                value_expression(builder, level + 1, stopWords + setOf(",", ")")) -> Unit
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
    }

    private fun parenthesized_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        if (StarRocksDmlParsing.parenthesized_query_expression(builder, level + 1)) {
            return true
        }
        if (builder.tokenText != "(") {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        while (!builder.eof() && builder.tokenText != ")") {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                value_expression(builder, level + 1, stopWords + setOf(",", ")")) -> Unit
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
        return true
    }

    private fun case_expression(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "CASE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        while (!builder.eof() && StarRocksParsingUtil.word(builder) != "END" && !isBoundary(builder, stopWords)) {
            if (StarRocksParsingUtil.word(builder) in CASE_KEYWORDS) {
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                continue
            }
            if (!value_expression(builder, level + 1, stopWords + CASE_KEYWORDS + "END")) {
                builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        StarRocksParsingUtil.consumeWord(builder, "END")
        marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
        return true
    }

    private fun in_list_or_subquery(builder: PsiBuilder, level: Int, stopWords: Set<String>): Boolean {
        if (StarRocksDmlParsing.parenthesized_query_expression(builder, level + 1)) {
            return true
        }
        if (builder.tokenText != "(") {
            return additive_expression(builder, level + 1, stopWords)
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        while (!builder.eof() && builder.tokenText != ")") {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                value_expression(builder, level + 1, stopWords + setOf(",", ")")) -> Unit
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
        return true
    }

    private fun array_access_tail(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenText != "[") {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        while (!builder.eof() && builder.tokenText != "]") {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                value_expression(builder, level + 1, setOf(",", "]")) -> Unit
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        if (builder.tokenText == "]") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
        return true
    }

    private fun field_access_tail(builder: PsiBuilder): Boolean {
        if (builder.tokenText != ".") {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.isIdentifier(builder)) {
            marker.rollbackTo()
            return false
        }
        val field = builder.mark()
        builder.advanceLexer()
        field.done(StarRocksElementTypes.COLUMN_REFERENCE_NAME)
        marker.drop()
        return true
    }

    private fun literal_expression(builder: PsiBuilder): Boolean {
        if (!isLiteralToken(builder.tokenText)) {
            return false
        }
        builder.advanceLexer()
        return true
    }

    private fun isLiteralToken(text: String?): Boolean {
        if (text == null || text.isBlank() || text == ";" || text == ")" || text == ",") {
            return false
        }
        val upper = text.uppercase()
        return upper in KEYWORD_LITERALS ||
            text.startsWith("'") ||
            text.startsWith("\"") ||
            text.startsWith("@") ||
            text.startsWith(":") ||
            text.startsWith("$") ||
            text == "?" ||
            text.firstOrNull()?.isDigit() == true
    }

    private fun consumeWindowSpecification(builder: PsiBuilder, level: Int) {
        var parenDepth = 0
        while (!builder.eof()) {
            val text = builder.tokenText
            if (parenDepth == 0 && text == ")") {
                return
            }
            when {
                text == "(" -> {
                    parenDepth++
                    builder.advanceLexer()
                }
                text == ")" && parenDepth > 0 -> {
                    parenDepth--
                    builder.advanceLexer()
                }
                value_expression(builder, level + 1, WINDOW_SPEC_BOUNDARIES) -> Unit
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
    }

    private fun consumeTypeElement(builder: PsiBuilder) {
        var parenDepth = 0
        var angleDepth = 0
        while (!builder.eof()) {
            val text = builder.tokenText
            val word = StarRocksParsingUtil.word(builder)
            if (parenDepth == 0 && angleDepth == 0 && (text == "," || text == ")" || word in TYPE_TRAIL_BOUNDARIES)) {
                return
            }
            when (text) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth-- else return
                "<" -> angleDepth++
                ">" -> if (angleDepth > 0) angleDepth-- else return
            }
            builder.advanceLexer()
        }
    }

    private fun consumeQualifiedName(builder: PsiBuilder): Boolean {
        var consumed = false
        var expectName = true
        while (!builder.eof()) {
            when {
                expectName && StarRocksParsingUtil.isIdentifier(builder) -> {
                    builder.advanceLexer()
                    consumed = true
                    expectName = false
                }
                !expectName && builder.tokenText == "." -> {
                    builder.advanceLexer()
                    expectName = true
                }
                else -> return consumed && !expectName
            }
        }
        return consumed && !expectName
    }

    private fun looksLikeFunctionCall(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        try {
            if (!consumeQualifiedName(builder)) {
                return false
            }
            StarRocksParsingUtil.skipNoise(builder)
            return builder.tokenText == "("
        } finally {
            marker.rollbackTo()
        }
    }

    private fun nextMeaningfulWordIn(builder: PsiBuilder, words: Set<String>): Boolean {
        val marker = builder.mark()
        try {
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
            return StarRocksParsingUtil.word(builder) in words
        } finally {
            marker.rollbackTo()
        }
    }

    private fun isBoundary(builder: PsiBuilder, stopWords: Set<String>): Boolean {
        val text = builder.tokenText
        val word = StarRocksParsingUtil.word(builder)
        return text == ";" || text in stopWords || word in stopWords
    }

    private fun PsiBuilder.Marker.rollbackFalse(): Boolean {
        rollbackTo()
        return false
    }

    private val EXPRESSION_BOUNDARIES = setOf(
        "FROM",
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

    private val DATA_TYPE_WORDS: Set<String> = buildSet {
        addAll(StarRocksTypeSystem.SCALAR_TYPES)
        addAll(StarRocksTypeSystem.COMPLEX_TYPES)
        addAll(listOf("BOOL", "INTEGER", "DECIMAL", "DECIMALV2", "VARCHAR2", "TEXT"))
    }

    private val COMPARISON_SYMBOLS = setOf("=", "<", ">", "<=", ">=", "<>", "!=", "<=>")

    private val COMPARISON_WORDS = setOf(
        "BETWEEN",
        "IN",
        "LIKE",
        "REGEXP",
        "RLIKE"
    )

    private val NEGATED_COMPARISON_WORDS = setOf("BETWEEN", "IN", "LIKE", "REGEXP", "RLIKE")

    private val ADDITIVE_OPERATORS = setOf("+", "-", "||")

    private val MULTIPLICATIVE_OPERATORS = setOf("*", "/", "%", "DIV", "MOD")

    private val UNARY_OPERATORS = setOf("+", "-", "!", "~")

    private val UNARY_WORDS = setOf("BINARY", "NOT")

    private val CASE_KEYWORDS = setOf("WHEN", "THEN", "ELSE")

    private val KEYWORD_LITERALS = setOf(
        "FALSE",
        "NULL",
        "TRUE",
        "CURRENT_DATE",
        "CURRENT_TIME",
        "CURRENT_TIMESTAMP",
        "LOCALTIME",
        "LOCALTIMESTAMP"
    )

    private val TYPED_LITERAL_PREFIXES = setOf("DATE", "DATETIME", "TIME", "TIMESTAMP")

    private val INTERVAL_UNITS = setOf(
        "MICROSECOND",
        "MILLISECOND",
        "SECOND",
        "SECONDS",
        "MINUTE",
        "MINUTES",
        "HOUR",
        "HOURS",
        "DAY",
        "DAYS",
        "WEEK",
        "MONTH",
        "QUARTER",
        "YEAR"
    )

    private val WINDOW_SPEC_BOUNDARIES = setOf(
        ")",
        "PARTITION",
        "ORDER",
        "ROWS",
        "RANGE",
        "GROUPS",
        "BETWEEN",
        "AND",
        "UNBOUNDED",
        "PRECEDING",
        "FOLLOWING",
        "CURRENT",
        "ROW"
    )

    private val TYPE_TRAIL_BOUNDARIES = setOf(
        "NULL",
        "NOT",
        "DEFAULT",
        "COMMENT",
        "AS",
        "WHEN",
        "THEN",
        "ELSE",
        "FROM",
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
}

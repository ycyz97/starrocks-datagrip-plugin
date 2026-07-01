package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder
import com.intellij.sql.psi.SqlCompositeElementTypes

object StarRocksDmlParsing {
    @JvmStatic
    fun qualify_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "QUALIFY")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!builder.eof()) {
            val expression = builder.mark()
            StarRocksExpressionParsing.value_expression(builder, level + 1, QUALIFY_BOUNDARIES)
            expression.done(StarRocksElementTypes.QUALIFY_EXPRESSION)
        }
        marker.done(SqlCompositeElementTypes.SQL_QUALIFY_CLAUSE)
        return true
    }

    @JvmStatic
    fun table_function_call(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "UNNEST")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (builder.tokenText == "(") {
            builder.advanceLexer()
            StarRocksParsingUtil.consumeBalancedTail(builder)
            if (builder.tokenText == ")") {
                builder.advanceLexer()
            }
        }
        marker.done(SqlCompositeElementTypes.SQL_FUNCTION_CALL)
        parseTableFunctionAlias(builder)
        return true
    }

    @JvmStatic
    fun values_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "VALUES")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        while (!builder.eof() && builder.tokenText != ";") {
            val word = StarRocksParsingUtil.word(builder)
            if (word in VALUES_BOUNDARIES) {
                break
            }
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                values_row(builder, level + 1) -> continue
                else -> builder.advanceLexer()
            }
        }
        marker.done(StarRocksElementTypes.VALUES_CLAUSE)
        return true
    }

    @JvmStatic
    fun values_row(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.consumeBalancedTail(builder)
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.VALUES_ROW)
        return true
    }

    @JvmStatic
    fun top_query_expression(builder: PsiBuilder, level: Int): Boolean {
        return query_expression_body(builder, level, stopAtRightParen = false)
    }

    private fun query_expression_body(builder: PsiBuilder, level: Int, stopAtRightParen: Boolean): Boolean {
        val marker = builder.mark()
        val before = builder.currentOffset
        var parenDepth = 0
        while (!builder.eof() && builder.tokenText != ";" && !(stopAtRightParen && parenDepth == 0 && builder.tokenText == ")")) {
            when {
                parenDepth == 0 && with_clause(builder, level + 1) -> continue
                parenDepth == 0 && select_clause(builder, level + 1) -> continue
                parenDepth == 0 && values_clause(builder, level + 1) -> continue
                parenDepth == 0 && where_clause(builder, level + 1) -> continue
                parenDepth == 0 && group_by_clause(builder, level + 1) -> continue
                parenDepth == 0 && having_clause(builder, level + 1) -> continue
                parenDepth == 0 && order_by_clause(builder, level + 1) -> continue
                parenDepth == 0 && limit_clause(builder, level + 1) -> continue
                parenDepth == 0 && window_clause(builder, level + 1) -> continue
                qualify_clause(builder, level + 1) -> continue
                table_function_call(builder, level + 1) -> continue
                parenDepth == 0 && table_reference(builder, level + 1) -> continue
                parenDepth == 0 && column_reference(builder, level + 1) -> continue
                else -> {
                    parenDepth = nextParenDepth(builder.tokenText, parenDepth)
                    builder.advanceLexer()
                }
            }
        }
        return if (builder.currentOffset > before) {
            marker.done(SqlCompositeElementTypes.SQL_SELECT_STATEMENT)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun insert_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "INSERT")) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        var parenDepth = 0
        while (!builder.eof() && builder.tokenText != ";") {
            when {
                parenDepth == 0 && with_clause(builder, level + 1) -> continue
                parenDepth == 0 && select_clause(builder, level + 1) -> continue
                parenDepth == 0 && values_clause(builder, level + 1) -> continue
                parenDepth == 0 && where_clause(builder, level + 1) -> continue
                parenDepth == 0 && group_by_clause(builder, level + 1) -> continue
                parenDepth == 0 && having_clause(builder, level + 1) -> continue
                parenDepth == 0 && order_by_clause(builder, level + 1) -> continue
                parenDepth == 0 && limit_clause(builder, level + 1) -> continue
                parenDepth == 0 && window_clause(builder, level + 1) -> continue
                qualify_clause(builder, level + 1) -> continue
                table_function_call(builder, level + 1) -> continue
                parenDepth == 0 && table_reference(builder, level + 1) -> continue
                parenDepth == 0 && column_reference(builder, level + 1) -> continue
                else -> {
                    parenDepth = nextParenDepth(builder.tokenText, parenDepth)
                    builder.advanceLexer()
                }
            }
        }
        return if (builder.currentOffset > before) {
            marker.done(SqlCompositeElementTypes.SQL_INSERT_STATEMENT)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun with_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "WITH")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "RECURSIVE")
        var parsed = false
        while (!builder.eof() && !withClauseBoundary(builder)) {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                named_query_definition(builder, level + 1) -> parsed = true
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        return if (parsed) {
            marker.done(StarRocksElementTypes.WITH_CLAUSE)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun named_query_definition(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.isIdentifier(builder) || StarRocksParsingUtil.word(builder) in QUERY_STARTERS) {
            return false
        }
        val marker = builder.mark()
        val name = builder.mark()
        builder.advanceLexer()
        name.done(StarRocksElementTypes.CTE_NAME)
        StarRocksParsingUtil.skipNoise(builder)
        cte_column_list(builder, level + 1)
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "AS")) {
            marker.rollbackTo()
            return false
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!cte_query(builder, level + 1)) {
            marker.rollbackTo()
            return false
        }
        marker.done(StarRocksElementTypes.CTE_DEFINITION)
        return true
    }

    @JvmStatic
    fun cte_column_list(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        while (!builder.eof() && builder.tokenText != ")") {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                StarRocksParsingUtil.isIdentifier(builder) -> {
                    val column = builder.mark()
                    builder.advanceLexer()
                    column.done(StarRocksElementTypes.CTE_COLUMN_NAME)
                }
                else -> builder.advanceLexer()
            }
        }
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.CTE_COLUMN_LIST)
        return true
    }

    @JvmStatic
    fun cte_query(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        query_expression_body(builder, level + 1, stopAtRightParen = true)
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.CTE_QUERY)
        return true
    }

    @JvmStatic
    fun select_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "SELECT")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        consumeSelectModifiers(builder)
        select_target_list(builder, level + 1)
        marker.done(StarRocksElementTypes.SELECT_CLAUSE)
        return true
    }

    @JvmStatic
    fun select_target_list(builder: PsiBuilder, level: Int): Boolean {
        var parsed = false
        while (!builder.eof() && !selectClauseBoundary(builder)) {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                select_target(builder, level + 1) -> parsed = true
                else -> builder.advanceLexer()
            }
        }
        return parsed
    }

    @JvmStatic
    fun select_target(builder: PsiBuilder, level: Int): Boolean {
        if (builder.eof() || selectItemBoundary(builder) || builder.tokenText == ",") {
            return false
        }
        val plan = analyzeSelectItem(builder)
        val marker = builder.mark()
        val before = builder.currentOffset
        var parenDepth = 0
        while (!builder.eof() && !selectItemBoundary(builder, parenDepth) && !(parenDepth == 0 && builder.tokenText == ",")) {
            when {
                builder.currentOffset == plan.explicitAsOffset -> {
                    builder.advanceLexer()
                    StarRocksParsingUtil.skipNoise(builder)
                }
                builder.currentOffset == plan.aliasOffset -> parseSelectAlias(builder)
                builder.tokenText == "(" && parenthesized_query_expression(builder, level + 1) -> continue
                window_reference(builder, level + 1) -> continue
                column_reference(builder, level + 1) -> continue
                else -> {
                    parenDepth = nextParenDepth(builder.tokenText, parenDepth)
                    builder.advanceLexer()
                }
            }
        }
        return if (builder.currentOffset > before) {
            marker.done(StarRocksElementTypes.SELECT_ITEM)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun select_item(builder: PsiBuilder, level: Int): Boolean {
        return select_target(builder, level)
    }

    @JvmStatic
    fun where_clause(builder: PsiBuilder, level: Int): Boolean {
        return expression_clause(
            builder,
            level,
            "WHERE",
            StarRocksElementTypes.WHERE_CLAUSE,
            StarRocksElementTypes.PREDICATE_EXPRESSION,
            WHERE_BOUNDARIES
        )
    }

    @JvmStatic
    fun group_by_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "GROUP")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "BY")) {
            marker.rollbackTo()
            return false
        }
        while (!builder.eof() && !topLevelBoundary(builder, GROUP_BY_BOUNDARIES)) {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                grouping_item(builder, level + 1) -> continue
                else -> builder.advanceLexer()
            }
        }
        marker.done(StarRocksElementTypes.GROUP_BY_CLAUSE)
        return true
    }

    @JvmStatic
    fun grouping_item(builder: PsiBuilder, level: Int): Boolean {
        if (builder.eof() || builder.tokenText == "," || topLevelBoundary(builder, GROUP_BY_BOUNDARIES)) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        consumeExpressionItems(builder, level + 1, GROUP_BY_BOUNDARIES, stopAtComma = true)
        return if (builder.currentOffset > before) {
            marker.done(StarRocksElementTypes.GROUPING_ITEM)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun having_clause(builder: PsiBuilder, level: Int): Boolean {
        return expression_clause(
            builder,
            level,
            "HAVING",
            StarRocksElementTypes.HAVING_CLAUSE,
            StarRocksElementTypes.PREDICATE_EXPRESSION,
            HAVING_BOUNDARIES
        )
    }

    @JvmStatic
    fun order_by_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "ORDER")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "BY")) {
            marker.rollbackTo()
            return false
        }
        order_by_expression_list(builder, level + 1)
        marker.done(StarRocksElementTypes.ORDER_BY_CLAUSE)
        return true
    }

    @JvmStatic
    fun order_by_expression_list(builder: PsiBuilder, level: Int): Boolean {
        var parsed = false
        while (!builder.eof() && !orderByBoundary(builder)) {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                order_expression(builder, level + 1) -> parsed = true
                else -> builder.advanceLexer()
            }
        }
        return parsed
    }

    @JvmStatic
    fun order_expression(builder: PsiBuilder, level: Int): Boolean {
        if (builder.eof() || orderByItemBoundary(builder) || builder.tokenText == ",") {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        var parenDepth = 0
        while (!builder.eof() && !orderByItemBoundary(builder, parenDepth) && !(parenDepth == 0 && builder.tokenText == ",")) {
            when {
                column_reference(builder, level + 1) -> continue
                else -> {
                    parenDepth = nextParenDepth(builder.tokenText, parenDepth)
                    builder.advanceLexer()
                }
            }
        }
        return if (builder.currentOffset > before) {
            marker.done(StarRocksElementTypes.ORDERING_ITEM)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun ordering_item(builder: PsiBuilder, level: Int): Boolean {
        return order_expression(builder, level)
    }

    @JvmStatic
    fun window_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "WINDOW")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        var parsed = false
        while (!builder.eof() && !topLevelBoundary(builder, WINDOW_BOUNDARIES)) {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                window_definition(builder, level + 1) -> parsed = true
                else -> builder.advanceLexer()
            }
        }
        return if (parsed) {
            marker.done(StarRocksElementTypes.WINDOW_CLAUSE)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun window_definition(builder: PsiBuilder, level: Int): Boolean {
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.isIdentifier(builder)) {
            return false
        }
        val marker = builder.mark()
        val name = builder.mark()
        builder.advanceLexer()
        name.done(StarRocksElementTypes.WINDOW_NAME)
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "AS")) {
            marker.rollbackTo()
            return false
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (builder.tokenText == "(") {
            builder.advanceLexer()
            consumeExpressionItems(builder, level + 1, emptySet(), stopAtComma = false, stopAtRightParen = true)
            if (builder.tokenText == ")") {
                builder.advanceLexer()
            }
        }
        marker.done(StarRocksElementTypes.WINDOW_DEFINITION)
        return true
    }

    @JvmStatic
    fun limit_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "LIMIT")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val expression = builder.mark()
        val before = builder.currentOffset
        while (!builder.eof() && !limitBoundary(builder)) {
            builder.advanceLexer()
        }
        if (builder.currentOffset > before) {
            expression.done(StarRocksElementTypes.LIMIT_EXPRESSION)
        } else {
            expression.drop()
        }
        marker.done(StarRocksElementTypes.LIMIT_CLAUSE)
        return true
    }

    private val QUALIFY_BOUNDARIES = setOf(
        "WINDOW",
        "ORDER",
        "LIMIT",
        "UNION",
        "INTERSECT",
        "EXCEPT",
        "MINUS"
    )

    private val VALUES_BOUNDARIES = setOf(
        "ORDER",
        "LIMIT",
        "UNION",
        "INTERSECT",
        "EXCEPT",
        "MINUS"
    )

    private val SELECT_CLAUSE_BOUNDARIES = setOf(
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

    private val WHERE_BOUNDARIES = setOf(
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

    private val GROUP_BY_BOUNDARIES = setOf(
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

    private val HAVING_BOUNDARIES = setOf(
        "QUALIFY",
        "WINDOW",
        "ORDER",
        "LIMIT",
        "UNION",
        "INTERSECT",
        "EXCEPT",
        "MINUS"
    )

    private val ORDER_BY_BOUNDARIES = setOf(
        "LIMIT",
        "UNION",
        "INTERSECT",
        "EXCEPT",
        "MINUS"
    )

    private val WINDOW_BOUNDARIES = setOf(
        "ORDER",
        "LIMIT",
        "UNION",
        "INTERSECT",
        "EXCEPT",
        "MINUS"
    )

    private val LIMIT_BOUNDARIES = setOf(
        "UNION",
        "INTERSECT",
        "EXCEPT",
        "MINUS"
    )

    private val QUERY_STARTERS = setOf("SELECT", "WITH", "VALUES")

    private fun expression_clause(
        builder: PsiBuilder,
        level: Int,
        keyword: String,
        clauseType: com.intellij.psi.tree.IElementType,
        expressionType: com.intellij.psi.tree.IElementType,
        boundaries: Set<String>
    ): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, keyword)) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val expression = builder.mark()
        val before = builder.currentOffset
        consumeExpressionItems(builder, level + 1, boundaries, stopAtComma = false)
        if (builder.currentOffset > before) {
            expression.done(expressionType)
        } else {
            expression.drop()
        }
        marker.done(clauseType)
        return true
    }

    private fun consumeExpressionItems(
        builder: PsiBuilder,
        level: Int,
        boundaries: Set<String>,
        stopAtComma: Boolean,
        stopAtRightParen: Boolean = false
    ) {
        var parenDepth = 0
        while (!builder.eof()) {
            val atBoundary = parenDepth == 0 &&
                (builder.tokenText == ";" ||
                    (stopAtComma && builder.tokenText == ",") ||
                    builder.tokenText == ")" ||
                    StarRocksParsingUtil.word(builder) in boundaries)
            if (atBoundary) {
                return
            }
            when {
                builder.tokenText == "(" && parenthesized_query_expression(builder, level + 1) -> continue
                window_reference(builder, level + 1) -> continue
                column_reference(builder, level + 1) -> continue
                else -> {
                    parenDepth = nextParenDepth(builder.tokenText, parenDepth)
                    builder.advanceLexer()
                }
            }
        }
    }

    @JvmStatic
    fun parenthesized_query_expression(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenText != "(" || !nextMeaningfulWordIsQueryStarter(builder)) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        query_expression_body(builder, level + 1, stopAtRightParen = true)
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.SUBQUERY_EXPRESSION)
        return true
    }

    private fun parseTableFunctionAlias(builder: PsiBuilder) {
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "AS")
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.word(builder) in TABLE_ALIAS_BOUNDARIES || !StarRocksParsingUtil.isIdentifier(builder)) {
            return
        }
        val alias = builder.mark()
        builder.advanceLexer()
        alias.done(StarRocksElementTypes.TABLE_ALIAS)
        StarRocksParsingUtil.skipNoise(builder)
        parseTableFunctionAliasColumnList(builder)
    }

    private fun table_reference(builder: PsiBuilder, level: Int): Boolean {
        val word = StarRocksParsingUtil.word(builder)
        if (word !in TABLE_REFERENCE_STARTERS) {
            return false
        }
        val marker = builder.mark()
        val clause = if (word == "FROM") builder.mark() else null
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val derivedTable = builder.mark()
        if (parenthesized_query_expression(builder, level + 1)) {
            parseTableAlias(builder)
            derivedTable.done(StarRocksElementTypes.TABLE_REFERENCE)
            clause?.done(StarRocksElementTypes.FROM_CLAUSE)
            marker.drop()
            return true
        }
        derivedTable.drop()
        if (StarRocksParsingUtil.word(builder) == "UNNEST" || !StarRocksParsingUtil.isIdentifier(builder)) {
            clause?.drop()
            marker.rollbackTo()
            return false
        }
        val table = builder.mark()
        val name = builder.mark()
        val parsed = StarRocksParsingUtil.consumeQualifiedIdentifier(builder)
        if (!parsed) {
            name.rollbackTo()
            table.rollbackTo()
            clause?.drop()
            marker.rollbackTo()
            return false
        }
        name.done(StarRocksElementTypes.TABLE_REFERENCE_NAME)
        parseTableAlias(builder)
        table.done(StarRocksElementTypes.TABLE_REFERENCE)
        clause?.done(StarRocksElementTypes.FROM_CLAUSE)
        marker.drop()
        return true
    }

    private fun withClauseBoundary(builder: PsiBuilder): Boolean {
        return StarRocksParsingUtil.word(builder) in QUERY_STARTERS || builder.tokenText == ";"
    }

    private fun consumeSelectModifiers(builder: PsiBuilder) {
        while (StarRocksParsingUtil.word(builder) in SELECT_MODIFIERS) {
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
        }
    }

    private fun parseSelectAlias(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.isIdentifier(builder)) {
            return false
        }
        val alias = builder.mark()
        builder.advanceLexer()
        alias.done(StarRocksElementTypes.SELECT_ALIAS)
        return true
    }

    private fun selectClauseBoundary(builder: PsiBuilder): Boolean {
        return topLevelBoundary(builder, SELECT_CLAUSE_BOUNDARIES) || builder.tokenText == ";"
    }

    private fun selectItemBoundary(builder: PsiBuilder): Boolean {
        return selectItemBoundary(builder, 0)
    }

    private fun selectItemBoundary(builder: PsiBuilder, parenDepth: Int): Boolean {
        return parenDepth == 0 && (builder.tokenText == ";" || topLevelBoundary(builder, SELECT_CLAUSE_BOUNDARIES))
    }

    private fun orderByBoundary(builder: PsiBuilder): Boolean {
        return builder.tokenText == ";" || topLevelBoundary(builder, ORDER_BY_BOUNDARIES)
    }

    private fun orderByItemBoundary(builder: PsiBuilder): Boolean {
        return orderByItemBoundary(builder, 0)
    }

    private fun orderByItemBoundary(builder: PsiBuilder, parenDepth: Int): Boolean {
        return parenDepth == 0 && orderByBoundary(builder)
    }

    private fun limitBoundary(builder: PsiBuilder): Boolean {
        return builder.tokenText == ";" || topLevelBoundary(builder, LIMIT_BOUNDARIES)
    }

    private fun topLevelBoundary(builder: PsiBuilder, words: Set<String>): Boolean {
        return builder.tokenText == ";" || builder.tokenText == ")" || StarRocksParsingUtil.word(builder) in words
    }

    private fun analyzeSelectItem(builder: PsiBuilder): SelectItemPlan {
        val marker = builder.mark()
        val tokens = mutableListOf<SelectItemToken>()
        var parenDepth = 0
        var explicitAsOffset: Int? = null
        var aliasOffset: Int? = null
        try {
            while (!builder.eof()) {
                val text = builder.tokenText
                val word = StarRocksParsingUtil.word(builder)
                if (parenDepth == 0 && (text == "," || text == ";" || word in SELECT_CLAUSE_BOUNDARIES)) {
                    break
                }
                if (!text.isNullOrBlank()) {
                    val token = SelectItemToken(builder.currentOffset, text, word, parenDepth)
                    tokens += token
                    if (parenDepth == 0 && word == "AS") {
                        nextMeaningfulSelectItemToken(builder)?.let { next ->
                            if (isAliasCandidate(next)) {
                                explicitAsOffset = token.offset
                                aliasOffset = next.offset
                            }
                        }
                    }
                }
                parenDepth = nextParenDepth(text, parenDepth)
                builder.advanceLexer()
            }
        } finally {
            marker.rollbackTo()
        }
        if (aliasOffset == null) {
            aliasOffset = implicitSelectAliasOffset(tokens)
        }
        return SelectItemPlan(explicitAsOffset, aliasOffset)
    }

    private fun nextMeaningfulSelectItemToken(builder: PsiBuilder): SelectItemToken? {
        val marker = builder.mark()
        try {
            var parenDepth = 0
            builder.advanceLexer()
            while (!builder.eof()) {
                val text = builder.tokenText
                val word = StarRocksParsingUtil.word(builder)
                if (parenDepth == 0 && (text == "," || text == ";" || word in SELECT_CLAUSE_BOUNDARIES)) {
                    return null
                }
                if (!text.isNullOrBlank()) {
                    return SelectItemToken(builder.currentOffset, text, word, parenDepth)
                }
                parenDepth = nextParenDepth(text, parenDepth)
                builder.advanceLexer()
            }
            return null
        } finally {
            marker.rollbackTo()
        }
    }

    private fun implicitSelectAliasOffset(tokens: List<SelectItemToken>): Int? {
        val topLevel = tokens.filter { it.depth == 0 }
        val candidate = topLevel.lastOrNull() ?: return null
        val previous = topLevel.dropLast(1).lastOrNull() ?: return null
        if (!isAliasCandidate(candidate) || previous.text == ".") {
            return null
        }
        return candidate.offset
    }

    private fun isAliasCandidate(token: SelectItemToken): Boolean {
        val word = token.word
        return token.text.firstOrNull()?.let { it == '_' || it == '`' || it.isLetter() } == true &&
            word !in SELECT_CLAUSE_BOUNDARIES &&
            word !in SELECT_ALIAS_EXCLUDED_WORDS
    }

    private fun column_reference(builder: PsiBuilder, level: Int): Boolean {
        val word = StarRocksParsingUtil.word(builder)
        if (!StarRocksParsingUtil.isIdentifier(builder) ||
            word in COLUMN_REFERENCE_BOUNDARIES ||
            (word != null && StarRocksKeywordCatalog.isKeyword(word)) ||
            nextMeaningfulTokenText(builder) == "("
        ) {
            return false
        }
        if (isQualifiedColumnReference(builder)) {
            val first = builder.mark()
            builder.advanceLexer()
            first.done(StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX)
            StarRocksParsingUtil.skipNoise(builder)
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
            val name = builder.mark()
            builder.advanceLexer()
            name.done(StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            return true
        }
        val first = builder.mark()
        builder.advanceLexer()
        first.done(StarRocksElementTypes.COLUMN_REFERENCE_NAME)
        return true
    }

    private fun isQualifiedColumnReference(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        try {
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
            if (builder.tokenText != ".") {
                return false
            }
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
            return StarRocksParsingUtil.isIdentifier(builder)
        } finally {
            marker.rollbackTo()
        }
    }

    private fun window_reference(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "OVER")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.isIdentifier(builder)) {
            marker.rollbackTo()
            return false
        }
        val name = builder.mark()
        builder.advanceLexer()
        name.done(StarRocksElementTypes.WINDOW_REFERENCE_NAME)
        marker.drop()
        return true
    }

    private fun nextMeaningfulWordIsQueryStarter(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        try {
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
            return StarRocksParsingUtil.word(builder) in QUERY_STARTERS
        } finally {
            marker.rollbackTo()
        }
    }

    private fun nextParenDepth(tokenText: String?, current: Int): Int {
        return when (tokenText) {
            "(" -> current + 1
            ")" -> (current - 1).coerceAtLeast(0)
            else -> current
        }
    }

    private fun nextMeaningfulTokenText(builder: PsiBuilder): String? {
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val result = builder.tokenText
        marker.rollbackTo()
        return result
    }

    private fun parseTableAlias(builder: PsiBuilder) {
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "AS")
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.word(builder) in TABLE_ALIAS_BOUNDARIES || !StarRocksParsingUtil.isIdentifier(builder)) {
            return
        }
        val alias = builder.mark()
        builder.advanceLexer()
        alias.done(StarRocksElementTypes.TABLE_ALIAS)
    }

    private fun parseTableFunctionAliasColumnList(builder: PsiBuilder): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        val list = builder.mark()
        builder.advanceLexer()
        while (!builder.eof() && builder.tokenText != ")") {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                StarRocksParsingUtil.isIdentifier(builder) -> {
                    val column = builder.mark()
                    builder.advanceLexer()
                    column.done(StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME)
                }
                else -> builder.advanceLexer()
            }
        }
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        list.done(StarRocksElementTypes.TABLE_ALIAS_COLUMN_LIST)
        return true
    }

    private val TABLE_ALIAS_BOUNDARIES = setOf(
        "ON",
        "WHERE",
        "GROUP",
        "HAVING",
        "QUALIFY",
        "WINDOW",
        "ORDER",
        "LIMIT",
        "JOIN",
        "LEFT",
        "RIGHT",
        "FULL",
        "INNER",
        "CROSS",
        "UNION",
        "INTERSECT",
        "EXCEPT",
        "MINUS"
    )

    private val TABLE_REFERENCE_STARTERS = setOf("FROM", "JOIN")

    private val COLUMN_REFERENCE_BOUNDARIES = TABLE_REFERENCE_STARTERS + TABLE_ALIAS_BOUNDARIES + setOf(
        "SELECT",
        "WITH",
        "AS",
        "BY",
        "OVER",
        "PARTITION"
    )

    private val SELECT_MODIFIERS = setOf("ALL", "DISTINCT")

    private val SELECT_ALIAS_EXCLUDED_WORDS = COLUMN_REFERENCE_BOUNDARIES + setOf(
        "ASC",
        "DESC",
        "NULLS",
        "FIRST",
        "LAST",
        "AS"
    )

    private data class SelectItemPlan(
        val explicitAsOffset: Int?,
        val aliasOffset: Int?
    )

    private data class SelectItemToken(
        val offset: Int,
        val text: String,
        val word: String?,
        val depth: Int
    )
}

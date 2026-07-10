package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.base.SqlGeneratedParserUtil
import com.intellij.sql.psi.SqlCompositeElementTypes

object StarRocksDmlParsing {
    @JvmStatic
    fun dml_statement(builder: PsiBuilder, level: Int): Boolean =
        insert_statement(builder, level + 1) ||
            update_statement(builder, level + 1) ||
            delete_statement(builder, level + 1) ||
            merge_statement(builder, level + 1) ||
            select_statement(builder, level + 1)

    @JvmStatic
    fun select_statement(builder: PsiBuilder, level: Int): Boolean {
        val word = StarRocksParsingUtil.word(builder)
        if (word !in QUERY_START_WORDS) {
            return false
        }
        return top_query_expression(builder, level + 1)
    }

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
        val marker = builder.mark()
        val before = builder.currentOffset
        if (!query_expression(builder, level + 1, -1)) {
            marker.rollbackTo()
            return false
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
    fun query_expression(builder: PsiBuilder, level: Int, priority: Int): Boolean {
        return query_expression_0(builder, level + 1, priority)
    }

    @JvmStatic
    fun query_expression_0(builder: PsiBuilder, level: Int, priority: Int): Boolean {
        if (!with_query_expression(builder, level + 1) && !atom_query_expression(builder, level + 1)) {
            return false
        }
        while (!builder.eof() && set_operation_clause(builder, level + 1)) {
            StarRocksParsingUtil.skipNoise(builder)
            if (!atom_query_expression(builder, level + 1)) {
                break
            }
        }
        return true
    }

    @JvmStatic
    fun simple_query_expression(builder: PsiBuilder, level: Int): Boolean {
        if (!select_clause(builder, level + 1)) {
            return false
        }
        parseQueryTail(builder, level + 1)
        return true
    }

    @JvmStatic
    fun with_query_expression(builder: PsiBuilder, level: Int): Boolean {
        if (!with_clause(builder, level + 1)) {
            return false
        }
        StarRocksParsingUtil.skipNoise(builder)
        return atom_query_expression(builder, level + 1)
    }

    @JvmStatic
    fun atom_query_expression(builder: PsiBuilder, level: Int): Boolean {
        return parenthesized_query_expression(builder, level + 1) ||
            simple_query_expression(builder, level + 1) ||
            values_expression(builder, level + 1)
    }

    @JvmStatic
    fun values_expression(builder: PsiBuilder, level: Int): Boolean {
        if (!values_clause(builder, level + 1)) {
            return false
        }
        parseQueryTail(builder, level + 1)
        return true
    }

    private fun parseQueryTail(builder: PsiBuilder, level: Int) {
        var progressed: Boolean
        do {
            val before = builder.currentOffset
            progressed = from_clause(builder, level + 1) ||
                where_clause(builder, level + 1) ||
                group_by_clause(builder, level + 1) ||
                having_clause(builder, level + 1) ||
                qualify_clause(builder, level + 1) ||
                window_clause(builder, level + 1) ||
                order_by_clause(builder, level + 1) ||
                limit_clause(builder, level + 1)
            if (builder.currentOffset == before) {
                progressed = false
            }
            StarRocksParsingUtil.skipNoise(builder)
        } while (progressed && !builder.eof())
    }

    @JvmStatic
    fun insert_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "INSERT")) {
            return false
        }
        return dmlStatementBody(builder, level, SqlCompositeElementTypes.SQL_INSERT_STATEMENT)
    }

    @JvmStatic
    fun update_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "UPDATE")) {
            return false
        }
        return dmlStatementBody(builder, level, SqlCompositeElementTypes.SQL_UPDATE_STATEMENT)
    }

    @JvmStatic
    fun delete_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "DELETE")) {
            return false
        }
        return dmlStatementBody(builder, level, SqlCompositeElementTypes.SQL_DELETE_STATEMENT)
    }

    @JvmStatic
    fun merge_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "MERGE")) {
            return false
        }
        return dmlStatementBody(builder, level, SqlCompositeElementTypes.SQL_MERGE_STATEMENT)
    }

    private fun dmlStatementBody(builder: PsiBuilder, level: Int, elementType: IElementType): Boolean {
        val marker = builder.mark()
        val before = builder.currentOffset
        var parenDepth = 0
        while (!builder.eof() && builder.tokenText != ";") {
            when {
                elementType == SqlCompositeElementTypes.SQL_INSERT_STATEMENT && parenDepth == 0 && insert_target_clause(builder, level + 1) -> continue
                elementType == SqlCompositeElementTypes.SQL_UPDATE_STATEMENT && parenDepth == 0 && update_target_clause(builder, level + 1) -> continue
                elementType == SqlCompositeElementTypes.SQL_DELETE_STATEMENT && parenDepth == 0 && delete_target_clause(builder, level + 1) -> continue
                elementType == SqlCompositeElementTypes.SQL_MERGE_STATEMENT && parenDepth == 0 && merge_target_clause(builder, level + 1) -> continue
                elementType == SqlCompositeElementTypes.SQL_MERGE_STATEMENT && parenDepth == 0 && merge_using_clause(builder, level + 1) -> continue
                elementType == SqlCompositeElementTypes.SQL_MERGE_STATEMENT && parenDepth == 0 && merge_on_clause(builder, level + 1) -> continue
                elementType == SqlCompositeElementTypes.SQL_MERGE_STATEMENT && parenDepth == 0 && merge_when_clause(builder, level + 1) -> continue
                parenDepth == 0 && set_clause(builder, level + 1) -> continue
                parenDepth == 0 && with_clause(builder, level + 1) -> continue
                parenDepth == 0 && select_clause(builder, level + 1) -> continue
                parenDepth == 0 && values_clause(builder, level + 1) -> continue
                parenDepth == 0 && from_clause(builder, level + 1) -> continue
                parenDepth == 0 && where_clause(builder, level + 1) -> continue
                parenDepth == 0 && group_by_clause(builder, level + 1) -> continue
                parenDepth == 0 && having_clause(builder, level + 1) -> continue
                parenDepth == 0 && order_by_clause(builder, level + 1) -> continue
                parenDepth == 0 && limit_clause(builder, level + 1) -> continue
                parenDepth == 0 && window_clause(builder, level + 1) -> continue
                parenDepth == 0 && set_operation_clause(builder, level + 1) -> continue
                qualify_clause(builder, level + 1) -> continue
                table_function_call(builder, level + 1) -> continue
                parenDepth == 0 && column_reference(builder, level + 1) -> continue
                else -> {
                    parenDepth = nextParenDepth(builder.tokenText, parenDepth)
                    builder.advanceLexer()
                }
            }
        }
        return if (builder.currentOffset > before) {
            marker.done(elementType)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun insert_target_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "INSERT")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val hasTargetIntro = when {
            StarRocksParsingUtil.consumeWord(builder, "INTO") -> true
            StarRocksParsingUtil.consumeWord(builder, "OVERWRITE") -> true
            else -> false
        }
        if (!hasTargetIntro) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "TABLE")
        StarRocksParsingUtil.skipNoise(builder)
        val parsed = dml_target_table(builder, level + 1)
        if (!parsed) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.INSERT_TARGET_CLAUSE)
        return true
    }

    @JvmStatic
    fun update_target_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "UPDATE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!dml_target_table(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        marker.drop()
        return true
    }

    @JvmStatic
    fun delete_target_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "DELETE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "FROM")
        StarRocksParsingUtil.skipNoise(builder)
        if (!dml_target_table(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        marker.drop()
        return true
    }

    @JvmStatic
    fun merge_target_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "MERGE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "INTO")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!dml_target_table(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        marker.drop()
        return true
    }

    @JvmStatic
    fun merge_using_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "USING")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!dml_target_table(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.MERGE_USING_CLAUSE)
        return true
    }

    @JvmStatic
    fun merge_on_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "ON")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val expression = builder.mark()
        val before = builder.currentOffset
        consumeExpressionItems(builder, level + 1, MERGE_ON_BOUNDARIES, stopAtComma = false)
        if (builder.currentOffset > before) {
            expression.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
        } else {
            expression.drop()
        }
        marker.done(StarRocksElementTypes.MERGE_ON_CLAUSE)
        return true
    }

    @JvmStatic
    fun merge_when_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "WHEN")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        var parenDepth = 0
        while (!builder.eof() && builder.tokenText != ";" && !(parenDepth == 0 && StarRocksParsingUtil.word(builder) == "WHEN")) {
            when {
                parenDepth == 0 && set_clause(builder, level + 1) -> continue
                parenDepth == 0 && values_clause(builder, level + 1) -> continue
                parenDepth == 0 && column_reference(builder, level + 1) -> continue
                else -> {
                    parenDepth = nextParenDepth(builder.tokenText, parenDepth)
                    builder.advanceLexer()
                }
            }
        }
        marker.done(StarRocksElementTypes.MERGE_WHEN_CLAUSE)
        return true
    }

    @JvmStatic
    fun set_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "SET")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        var parsed = false
        while (!builder.eof() && !topLevelBoundary(builder, SET_CLAUSE_BOUNDARIES)) {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                set_assignment(builder, level + 1) -> parsed = true
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        marker.done(StarRocksElementTypes.SET_CLAUSE)
        return parsed
    }

    @JvmStatic
    fun set_assignment(builder: PsiBuilder, level: Int): Boolean {
        if (builder.eof() || builder.tokenText == "," || topLevelBoundary(builder, SET_CLAUSE_BOUNDARIES)) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        var parenDepth = 0
        while (!builder.eof() && !(parenDepth == 0 && (builder.tokenText == "," || topLevelBoundary(builder, SET_CLAUSE_BOUNDARIES)))) {
            when {
                column_reference(builder, level + 1) -> continue
                StarRocksExpressionParsing.cast_expression(builder, level + 1) -> continue
                else -> {
                    parenDepth = nextParenDepth(builder.tokenText, parenDepth)
                    builder.advanceLexer()
                }
            }
        }
        return if (builder.currentOffset > before) {
            marker.done(StarRocksElementTypes.SET_ASSIGNMENT)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun dml_target_table(builder: PsiBuilder, level: Int): Boolean {
        val marker = builder.mark()
        val parsed = SqlGeneratedParserUtil.parseReference(
            builder,
            level + 1,
            SqlCompositeElementTypes.SQL_TABLE_REFERENCE
        )
        if (!parsed) {
            return marker.rollbackFalse()
        }
        parseTableAlias(builder)
        marker.done(StarRocksElementTypes.DML_TARGET_TABLE)
        return true
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
        if (!SqlGeneratedParserUtil.parseIdentifier(builder, level + 1)) {
            marker.rollbackTo()
            return false
        }
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
        query_expression(builder, level + 1, -1)
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
                StarRocksExpressionParsing.cast_expression(builder, level + 1) -> continue
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
    fun from_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "FROM")) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val parsed = join_expression(builder, level + 1)
        while (!builder.eof() && !fromClauseBoundary(builder)) {
            val current = builder.currentOffset
            if (!join_expression(builder, level + 1)) {
                builder.advanceLexer()
            }
            if (builder.currentOffset == current && !builder.eof()) {
                builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        return if (parsed || builder.currentOffset > before) {
            marker.done(StarRocksElementTypes.FROM_CLAUSE)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun join_expression(builder: PsiBuilder, level: Int): Boolean {
        var expression = tableExpressionMarker(builder, level + 1) ?: return false
        joinLoop@ while (!builder.eof() && !fromClauseBoundary(builder)) {
            StarRocksParsingUtil.skipNoise(builder)
            val joinMarker = when {
                builder.tokenText == "," -> {
                    val marker = expression.precede()
                    builder.advanceLexer()
                    marker
                }
                isJoinOperatorStart(builder) -> {
                    val marker = expression.precede()
                    if (!consumeJoinOperator(builder)) {
                        marker.drop()
                        break@joinLoop
                    }
                    marker
                }
                else -> break@joinLoop
            }
            StarRocksParsingUtil.skipNoise(builder)
            tableExpressionMarker(builder, level + 1)
            StarRocksParsingUtil.skipNoise(builder)
            join_condition_clause(builder, level + 1)
            joinMarker.done(StarRocksElementTypes.JOIN_EXPRESSION)
            expression = joinMarker
        }
        return true
    }

    @JvmStatic
    fun atom_join_expression(builder: PsiBuilder, level: Int): Boolean {
        return table_expression(builder, level)
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

    @JvmStatic
    fun set_operation_clause(builder: PsiBuilder, level: Int): Boolean {
        if (StarRocksParsingUtil.word(builder) !in SET_OPERATORS) {
            return false
        }
        val marker = builder.mark()
        if (!set_operator(builder, level + 1)) {
            marker.rollbackTo()
            return false
        }
        marker.done(StarRocksElementTypes.SET_OPERATION_CLAUSE)
        return true
    }

    @JvmStatic
    fun set_operator(builder: PsiBuilder, level: Int): Boolean {
        val word = StarRocksParsingUtil.word(builder)
        if (word !in SET_OPERATORS) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.word(builder) in SET_OPERATOR_MODIFIERS) {
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
        }
        marker.done(StarRocksElementTypes.SET_OPERATOR)
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

    private val SET_CLAUSE_BOUNDARIES = setOf(
        "FROM",
        "WHERE",
        "USING",
        "ON",
        "WHEN",
        "VALUES",
        "SELECT",
        "GROUP",
        "HAVING",
        "QUALIFY",
        "ORDER",
        "LIMIT"
    )

    private val MERGE_ON_BOUNDARIES = setOf("WHEN")

    private val QUERY_STARTERS = setOf("SELECT", "WITH", "VALUES")

    private val SET_OPERATORS = setOf("UNION", "INTERSECT", "EXCEPT", "MINUS")

    private val SET_OPERATOR_MODIFIERS = setOf("ALL", "DISTINCT")

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
        val expressionBoundaries = buildSet {
            addAll(boundaries)
            if (stopAtComma) {
                add(",")
            }
            if (stopAtRightParen) {
                add(")")
            }
        }
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
                StarRocksExpressionParsing.value_expression(builder, level + 1, expressionBoundaries) -> continue
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
        query_expression(builder, level + 1, -1)
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

    private fun table_expression(builder: PsiBuilder, level: Int): Boolean {
        return tableExpressionMarker(builder, level) != null
    }

    private fun tableExpressionMarker(builder: PsiBuilder, level: Int): PsiBuilder.Marker? {
        StarRocksParsingUtil.skipNoise(builder)
        val marker = builder.mark()
        if (!tableExpressionBody(builder, level + 1)) {
            marker.rollbackTo()
            return null
        }
        marker.done(StarRocksElementTypes.TABLE_EXPRESSION)
        return marker
    }

    private fun tableExpressionBody(builder: PsiBuilder, level: Int): Boolean {
        val derivedTable = builder.mark()
        if (parenthesized_query_expression(builder, level + 1)) {
            parseTableAlias(builder)
            derivedTable.done(StarRocksElementTypes.TABLE_REFERENCE)
            return true
        }
        derivedTable.drop()
        val parenthesizedJoin = builder.mark()
        if (parenthesized_join_expression(builder, level + 1)) {
            parseTableAlias(builder)
            parenthesizedJoin.done(StarRocksElementTypes.TABLE_REFERENCE)
            return true
        }
        parenthesizedJoin.drop()
        if (StarRocksParsingUtil.word(builder) == "UNNEST") {
            val table = builder.mark()
            if (!table_function_call(builder, level + 1)) {
                table.rollbackTo()
                return false
            }
            table.done(StarRocksElementTypes.TABLE_REFERENCE)
            return true
        }
        if (!StarRocksParsingUtil.isIdentifier(builder)) {
            return false
        }
        val table = builder.mark()
        val parsed = SqlGeneratedParserUtil.parseReference(
            builder,
            level + 1,
            SqlCompositeElementTypes.SQL_TABLE_REFERENCE
        )
        if (!parsed) {
            table.rollbackTo()
            return false
        }
        parseTableAlias(builder)
        table.done(StarRocksElementTypes.TABLE_REFERENCE)
        return true
    }

    private fun parenthesized_join_expression(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenText != "(" || nextMeaningfulWordIsQueryStarter(builder)) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!join_expression(builder, level + 1)) {
            marker.rollbackTo()
            return false
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.PARENTHESIZED_JOIN_EXPRESSION)
        return true
    }

    private fun consumeJoinOperator(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        val before = builder.currentOffset
        var sawJoin = false
        var consumed = false
        while (!builder.eof()) {
            val word = StarRocksParsingUtil.word(builder) ?: break
            when {
                word == "JOIN" || word == "STRAIGHT_JOIN" -> {
                    builder.advanceLexer()
                    sawJoin = true
                    consumed = true
                    break
                }
                word in JOIN_MODIFIERS -> {
                    builder.advanceLexer()
                    consumed = true
                    StarRocksParsingUtil.skipNoise(builder)
                }
                else -> break
            }
        }
        return if (sawJoin && consumed && builder.currentOffset > before) {
            marker.drop()
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun isJoinOperatorStart(builder: PsiBuilder): Boolean {
        return StarRocksParsingUtil.word(builder) in JOIN_STARTERS
    }

    private fun join_condition_clause(builder: PsiBuilder, level: Int): Boolean {
        return when (StarRocksParsingUtil.word(builder)) {
            "ON" -> {
                val marker = builder.mark()
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                val expression = builder.mark()
                val before = builder.currentOffset
                consumeExpressionItems(builder, level + 1, JOIN_CONDITION_BOUNDARIES, stopAtComma = false)
                if (builder.currentOffset > before) {
                    expression.done(StarRocksElementTypes.PREDICATE_EXPRESSION)
                } else {
                    expression.drop()
                }
                marker.done(StarRocksElementTypes.JOIN_CONDITION_CLAUSE)
                true
            }
            "USING" -> {
                val marker = builder.mark()
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                if (builder.tokenText == "(") {
                    builder.advanceLexer()
                    parseUsingReferenceList(builder)
                    if (builder.tokenText == ")") {
                        builder.advanceLexer()
                    }
                }
                marker.done(StarRocksElementTypes.USING_CLAUSE)
                true
            }
            else -> false
        }
    }

    private fun parseUsingReferenceList(builder: PsiBuilder): Boolean {
        val list = builder.mark()
        var parsed = false
        while (!builder.eof() && builder.tokenText != ")") {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                StarRocksParsingUtil.isIdentifier(builder) -> {
                    val reference = builder.mark()
                    builder.advanceLexer()
                    reference.done(SqlCompositeElementTypes.SQL_COLUMN_REFERENCE)
                    parsed = true
                }
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        list.done(SqlCompositeElementTypes.SQL_REFERENCE_LIST)
        return parsed
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

    private fun fromClauseBoundary(builder: PsiBuilder): Boolean {
        return builder.tokenText == ";" || builder.tokenText == ")" || StarRocksParsingUtil.word(builder) in FROM_BOUNDARIES
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

    @JvmStatic
    fun column_reference(builder: PsiBuilder, level: Int): Boolean {
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

    private fun PsiBuilder.Marker.rollbackFalse(): Boolean {
        rollbackTo()
        return false
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
        "SET",
        "USING",
        "WHEN",
        "VALUES",
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

    private val FROM_BOUNDARIES = setOf(
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

    private val JOIN_STARTERS = setOf(
        "JOIN",
        "INNER",
        "LEFT",
        "RIGHT",
        "FULL",
        "CROSS",
        "NATURAL",
        "STRAIGHT_JOIN"
    )

    private val JOIN_MODIFIERS = JOIN_STARTERS + setOf("OUTER", "SEMI", "ANTI")

    private val JOIN_CONDITION_BOUNDARIES = FROM_BOUNDARIES + JOIN_STARTERS

    private val COLUMN_REFERENCE_BOUNDARIES = setOf("FROM") + TABLE_ALIAS_BOUNDARIES + JOIN_STARTERS + setOf(
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

    private val QUERY_START_WORDS = setOf(
        "SELECT",
        "WITH",
        "VALUES"
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

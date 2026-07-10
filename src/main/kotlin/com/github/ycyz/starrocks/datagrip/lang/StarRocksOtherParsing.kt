package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.sql.dialects.base.SqlGeneratedParserUtil
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlReferenceElementType

object StarRocksOtherParsing {
    @JvmStatic
    fun other_statement(builder: PsiBuilder, level: Int): Boolean =
        show_statement(builder, level + 1) ||
            admin_statement(builder, level + 1) ||
            analyze_statement(builder, level + 1) ||
            set_password_statement(builder, level + 1) ||
            set_statement(builder, level + 1) ||
            unset_statement(builder, level + 1) ||
            kill_statement(builder, level + 1) ||
            sync_statement(builder, level + 1) ||
            call_statement(builder, level + 1) ||
            start_transaction_statement(builder, level + 1) ||
            commit_statement(builder, level + 1) ||
            rollback_statement(builder, level + 1) ||
            use_statement(builder, level + 1) ||
            explain_statement(builder, level + 1) ||
            describe_statement(builder, level + 1) ||
            load_statement(builder, level + 1) ||
            cancel_load_statement(builder, level + 1) ||
            task_statement(builder, level + 1) ||
            export_statement(builder, level + 1) ||
            backup_restore_statement(builder, level + 1)

    @JvmStatic
    fun show_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "SHOW")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val elementType = when (StarRocksParsingUtil.word(builder)) {
            "CATALOGS" -> {
                builder.advanceLexer()
                StarRocksElementTypes.CATALOG_STATEMENT
            }
            "RESOURCES" -> {
                builder.advanceLexer()
                StarRocksElementTypes.RESOURCE_STATEMENT
            }
            "BACKUP", "RESTORE" -> {
                builder.advanceLexer()
                parseOptionalFromReference(builder, level + 1, SqlCompositeElementTypes.SQL_SCHEMA_REFERENCE)
                StarRocksElementTypes.BACKUP_RESTORE_STATEMENT
            }
            "MATERIALIZED" -> {
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                StarRocksParsingUtil.consumeWord(builder, "VIEWS")
                StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT
            }
            "TABLES" -> {
                builder.advanceLexer()
                parseOptionalFromReference(builder, level + 1, SqlCompositeElementTypes.SQL_SCHEMA_REFERENCE)
                StarRocksElementTypes.ADMIN_STATEMENT
            }
            "PARTITIONS" -> {
                builder.advanceLexer()
                parseOptionalFromReference(builder, level + 1, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)
                StarRocksElementTypes.TABLE_DDL_STATEMENT
            }
            "CREATE" -> parseShowCreateTail(builder, level + 1)
            "DATABASES", "SCHEMAS", "VARIABLES", "ROLES", "USERS", "FUNCTIONS", "GRANTS" -> {
                builder.advanceLexer()
                StarRocksElementTypes.ADMIN_STATEMENT
            }
            "ANALYZE" -> {
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                StarRocksParsingUtil.consumeWord(builder, "STATUS")
                StarRocksElementTypes.ANALYZE_STATEMENT
            }
            "STATS" -> {
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                StarRocksParsingUtil.consumeWord(builder, "META")
                StarRocksElementTypes.ANALYZE_STATEMENT
            }
            "PROC" -> {
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                consumeOptionalSingleShowAtom(builder)
                StarRocksElementTypes.ADMIN_STATEMENT
            }
            else -> return marker.rollbackFalse()
        }
        marker.done(elementType)
        return true
    }

    @JvmStatic
    fun admin_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "ADMIN")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!admin_command(builder)) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.ADMIN_STATEMENT)
        return true
    }

    @JvmStatic
    fun analyze_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "ANALYZE")) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        consumeAnalyzeMode(builder)
        StarRocksParsingUtil.skipNoise(builder)
        if (!analyze_target(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        while (!builder.eof() && builder.tokenText != ";") {
            when {
                analyze_histogram_clause(builder, level + 1) -> continue
                analyze_column_list(builder, level + 1) -> continue
                StarRocksDdlParsing.properties_clause(builder, level + 1) -> continue
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        return if (builder.currentOffset > before) {
            marker.done(StarRocksElementTypes.ANALYZE_STATEMENT)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun analyze_target(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "TABLE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        val parsed = SqlGeneratedParserUtil.parseReference(
            builder,
            level + 1,
            SqlCompositeElementTypes.SQL_TABLE_REFERENCE
        )
        if (!parsed) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        analyze_column_list(builder, level + 1)
        marker.done(StarRocksElementTypes.ANALYZE_TARGET)
        return true
    }

    @JvmStatic
    fun analyze_histogram_clause(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "UPDATE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "HISTOGRAM")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "ON")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!analyze_column_references(builder, level + 1, ANALYZE_HISTOGRAM_BOUNDARIES)) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.ANALYZE_HISTOGRAM_CLAUSE)
        return true
    }

    @JvmStatic
    fun analyze_column_list(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        analyze_column_references(builder, level + 1, setOf(")"))
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        marker.done(StarRocksElementTypes.ANALYZE_COLUMN_LIST)
        return true
    }

    @JvmStatic
    fun set_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "SET")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!set_assignment_list(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        marker.done(SqlCompositeElementTypes.SQL_SET_STATEMENT)
        return true
    }

    @JvmStatic
    fun set_password_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "SET", "PASSWORD")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.consumeWord(builder, "FOR")) {
            StarRocksParsingUtil.skipNoise(builder)
            consumeNameLike(builder, allowQualified = false, allowUserHost = true)
            StarRocksParsingUtil.skipNoise(builder)
        }
        if (!consumeEqualsExpression(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.SET_PASSWORD_STATEMENT)
        return true
    }

    @JvmStatic
    fun unset_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "UNSET")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "VARIABLE")
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeVariableOrName(builder)) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.ADMIN_STATEMENT)
        return true
    }

    @JvmStatic
    fun kill_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "KILL")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeSingleAtom(builder)) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.ADMIN_STATEMENT)
        return true
    }

    @JvmStatic
    fun sync_statement(builder: PsiBuilder, level: Int): Boolean =
        exact_words_statement(builder, StarRocksElementTypes.ADMIN_STATEMENT, "SYNC")

    @JvmStatic
    fun call_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "CALL")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksExpressionParsing.value_expression(builder, level + 1, emptySet())) {
            return marker.rollbackFalse()
        }
        marker.done(SqlCompositeElementTypes.SQL_CALL_STATEMENT)
        return true
    }

    @JvmStatic
    fun start_transaction_statement(builder: PsiBuilder, level: Int): Boolean =
        exact_words_statement(builder, SqlCompositeElementTypes.SQL_START_TRANSACTION_STATEMENT, "BEGIN") ||
            exact_words_statement(builder, SqlCompositeElementTypes.SQL_START_TRANSACTION_STATEMENT, "START", "TRANSACTION")

    @JvmStatic
    fun commit_statement(builder: PsiBuilder, level: Int): Boolean =
        exact_words_statement(builder, SqlCompositeElementTypes.SQL_COMMIT_STATEMENT, "COMMIT")

    @JvmStatic
    fun rollback_statement(builder: PsiBuilder, level: Int): Boolean =
        exact_words_statement(builder, SqlCompositeElementTypes.SQL_ROLLBACK_STATEMENT, "ROLLBACK")

    @JvmStatic
    fun use_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "USE")) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!use_target(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        while (!builder.eof() && builder.tokenText != ";") {
            builder.advanceLexer()
        }
        return if (builder.currentOffset > before) {
            marker.done(SqlCompositeElementTypes.SQL_USE_SCHEMA_STATEMENT)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun use_target(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.isIdentifier(builder)) {
            return false
        }
        val marker = builder.mark()
        val parsed = SqlGeneratedParserUtil.parseReference(
            builder,
            level + 1,
            SqlCompositeElementTypes.SQL_SCHEMA_REFERENCE
        )
        if (!parsed) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.USE_TARGET)
        return true
    }

    @JvmStatic
    fun explain_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "EXPLAIN")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        consumeExplainModifiers(builder)
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksDmlParsing.query_expression(builder, level + 1, -1)) {
            return marker.rollbackFalse()
        }
        marker.done(SqlCompositeElementTypes.SQL_EXPLAIN_STATEMENT)
        return true
    }

    @JvmStatic
    fun describe_statement(builder: PsiBuilder, level: Int): Boolean {
        if (StarRocksParsingUtil.word(builder) !in DESCRIBE_WORDS) {
            return false
        }
        val marker = builder.mark()
        val before = builder.currentOffset
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        StarRocksParsingUtil.consumeWord(builder, "TABLE")
        StarRocksParsingUtil.skipNoise(builder)
        if (!describe_target(builder, level + 1)) {
            return marker.rollbackFalse()
        }
        while (!builder.eof() && builder.tokenText != ";") {
            when {
                SqlGeneratedParserUtil.parseReference(
                    builder,
                    level + 1,
                    SqlCompositeElementTypes.SQL_COLUMN_REFERENCE
                ) -> continue
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        return if (builder.currentOffset > before) {
            marker.done(StarRocksElementTypes.DESCRIBE_STATEMENT)
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    @JvmStatic
    fun describe_target(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.isIdentifier(builder)) {
            return false
        }
        val marker = builder.mark()
        val parsed = SqlGeneratedParserUtil.parseReference(
            builder,
            level + 1,
            SqlCompositeElementTypes.SQL_TABLE_REFERENCE
        )
        if (!parsed) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.DESCRIBE_TARGET)
        return true
    }

    @JvmStatic
    fun load_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "LOAD")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "LABEL")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeNameLike(builder, allowQualified = true, allowUserHost = false)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        parenthesized_payload_clause(builder)
        consumeKnownTail(builder) {
            with_broker_clause(builder) ||
                StarRocksDdlParsing.properties_clause(builder, level + 1)
        }
        marker.done(StarRocksElementTypes.LOAD_STATEMENT)
        return true
    }

    @JvmStatic
    fun cancel_load_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "CANCEL", "LOAD")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        parseOptionalFromReference(builder, level + 1, SqlCompositeElementTypes.SQL_SCHEMA_REFERENCE)
        StarRocksParsingUtil.skipNoise(builder)
        where_condition_clause(builder)
        marker.done(StarRocksElementTypes.LOAD_STATEMENT)
        return true
    }

    @JvmStatic
    fun task_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "SUBMIT", "TASK")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeNameLike(builder, allowQualified = true, allowUserHost = false)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "AS")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksDdlParsing.refresh_materialized_view_statement(builder, level + 1) &&
            !StarRocksDmlParsing.top_query_expression(builder, level + 1)
        ) {
            return marker.rollbackFalse()
        }
        marker.done(StarRocksElementTypes.TASK_STATEMENT)
        return true
    }

    @JvmStatic
    fun export_statement(builder: PsiBuilder, level: Int): Boolean =
        export_table_statement(builder, level + 1) ||
            cancel_export_statement(builder, level + 1)

    @JvmStatic
    fun backup_restore_statement(builder: PsiBuilder, level: Int): Boolean =
        snapshot_statement(builder, level + 1, "BACKUP", "TO") ||
            snapshot_statement(builder, level + 1, "RESTORE", "FROM") ||
            recover_statement(builder, level + 1)

    private fun exact_words_statement(builder: PsiBuilder, elementType: IElementType, vararg words: String): Boolean {
        if (!hasWords(builder, *words)) {
            return false
        }
        val marker = builder.mark()
        words.forEach { word ->
            StarRocksParsingUtil.skipNoise(builder)
            if (!StarRocksParsingUtil.consumeWord(builder, word)) {
                return marker.rollbackFalse()
            }
        }
        marker.done(elementType)
        return true
    }

    private fun admin_command(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.consumeWord(builder, "SHOW")) {
            return false
        }
        StarRocksParsingUtil.skipNoise(builder)
        val target = StarRocksParsingUtil.word(builder)
        if (target !in ADMIN_SHOW_TARGETS) {
            return false
        }
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.word(builder) in ADMIN_SHOW_SUFFIXES) {
            builder.advanceLexer()
        }
        return true
    }

    private fun set_assignment_list(builder: PsiBuilder, level: Int): Boolean {
        var parsed = false
        do {
            StarRocksParsingUtil.skipNoise(builder)
            if (builder.tokenText == ",") {
                builder.advanceLexer()
                continue
            }
            val assignment = builder.mark()
            if (!consumeVariableOrName(builder)) {
                assignment.rollbackTo()
                return parsed
            }
            StarRocksParsingUtil.skipNoise(builder)
            if (!consumeEqualsExpression(builder, level + 1)) {
                assignment.rollbackTo()
                return parsed
            }
            assignment.done(StarRocksElementTypes.SET_ASSIGNMENT)
            parsed = true
            StarRocksParsingUtil.skipNoise(builder)
        } while (builder.tokenText == ",")
        return parsed
    }

    private fun consumeEqualsExpression(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenText != "=") {
            return false
        }
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        return StarRocksExpressionParsing.value_expression(builder, level + 1, setOf(","))
    }

    private fun consumeExplainModifiers(builder: PsiBuilder): Boolean {
        var parsed = false
        while (StarRocksParsingUtil.word(builder) in EXPLAIN_MODIFIERS) {
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
            parsed = true
        }
        return parsed
    }

    private fun export_table_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "EXPORT", "TABLE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!parseReference(builder, level + 1, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "TO")) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeSingleAtom(builder)) {
            return marker.rollbackFalse()
        }
        consumeKnownTail(builder) {
            StarRocksDdlParsing.properties_clause(builder, level + 1)
        }
        marker.done(StarRocksElementTypes.EXPORT_STATEMENT)
        return true
    }

    private fun cancel_export_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!hasWords(builder, "CANCEL", "EXPORT")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        parseOptionalFromReference(builder, level + 1, SqlCompositeElementTypes.SQL_SCHEMA_REFERENCE)
        StarRocksParsingUtil.skipNoise(builder)
        where_condition_clause(builder)
        marker.done(StarRocksElementTypes.EXPORT_STATEMENT)
        return true
    }

    private fun snapshot_statement(builder: PsiBuilder, level: Int, verb: String, repositorySeparator: String): Boolean {
        if (!hasWords(builder, verb, "SNAPSHOT")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeNameLike(builder, allowQualified = true, allowUserHost = false)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, repositorySeparator)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeNameLike(builder, allowQualified = true, allowUserHost = false)) {
            return marker.rollbackFalse()
        }
        consumeKnownTail(builder) {
            on_object_list_clause(builder) ||
                StarRocksDdlParsing.properties_clause(builder, level + 1)
        }
        marker.done(StarRocksElementTypes.BACKUP_RESTORE_STATEMENT)
        return true
    }

    private fun recover_statement(builder: PsiBuilder, level: Int): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "RECOVER")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (StarRocksParsingUtil.word(builder) in RECOVER_OBJECT_WORDS) {
            builder.advanceLexer()
            StarRocksParsingUtil.skipNoise(builder)
        }
        consumeNameLike(builder, allowQualified = true, allowUserHost = false)
        marker.done(StarRocksElementTypes.BACKUP_RESTORE_STATEMENT)
        return true
    }

    private fun on_object_list_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "ON")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!parenthesized_payload_clause(builder)) {
            return marker.rollbackFalse()
        }
        marker.drop()
        return true
    }

    private fun where_condition_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "WHERE")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeVariableOrName(builder)) {
            return marker.rollbackFalse()
        }
        StarRocksParsingUtil.skipNoise(builder)
        if (builder.tokenText !in CONDITION_OPERATORS) {
            return marker.rollbackFalse()
        }
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!consumeSingleAtom(builder)) {
            return marker.rollbackFalse()
        }
        marker.drop()
        return true
    }

    private fun with_broker_clause(builder: PsiBuilder): Boolean {
        if (!StarRocksParsingUtil.tokenIs(builder, "WITH")) {
            return false
        }
        val marker = builder.mark()
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "BROKER")) {
            return marker.rollbackFalse()
        }
        marker.drop()
        return true
    }

    private fun consumeKnownTail(builder: PsiBuilder, parseClause: () -> Boolean) {
        while (!builder.eof() && builder.tokenText != ";") {
            StarRocksParsingUtil.skipNoise(builder)
            val before = builder.currentOffset
            if (!parseClause() || builder.currentOffset == before) {
                return
            }
        }
    }

    private fun parenthesized_payload_clause(builder: PsiBuilder): Boolean {
        if (builder.tokenText != "(") {
            return false
        }
        builder.advanceLexer()
        StarRocksParsingUtil.consumeBalancedTail(builder)
        if (builder.tokenText == ")") {
            builder.advanceLexer()
        }
        return true
    }

    private fun parseShowCreateTail(builder: PsiBuilder, level: Int): IElementType {
        builder.advanceLexer()
        StarRocksParsingUtil.skipNoise(builder)
        return when (StarRocksParsingUtil.word(builder)) {
            "CATALOG" -> {
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                parseReference(builder, level + 1, SqlCompositeElementTypes.SQL_CATALOG_REFERENCE)
                StarRocksElementTypes.CATALOG_STATEMENT
            }
            "MATERIALIZED" -> {
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                StarRocksParsingUtil.consumeWord(builder, "VIEW")
                StarRocksParsingUtil.skipNoise(builder)
                parseReference(builder, level + 1, SqlCompositeElementTypes.SQL_MATERIALIZED_VIEW_REFERENCE)
                StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT
            }
            "TABLE" -> {
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                parseReference(builder, level + 1, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)
                StarRocksElementTypes.TABLE_DDL_STATEMENT
            }
            "VIEW" -> {
                builder.advanceLexer()
                StarRocksParsingUtil.skipNoise(builder)
                parseReference(builder, level + 1, SqlCompositeElementTypes.SQL_VIEW_REFERENCE)
                StarRocksElementTypes.VIEW_STATEMENT
            }
            else -> StarRocksElementTypes.ADMIN_STATEMENT
        }
    }

    private fun parseOptionalFromReference(
        builder: PsiBuilder,
        level: Int,
        referenceType: SqlReferenceElementType
    ): Boolean {
        StarRocksParsingUtil.skipNoise(builder)
        if (!StarRocksParsingUtil.consumeWord(builder, "FROM")) {
            return false
        }
        StarRocksParsingUtil.skipNoise(builder)
        return parseReference(builder, level + 1, referenceType)
    }

    private fun parseReference(builder: PsiBuilder, level: Int, referenceType: SqlReferenceElementType): Boolean {
        if (!StarRocksParsingUtil.isIdentifier(builder)) {
            return false
        }
        return SqlGeneratedParserUtil.parseReference(builder, level + 1, referenceType)
    }

    private fun hasWords(builder: PsiBuilder, vararg words: String): Boolean {
        return words.indices.all { index -> wordAt(builder, index) == words[index] }
    }

    private fun consumeOptionalSingleShowAtom(builder: PsiBuilder): Boolean {
        val text = builder.tokenText
        if (builder.eof() || text == null || text == ";" || text == "," || text == ")" || text == "(") {
            return false
        }
        builder.advanceLexer()
        return true
    }

    private fun consumeVariableOrName(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        val parsed = consumeNameLike(builder, allowQualified = true, allowUserHost = false)
        return if (parsed) {
            marker.drop()
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun consumeNameLike(
        builder: PsiBuilder,
        allowQualified: Boolean,
        allowUserHost: Boolean
    ): Boolean {
        var consumed = false
        var expectPart = true
        while (!builder.eof()) {
            val text = builder.tokenText
            when {
                expectPart && isNameLikeToken(builder) -> {
                    builder.advanceLexer()
                    consumed = true
                    expectPart = false
                }
                consumed && allowQualified && text == "." -> {
                    builder.advanceLexer()
                    expectPart = true
                }
                consumed && allowUserHost && text == "@" -> {
                    builder.advanceLexer()
                    expectPart = true
                }
                else -> return consumed
            }
        }
        return consumed
    }

    private fun isNameLikeToken(builder: PsiBuilder): Boolean {
        val text = builder.tokenText ?: return false
        if (text in NAME_TOKEN_EXCLUSIONS) {
            return false
        }
        return StarRocksParsingUtil.isIdentifier(builder) ||
            text.startsWith("'") ||
            text.startsWith("\"") ||
            text.startsWith("@") ||
            text.startsWith(":")
    }

    private fun consumeSingleAtom(builder: PsiBuilder): Boolean {
        val text = builder.tokenText
        if (builder.eof() || text == null || text == ";" || text == "," || text == ")" || text == "(") {
            return false
        }
        builder.advanceLexer()
        return true
    }

    private fun wordAt(builder: PsiBuilder, offset: Int): String? {
        val marker = builder.mark()
        var current = 0
        var result: String? = null
        while (!builder.eof() && builder.tokenText != ";" && current <= offset) {
            val text = builder.tokenText
            if (text != null && text.firstOrNull()?.isLetter() == true) {
                if (current == offset) {
                    result = text.uppercase()
                    break
                }
                current++
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }

    private fun analyze_column_references(builder: PsiBuilder, level: Int, boundaries: Set<String>): Boolean {
        var parsed = false
        while (!builder.eof() && builder.tokenText != ";" && !isAnalyzeColumnBoundary(builder, boundaries)) {
            when {
                builder.tokenText == "," -> builder.advanceLexer()
                SqlGeneratedParserUtil.parseReference(
                    builder,
                    level + 1,
                    SqlCompositeElementTypes.SQL_COLUMN_REFERENCE
                ) -> parsed = true
                else -> builder.advanceLexer()
            }
            StarRocksParsingUtil.skipNoise(builder)
        }
        return parsed
    }

    private fun consumeAnalyzeMode(builder: PsiBuilder): Boolean {
        val marker = builder.mark()
        return if (StarRocksParsingUtil.word(builder) in ANALYZE_MODES) {
            builder.advanceLexer()
            marker.drop()
            true
        } else {
            marker.rollbackTo()
            false
        }
    }

    private fun isAnalyzeColumnBoundary(builder: PsiBuilder, boundaries: Set<String>): Boolean {
        val text = builder.tokenText
        return text in boundaries || StarRocksParsingUtil.word(builder) in boundaries
    }

    private fun PsiBuilder.Marker.rollbackFalse(): Boolean {
        rollbackTo()
        return false
    }

    private val ANALYZE_MODES = setOf("FULL", "SAMPLE")

    private val DESCRIBE_WORDS = setOf("DESC", "DESCRIBE")

    private val ADMIN_SHOW_TARGETS = setOf("FRONTEND", "FRONTENDS", "BACKEND", "BACKENDS", "BROKER", "BROKERS")

    private val ADMIN_SHOW_SUFFIXES = setOf("CONFIG", "STATUS")

    private val EXPLAIN_MODIFIERS = setOf("ANALYZE", "VERBOSE", "LOGICAL", "COSTS")

    private val CONDITION_OPERATORS = setOf("=", "<", ">", "<=", ">=", "<>", "!=", "<=>")

    private val RECOVER_OBJECT_WORDS = setOf("TABLE", "DATABASE", "PARTITION", "REPOSITORY")

    private val NAME_TOKEN_EXCLUSIONS = setOf(",", ";", "(", ")", "=", ":", "+", "-", "*", "/")

    private val ANALYZE_HISTOGRAM_BOUNDARIES = setOf(
        "WITH",
        "PROPERTIES",
        "BUCKETS"
    )
}

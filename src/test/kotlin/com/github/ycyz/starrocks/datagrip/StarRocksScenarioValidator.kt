package com.github.ycyz.starrocks.datagrip

import com.github.ycyz.starrocks.datagrip.completion.StarRocksCompletionCatalog
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksFunctionCatalog
import com.github.ycyz.starrocks.datagrip.database.StarRocksDefinitionProvider
import com.github.ycyz.starrocks.datagrip.database.StarRocksDdlStatements
import com.github.ycyz.starrocks.datagrip.database.StarRocksTypeSystem
import com.github.ycyz.starrocks.datagrip.format.StarRocksFormatterHelper
import com.github.ycyz.starrocks.datagrip.format.StarRocksFormattingProfile
import com.github.ycyz.starrocks.datagrip.highlight.StarRocksSyntaxHighlighter
import com.github.ycyz.starrocks.datagrip.lang.StarRocksColumnNameIndex
import com.github.ycyz.starrocks.datagrip.lang.StarRocksFeature
import com.github.ycyz.starrocks.datagrip.lang.StarRocksDdlParsing
import com.github.ycyz.starrocks.datagrip.lang.StarRocksDmlParsing
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementFactory
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksExpressionParsing
import com.github.ycyz.starrocks.datagrip.lang.StarRocksGeneratedParser
import com.github.ycyz.starrocks.datagrip.lang.StarRocksGrammarMilestone
import com.github.ycyz.starrocks.datagrip.lang.StarRocksHighlightingLexer
import com.github.ycyz.starrocks.datagrip.lang.StarRocksHighlightTokenTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksLexer
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElementType
import com.github.ycyz.starrocks.datagrip.lang.StarRocksParser
import com.github.ycyz.starrocks.datagrip.lang.StarRocksParserScenarioCatalog
import com.github.ycyz.starrocks.datagrip.lang.StarRocksAuxiliaryParsing
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStatementElementSets
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStatementFamily
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStatementWordsClassifier
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStubIndexKeys
import com.github.ycyz.starrocks.datagrip.lang.StarRocksStubElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksTableNameIndex
import com.github.ycyz.starrocks.datagrip.lang.StarRocksTokens
import com.github.ycyz.starrocks.datagrip.resolve.StarRocksLocalSqlContextAnalyzer
import com.intellij.database.model.ObjectKind
import com.intellij.database.types.DasTypeCategory
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.IElementType
import com.intellij.sql.editor.SqlColors
import com.intellij.sql.editor.SqlCodeBlockProviderUtils
import com.intellij.sql.dialects.base.TokenClasses
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlStatement
import com.intellij.sql.psi.SqlTokens.SQL_COMMA
import com.intellij.sql.psi.SqlTokens.SQL_LEFT_PAREN
import com.intellij.sql.psi.SqlTokens.SQL_PERIOD
import com.intellij.sql.psi.SqlTokens.SQL_RIGHT_PAREN
import com.intellij.sql.psi.SqlTokens.SQL_SEMICOLON
import com.intellij.sql.util.SqlTokenRegistry
import java.io.File

object StarRocksScenarioValidator {
    @JvmStatic
    fun main(args: Array<String>) {
        val projectDir = args.firstOrNull()?.let(::File) ?: File(".")
        val testDataDir = projectDir.resolve("src/testData/sql")
        val manifest = loadManifest(testDataDir.resolve("scenarios.properties"))

        checkManifestMatchesCatalog(manifest)
        StarRocksParserScenarioCatalog.SCENARIOS.forEach { scenario ->
            val sql = testDataDir.resolve(scenario.fileName).readText()
            val statements = splitStatements(sql)
            check(statements.isNotEmpty()) { "${scenario.fileName} does not contain SQL statements." }
            validateStatementFamilies(scenario.fileName, statements)
        }

        validateCoreQuery(testDataDir.resolve("query/core-query.sql").readText())
        validateSetOperationAndWindowQuery(testDataDir.resolve("query/set-window-query.sql").readText())
        validateCreateTable(testDataDir.resolve("ddl/create-table.sql").readText())
        validateMaterializedView(testDataDir.resolve("ddl/materialized-view.sql").readText())
        validateCreateView(testDataDir.resolve("ddl/view.sql").readText())
        validateLocalDdlReference(testDataDir.resolve("dml/insert-local-ddl.sql").readText())
        validateComplexTypes(testDataDir.resolve("types/complex-types.sql").readText())
        validateFunctionCatalog()
        validateCompletionCatalog()
        validateLexerKeywordTokens()
        validateSyntaxHighlighterColors()
        validateDialectTokens()
        validateGeneratedGrammarSkeleton()
        validateFormattingProfile()
        validateElementFactory()
        validateDatabaseIntegration(projectDir)
        validateDmlFamilies(testDataDir.resolve("dml/mutations.sql").readText())
    }

    private fun loadManifest(file: File): Map<String, ScenarioMetadata> {
        return file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .associate { line ->
                val parts = line.split("=", limit = 2)
                check(parts.size == 2) { "Invalid scenario manifest line: $line" }
                val metadata = parts[1].split("|", limit = 2)
                check(metadata.size == 2) { "Invalid scenario metadata for ${parts[0]}" }
                parts[0].trim() to ScenarioMetadata(
                    milestone = StarRocksGrammarMilestone.valueOf(metadata[0].trim()),
                    features = metadata[1].split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .mapTo(mutableSetOf()) { StarRocksFeature.valueOf(it) }
                )
            }
    }

    private fun checkManifestMatchesCatalog(manifest: Map<String, ScenarioMetadata>) {
        val catalog = StarRocksParserScenarioCatalog.SCENARIOS.associateBy { it.fileName }
        check(manifest.keys == catalog.keys) {
            "Scenario manifest and catalog differ. manifest=${manifest.keys.sorted()} catalog=${catalog.keys.sorted()}"
        }
        manifest.forEach { (fileName, metadata) ->
            val scenario = catalog.getValue(fileName)
            check(metadata.milestone == scenario.milestone) {
                "$fileName milestone differs between manifest and catalog."
            }
            check(metadata.features == scenario.requiredFeatures) {
                "$fileName feature set differs between manifest and catalog."
            }
        }
    }

    private fun validateStatementFamilies(fileName: String, statements: List<String>) {
        statements.forEach { statement ->
            val words = statementWords(statement)
            val family = StarRocksStatementWordsClassifier.classify(words)
            check(family != null) { "$fileName has an unclassified statement: ${statement.take(80)}" }
        }
    }

    private fun validateCoreQuery(sql: String) {
        check(sql.contains("QUALIFY", ignoreCase = true)) { "Core query fixture must cover QUALIFY." }
        check(sql.contains("CAST('20200101' AS BIGINT)", ignoreCase = true)) {
            "Core query fixture must cover CAST(... AS BIGINT)."
        }
        check(sql.contains("GROUPING SETS", ignoreCase = true)) {
            "Core query fixture must cover GROUPING SETS."
        }
        val context = StarRocksLocalSqlContextAnalyzer.analyze(sql)
        check(context.queryTableReferences.any { it.tableFunction && it.normalizedName == "unnest" }) {
            "Core query fixture must expose UNNEST as a table function reference."
        }
    }

    private fun validateSetOperationAndWindowQuery(sql: String) {
        check(sql.contains("UNION ALL", ignoreCase = true)) {
            "Set/window query fixture must cover UNION ALL."
        }
        check(sql.contains("EXCEPT", ignoreCase = true)) {
            "Set/window query fixture must cover EXCEPT."
        }
        check(sql.contains("WINDOW recent_orders AS", ignoreCase = true)) {
            "Set/window query fixture must cover named WINDOW definitions."
        }
        val statements = splitStatements(sql)
        check(statements.size == 1) { "Set/window fixture should be a single compound query statement." }
        check(StarRocksStatementWordsClassifier.classify(statementWords(statements.single())) == StarRocksStatementFamily.QUERY) {
            "Set/window fixture must classify as a QUERY statement."
        }
        val context = StarRocksLocalSqlContextAnalyzer.analyze(sql)
        check(context.ctes.map { it.normalizedName }.containsAll(listOf("base", "archived"))) {
            "Set/window fixture CTE names were not captured."
        }
        val references = context.queryTableReferences.map { it.normalizedName }
        check(references.containsAll(listOf("base", "archived", "dws.refund_orders"))) {
            "Set/window fixture table references are incomplete: $references"
        }
    }

    private fun validateCreateTable(sql: String) {
        val context = StarRocksLocalSqlContextAnalyzer.analyze(sql)
        val table = context.resolveTable("dws.dws_trade_sale_by_order_ri")
        check(table != null) { "CREATE TABLE fixture did not declare the expected table." }
        check(table.columns.map { it.normalizedName }.containsAll(listOf("biz_date", "store_id", "order_id"))) {
            "CREATE TABLE fixture did not expose expected local columns."
        }
        val ctas = context.resolveTable("dws.dws_trade_sale_by_order_ctas")
        check(ctas != null) { "CREATE TABLE AS SELECT fixture did not declare the expected CTAS table." }
        check(ctas.columns.isEmpty()) { "CTAS table without explicit column list should not steal SELECT expressions as columns." }
        check(context.queryTableReferences.any { it.normalizedName == "dws.dws_trade_sale_by_order_ri" }) {
            "CTAS source table was not captured from AS SELECT."
        }
    }

    private fun validateMaterializedView(sql: String) {
        val statements = splitStatements(sql)
        check(StarRocksStatementWordsClassifier.classify(statementWords(statements.first())) == StarRocksStatementFamily.MATERIALIZED_VIEW) {
            "Materialized view fixture must classify as MATERIALIZED_VIEW."
        }
        val context = StarRocksLocalSqlContextAnalyzer.analyze(sql)
        check(context.queryTableReferences.any { it.normalizedName == "dws.dws_trade_sale_by_order_ri" }) {
            "Materialized view AS SELECT source table was not captured."
        }
    }

    private fun validateCreateView(sql: String) {
        val statements = splitStatements(sql)
        check(StarRocksStatementWordsClassifier.classify(statementWords(statements.first())) == StarRocksStatementFamily.VIEW) {
            "View fixture must classify as VIEW."
        }
        check(StarRocksStatementWordsClassifier.classify(statementWords("CREATE OR REPLACE VIEW v AS SELECT 1")) == StarRocksStatementFamily.VIEW) {
            "CREATE OR REPLACE VIEW must classify as VIEW."
        }
        val context = StarRocksLocalSqlContextAnalyzer.analyze(sql)
        val view = context.resolveTable("dws.v_trade_sale_summary")
        check(view != null) { "CREATE VIEW fixture did not declare the expected local view." }
        check(view.columns.map { it.normalizedName }.containsAll(listOf("biz_date", "order_id", "total_amount"))) {
            "CREATE VIEW fixture did not expose expected view columns."
        }
        check(context.queryTableReferences.any { it.normalizedName == "dws.dws_trade_sale_by_order_ri" }) {
            "View AS SELECT source table was not captured."
        }
        check(context.queryTableReferences.any { it.normalizedName == "dws.v_trade_sale_summary" }) {
            "Query reference to the local view was not captured."
        }
    }

    private fun validateLocalDdlReference(sql: String) {
        val context = StarRocksLocalSqlContextAnalyzer.analyze(sql)
        val insert = context.dmlTargets.singleOrNull { it.statementKind == "INSERT" }
        check(insert != null) { "Expected one INSERT target in local DDL fixture." }
        check(insert.resolvedTable != null) { "INSERT target did not resolve to earlier local CREATE TABLE." }
        check(insert.resolvedColumns.map { it.normalizedName }.containsAll(listOf("biz_date", "store_id", "order_id", "sale_time"))) {
            "Resolved INSERT target columns are incomplete."
        }
    }

    private fun validateComplexTypes(sql: String) {
        val context = StarRocksLocalSqlContextAnalyzer.analyze(sql)
        val table = context.resolveTable("complex_type_sample")
        check(table != null) { "Complex type fixture did not declare complex_type_sample." }
        val columnsByName = table.columns.associateBy { it.normalizedName }
        check(columnsByName["tags"]?.typeText?.contains("ARRAY", ignoreCase = true) == true) {
            "ARRAY type was not captured for tags."
        }
        check(columnsByName["attributes"]?.typeText?.contains("MAP", ignoreCase = true) == true) {
            "MAP type was not captured for attributes."
        }
        check(columnsByName["profile"]?.typeText?.contains("STRUCT", ignoreCase = true) == true) {
            "STRUCT type was not captured for profile."
        }
    }

    private fun validateFunctionCatalog() {
        listOf("CAST", "ROW_NUMBER", "UNNEST", "BITMAP_UNION_INT", "GET_JSON_STRING").forEach { function ->
            check(StarRocksFunctionCatalog.find(function) != null) {
                "Function catalog is missing $function."
            }
        }
        check(StarRocksFunctionCatalog.FUNCTIONS.size >= 60) {
            "Function catalog should cover common StarRocks function families."
        }
        listOf("ARRAY_JOIN", "BITMAP_COUNT", "JSON_LENGTH", "WINDOW_FUNNEL").forEach { function ->
            check(function in StarRocksFunctionCatalog.BUILTIN_FUNCTION_NAMES) {
                "Builtin function name catalog is missing $function."
            }
        }
        check(StarRocksFunctionCatalog.BUILTIN_FUNCTION_NAMES.size >= 450) {
            "Builtin function name catalog should track the StarRocks server function set."
        }
    }

    private fun validateCompletionCatalog() {
        listOf("ARRAY_JOIN", "BITMAP_COUNT", "JSON_LENGTH", "WINDOW_FUNNEL").forEach { function ->
            check(function in StarRocksCompletionCatalog.FUNCTIONS) {
                "Completion catalog should include official builtin function $function."
            }
        }
        check(StarRocksCompletionCatalog.FUNCTIONS.size >= StarRocksFunctionCatalog.BUILTIN_FUNCTION_NAMES.size) {
            "Completion catalog should expose the full builtin function name set."
        }
    }

    private fun validateLexerKeywordTokens() {
        listOf(
            "SELECT",
            "INSERT",
            "WITH",
            "SET",
            "ALL",
            "EXCEPT",
            "INTERSECT",
            "FROM",
            "WHERE",
            "AND",
            "LIMIT",
            "JOIN",
            "ORDER",
            "GROUP",
            "HAVING",
            "QUALIFY",
            "INTERVAL",
            "INTO",
            "ASC",
            "DESC",
            "WINDOW"
        ).forEach { keyword ->
            val keywordLexer = StarRocksLexer()
            keywordLexer.start(keyword, 0, keyword.length, 0)
            check(keywordLexer.tokenType == SqlTokenRegistry.getType(keyword)) {
                "StarRocks lexer should classify official keyword $keyword as its registered platform SQL token."
            }
        }

        listOf("select", "FrOm", "where", "limit").forEach { keyword ->
            val keywordLexer = StarRocksLexer()
            keywordLexer.start(keyword, 0, keyword.length, 0)
            check(keywordLexer.tokenType == SqlTokenRegistry.getType(keyword.uppercase())) {
                "StarRocks lexer should classify $keyword case-insensitively as its registered platform SQL token."
            }
        }

        val highlightedTokens = lexSignificantTokens("SELECT f(a.b, 1) FROM t WHERE a > 0 LIMIT 1;")
        listOf("SELECT", "FROM", "WHERE", "LIMIT").forEach { keyword ->
            check(SqlTokenRegistry.getType(keyword) in highlightedTokens) {
                "StarRocks lexer should emit registered platform keyword token $keyword for SQL highlighter."
            }
        }
        listOf(SQL_LEFT_PAREN, SQL_RIGHT_PAREN, SQL_PERIOD, SQL_COMMA, SQL_SEMICOLON).forEach { token ->
            check(token in highlightedTokens) {
                "StarRocks lexer should emit $token for SQL highlighter."
            }
        }

        val psiTokens = lexSignificantTokens("SELECT IF(flag, 1, 0), SUM(price) FROM t;")
        check(StarRocksHighlightTokenTypes.FUNCTION !in psiTokens) {
            "StarRocks PSI lexer should keep function calls on platform SQL tokens."
        }

        val functionTokens = lexSignificantTokens(
            sql = "SELECT IF(flag, 1, 0), SUM(price), ARRAY_JOIN(tags, ','), WINDOW_FUNNEL(ts, event) FROM t;",
            useHighlightingLexer = true
        )
        check(StarRocksHighlightTokenTypes.FUNCTION in functionTokens) {
            "StarRocks lexer should emit function tokens for builtin function calls."
        }

        val userFunctionTokens = lexSignificantTokens(
            sql = "SELECT my_udf(payload), analytics.custom_score(id) FROM t;",
            useHighlightingLexer = true
        )
        check(StarRocksHighlightTokenTypes.FUNCTION in userFunctionTokens) {
            "StarRocks lexer should emit function tokens for user-defined function calls."
        }

        val functionLikeKeywordTexts = lexSignificantTokenTexts(
            sql = "SELECT EXTRACT(DAY FROM ts), GROUPING(k), GROUPING_ID(k), PERCENTILE(v, 0.95), SUM(v) OVER (PARTITION BY k) FROM t;",
            tokenType = StarRocksHighlightTokenTypes.FUNCTION,
            useHighlightingLexer = true
        )
        check(functionLikeKeywordTexts.containsAll(listOf("EXTRACT", "GROUPING", "GROUPING_ID", "PERCENTILE", "SUM"))) {
            "StarRocks lexer should highlight function-like keyword calls as functions. Actual: $functionLikeKeywordTexts"
        }
        check("OVER" !in functionLikeKeywordTexts) {
            "StarRocks lexer must not misclassify window clause keywords as functions."
        }

        val typeTokens = lexSignificantTokens(
            sql = "CREATE TABLE t (id BIGINT, attrs MAP<STRING, JSON>, amount DECIMAL(18, 2));",
            useHighlightingLexer = true
        )
        check(StarRocksHighlightTokenTypes.DATA_TYPE in typeTokens) {
            "StarRocks lexer should emit data type tokens for StarRocks type names."
        }

        val variableAndParameterTokens = lexSignificantTokens(
            sql = "SELECT @tenant, @@session.query_timeout, :limit, \${biz_date}, ? FROM t;",
            useHighlightingLexer = true
        )
        check(StarRocksHighlightTokenTypes.VARIABLE in variableAndParameterTokens) {
            "StarRocks lexer should emit variable tokens for user and system variables."
        }
        check(StarRocksHighlightTokenTypes.PARAMETER in variableAndParameterTokens) {
            "StarRocks lexer should emit parameter tokens for named and positional parameters."
        }
    }

    private fun validateSyntaxHighlighterColors() {
        val highlighter = StarRocksSyntaxHighlighter(project = null, file = null)
        check(SqlColors.SQL_PROCEDURE in highlighter.getTokenHighlights(StarRocksHighlightTokenTypes.FUNCTION)) {
            "StarRocks function token should use SQL procedure/function highlighting."
        }
        check(SqlColors.SQL_TYPE in highlighter.getTokenHighlights(StarRocksHighlightTokenTypes.DATA_TYPE)) {
            "StarRocks data type token should use SQL type highlighting."
        }
        check(SqlColors.SQL_VARIABLE in highlighter.getTokenHighlights(StarRocksHighlightTokenTypes.VARIABLE)) {
            "StarRocks variable token should use SQL variable highlighting."
        }
        check(SqlColors.SQL_PARAMETER in highlighter.getTokenHighlights(StarRocksHighlightTokenTypes.PARAMETER)) {
            "StarRocks parameter token should use SQL parameter highlighting."
        }
        check(SqlColors.SQL_KEYWORD in highlighter.getTokenHighlights(SqlTokenRegistry.getType("SELECT"))) {
            "StarRocks registered keyword token should keep platform SQL keyword highlighting."
        }
        val highlightLexer = highlighter.highlightingLexer
        check(highlightLexer is StarRocksHighlightingLexer) {
            "StarRocks syntax highlighter must use a dedicated highlighting lexer, not the parser lexer."
        }
        highlightLexer.start("SELECT JSON_LENGTH(payload), BITMAP_COUNT(bm) FROM t;")
        val tokenTypes = mutableListOf<IElementType>()
        while (highlightLexer.tokenType != null) {
            highlightLexer.tokenType?.let(tokenTypes::add)
            highlightLexer.advance()
        }
        check(StarRocksHighlightTokenTypes.FUNCTION in tokenTypes) {
            "StarRocks syntax highlighter should use the highlighting lexer with builtin function tokens."
        }
    }

    private fun lexSignificantTokens(sql: String, useHighlightingLexer: Boolean = false): List<IElementType> {
        val lexer = if (useHighlightingLexer) StarRocksHighlightingLexer() else StarRocksLexer()
        lexer.start(sql, 0, sql.length, 0)
        val tokens = mutableListOf<IElementType>()
        while (lexer.tokenType != null) {
            val token = lexer.tokenType
            if (token != null && token != WHITE_SPACE) {
                tokens += token
            }
            lexer.advance()
        }
        return tokens
    }

    private fun lexSignificantTokenTexts(
        sql: String,
        tokenType: IElementType,
        useHighlightingLexer: Boolean = false
    ): List<String> {
        val lexer = if (useHighlightingLexer) StarRocksHighlightingLexer() else StarRocksLexer()
        lexer.start(sql, 0, sql.length, 0)
        val tokens = mutableListOf<String>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType == tokenType) {
                tokens += sql.substring(lexer.tokenStart, lexer.tokenEnd)
            }
            lexer.advance()
        }
        return tokens
    }

    private fun validateDialectTokens() {
        val classes = TokenClasses(StarRocksTokens::class.java)
        SqlTokenRegistry.initTypeMap(classes.all, null)
        SqlTokenRegistry.initTypeMap(classes.reserved, null)
        val keywords = SqlTokenRegistry.getTokens(classes.all)
        val reservedKeywords = SqlTokenRegistry.getTokens(classes.reserved)
        check("SELECT" in keywords && "QUALIFY" in keywords && "EXCEPT" in keywords && "INTERSECT" in keywords && "WINDOW" in keywords) {
            "StarRocks token class did not expose core StarRocks keywords."
        }
        check("SELECT" in reservedKeywords && "QUALIFY" in reservedKeywords && "EXCEPT" in reservedKeywords &&
            "INTERSECT" in reservedKeywords && "WINDOW" in reservedKeywords) {
            "StarRocks token class did not expose reserved StarRocks keywords."
        }
        check(SqlTokenRegistry.getTokenProvider(classes.reserved).`fun`(SqlTokenRegistry.getType("SELECT").toString()) != null) {
            "StarRocks reserved token provider should resolve SELECT."
        }
        check(StarRocksTokens::class.java.isAssignableFrom(StarRocksElementFactory::class.java)) {
            "StarRocks element factory must implement StarRocksTokens so keyword interfaces are initialized like mature SQL dialects."
        }
        check(SqlCodeBlockProviderUtils.STARTERS.contains(SqlTokenRegistry.getType("IF"))) {
            "Platform SQL block highlighter must load after StarRocks keyword token initialization."
        }
    }

    private fun validateGeneratedGrammarSkeleton() {
        val builderClass = com.intellij.lang.PsiBuilder::class.java
        val levelClass = Int::class.javaPrimitiveType!!
        listOf(
            StarRocksGeneratedParser::class.java.getMethod("statement", builderClass, levelClass),
            StarRocksGeneratedParser::class.java.getMethod("expression", builderClass, levelClass),
            StarRocksGeneratedParser::class.java.getMethod("table_column_list", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("select_clause", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("select_target_list", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("select_target", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("with_clause", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("named_query_definition", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("cte_column_list", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("cte_query", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("where_clause", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("group_by_clause", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("grouping_item", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("having_clause", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("order_by_clause", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("order_by_expression_list", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("order_expression", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("window_clause", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("window_definition", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("limit_clause", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("parenthesized_query_expression", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("qualify_clause", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("table_function_call", builderClass, levelClass),
            StarRocksDmlParsing::class.java.getMethod("insert_statement", builderClass, levelClass),
            StarRocksDdlParsing::class.java.getMethod("type_element", builderClass, levelClass),
            StarRocksDdlParsing::class.java.getMethod("table_column_list", builderClass, levelClass),
            StarRocksDdlParsing::class.java.getMethod("column_definition", builderClass, levelClass),
            StarRocksDdlParsing::class.java.getMethod("properties_clause", builderClass, levelClass),
            StarRocksDdlParsing::class.java.getMethod("create_table_statement", builderClass, levelClass),
            StarRocksDdlParsing::class.java.getMethod("create_materialized_view_statement", builderClass, levelClass),
            StarRocksAuxiliaryParsing::class.java.getMethod("show_statement", builderClass, levelClass),
            StarRocksAuxiliaryParsing::class.java.getMethod("admin_statement", builderClass, levelClass),
            StarRocksAuxiliaryParsing::class.java.getMethod("use_statement", builderClass, levelClass),
            StarRocksAuxiliaryParsing::class.java.getMethod("explain_statement", builderClass, levelClass),
            StarRocksAuxiliaryParsing::class.java.getMethod("describe_statement", builderClass, levelClass),
            StarRocksAuxiliaryParsing::class.java.getMethod("load_statement", builderClass, levelClass),
            StarRocksAuxiliaryParsing::class.java.getMethod("create_resource_statement", builderClass, levelClass),
            StarRocksAuxiliaryParsing::class.java.getMethod("export_statement", builderClass, levelClass),
            StarRocksAuxiliaryParsing::class.java.getMethod("backup_restore_statement", builderClass, levelClass),
            StarRocksExpressionParsing::class.java.getMethod("value_expression", builderClass, levelClass)
        ).forEach { method ->
            check(method.returnType == Boolean::class.javaPrimitiveType) {
                "Generated grammar skeleton method ${method.name} must return boolean like JetBrains generated parsers."
            }
        }
        check(StarRocksGeneratedParser.EXTENDS_SETS_.isEmpty()) {
            "StarRocks generated parser skeleton should expose EXTENDS_SETS_ like JetBrains generated dialect parsers."
        }
        val extendsSetsMethod = StarRocksParser::class.java.getDeclaredMethod("getExtendsTokenSets")
        extendsSetsMethod.isAccessible = true
        check(extendsSetsMethod.invoke(StarRocksParser()) === StarRocksGeneratedParser.EXTENDS_SETS_) {
            "StarRocksParser must expose StarRocksGeneratedParser.EXTENDS_SETS_ like generated JetBrains dialect parsers."
        }
    }

    private fun validateFormattingProfile() {
        check(StarRocksFormattingProfile.USE_PLATFORM_SQL_FORMATTER) {
            "StarRocks formatter must stay wired to the platform SQL formatter."
        }
        check(StarRocksFormattingProfile.USE_GENERIC_SQL_FORMATTER_BRIDGE) {
            "StarRocks formatter should keep the platform GenericSQL bridge for ordinary queries."
        }
        check(!StarRocksFormattingProfile.USE_WHOLE_FILE_STRING_REWRITE) {
            "StarRocks formatter must not rely on whole-file string rewrites."
        }
        check(!StarRocksFormattingProfile.requiresSafeFormatter("SELECT id FROM t WHERE id > 0")) {
            "Ordinary StarRocks queries should still use the platform SQL formatter bridge."
        }
        check(StarRocksFormattingProfile.requiresSafeFormatter("CREATE TABLE t (id BIGINT) DISTRIBUTED BY HASH(id) BUCKETS 1")) {
            "StarRocks table DDL should use safe formatting while DDL PSI formatting is incomplete."
        }
        check(!StarRocksFormattingProfile.requiresSafeFormatter("CREATE VIEW v AS SELECT id FROM t")) {
            "Ordinary CREATE VIEW should use the platform SQL formatter once it has generated PSI."
        }
        check(StarRocksFormattingProfile.requiresSafeFormatter("CREATE MATERIALIZED VIEW mv AS SELECT id FROM t")) {
            "StarRocks materialized view DDL should use safe formatting while MV PSI formatting is incomplete."
        }
        check(StarRocksFormattingProfile.QUERY_CLAUSE_ORDER.indexOf("HAVING") <
            StarRocksFormattingProfile.QUERY_CLAUSE_ORDER.indexOf("QUALIFY")) {
            "QUALIFY must format after HAVING, matching StarRocks query grammar."
        }
        check(SqlCompositeElementTypes.SQL_QUALIFY_CLAUSE in StarRocksFormatterHelper().basicBlockCreation.keys) {
            "Generated StarRocks QUALIFY must use the platform SQL_QUALIFY_CLAUSE formatter block."
        }
        check(SqlCompositeElementTypes.SQL_FUNCTION_CALL in StarRocksFormatterHelper().basicBlockCreation.keys) {
            "Generated StarRocks UNNEST must use the platform SQL_FUNCTION_CALL formatter block."
        }
        check(SqlCompositeElementTypes.SQL_TYPE_ELEMENT in StarRocksFormatterHelper().basicBlockCreation.keys) {
            "Generated StarRocks complex types must use the platform SQL_TYPE_ELEMENT formatter block."
        }
        check(SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT in StarRocksFormatterHelper().basicBlockCreation.keys) {
            "Generated StarRocks CREATE TABLE must use the platform SQL_CREATE_TABLE_STATEMENT formatter block."
        }
        check(SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT in StarRocksFormatterHelper().basicBlockCreation.keys) {
            "Generated StarRocks CREATE VIEW must use the platform SQL_CREATE_VIEW_STATEMENT formatter block."
        }
        check(SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT in StarRocksFormatterHelper().basicBlockCreation.keys) {
            "Generated StarRocks materialized view must use the platform SQL_CREATE_MATERIALIZED_VIEW_STATEMENT formatter block."
        }
        check(SqlCompositeElementTypes.SQL_SELECT_STATEMENT in StarRocksFormatterHelper().basicBlockCreation.keys) {
            "Ordinary StarRocks SELECT must use the platform SQL_SELECT_STATEMENT formatter block."
        }
        check(SqlCompositeElementTypes.SQL_INSERT_STATEMENT in StarRocksFormatterHelper().basicBlockCreation.keys) {
            "Ordinary StarRocks INSERT must use the platform SQL_INSERT_STATEMENT formatter block."
        }
        listOf(
            StarRocksElementTypes.KEY_MODEL_CLAUSE,
            StarRocksElementTypes.COMMENT_CLAUSE,
            StarRocksElementTypes.PARTITION_CLAUSE,
            StarRocksElementTypes.DISTRIBUTION_CLAUSE,
            StarRocksElementTypes.BUCKETS_CLAUSE,
            StarRocksElementTypes.REFRESH_CLAUSE,
            StarRocksElementTypes.PROPERTIES_CLAUSE
        ).forEach { type ->
            check(type in StarRocksFormatterHelper().basicBlockCreation.keys) {
                "Generated StarRocks DDL clause $type must have a platform formatter block."
            }
        }
        check("DISTRIBUTED BY" in StarRocksFormattingProfile.TABLE_DDL_CLAUSE_ORDER) {
            "DDL formatting profile must include StarRocks distribution clauses."
        }
    }

    private fun validateElementFactory() {
        val factory = StarRocksElementFactory()
        listOf(
            StarRocksElementTypes.SELECT_CLAUSE,
            StarRocksElementTypes.SELECT_ITEM,
            StarRocksElementTypes.WITH_CLAUSE,
            StarRocksElementTypes.CTE_DEFINITION,
            StarRocksElementTypes.CTE_COLUMN_LIST,
            StarRocksElementTypes.CTE_QUERY,
            StarRocksElementTypes.WHERE_CLAUSE,
            StarRocksElementTypes.GROUP_BY_CLAUSE,
            StarRocksElementTypes.GROUPING_ITEM,
            StarRocksElementTypes.HAVING_CLAUSE,
            StarRocksElementTypes.PREDICATE_EXPRESSION,
            StarRocksElementTypes.ORDER_BY_CLAUSE,
            StarRocksElementTypes.ORDERING_ITEM,
            StarRocksElementTypes.LIMIT_CLAUSE,
            StarRocksElementTypes.LIMIT_EXPRESSION,
            StarRocksElementTypes.SUBQUERY_EXPRESSION,
            StarRocksElementTypes.VALUES_CLAUSE,
            StarRocksElementTypes.VALUES_ROW,
            StarRocksElementTypes.TABLE_REFERENCE,
            StarRocksElementTypes.SET_OPERATION_CLAUSE,
            StarRocksElementTypes.SET_OPERATOR,
            StarRocksElementTypes.WINDOW_CLAUSE,
            StarRocksElementTypes.WINDOW_DEFINITION,
            StarRocksElementTypes.WINDOW_NAME,
            StarRocksElementTypes.CTE_NAME,
            StarRocksElementTypes.TABLE_ALIAS,
            StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME,
            StarRocksElementTypes.SELECT_ALIAS,
            StarRocksElementTypes.TABLE_REFERENCE_NAME,
            StarRocksElementTypes.TABLE_NAME,
            StarRocksElementTypes.COLUMN_NAME,
            StarRocksElementTypes.KEY_MODEL_CLAUSE,
            StarRocksElementTypes.KEY_COLUMN,
            StarRocksElementTypes.COMMENT_CLAUSE,
            StarRocksElementTypes.PARTITION_CLAUSE,
            StarRocksElementTypes.PARTITION_EXPRESSION,
            StarRocksElementTypes.DISTRIBUTION_CLAUSE,
            StarRocksElementTypes.DISTRIBUTION_EXPRESSION,
            StarRocksElementTypes.BUCKETS_CLAUSE,
            StarRocksElementTypes.REFRESH_CLAUSE
        ).forEach { type ->
            val node = factory.createElementNode(type)
            check(node.elementType == type) { "Element factory created the wrong node type for $type." }
        }
        listOf(
            StarRocksElementTypes.TABLE_DDL_STATEMENT,
            StarRocksElementTypes.VIEW_STATEMENT,
            StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT,
            StarRocksElementTypes.CATALOG_STATEMENT,
            StarRocksElementTypes.RESOURCE_STATEMENT,
            StarRocksElementTypes.LOAD_STATEMENT,
            StarRocksElementTypes.ROUTINE_LOAD_STATEMENT,
            StarRocksElementTypes.TASK_STATEMENT,
            StarRocksElementTypes.EXPORT_STATEMENT,
            StarRocksElementTypes.BACKUP_RESTORE_STATEMENT,
            StarRocksElementTypes.ADMIN_STATEMENT
        ).forEach { type ->
            val node = factory.createElementNode(type)
            check(node is SqlStatement) {
                "StarRocks top-level statement node $type must implement SqlStatement so IDE run actions can find it."
            }
        }
        listOf(
            SqlCompositeElementTypes.SQL_USE_SCHEMA_STATEMENT,
            SqlCompositeElementTypes.SQL_USE_CATALOG_STATEMENT,
            SqlCompositeElementTypes.SQL_USE_NAMESPACE_STATEMENT,
            SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT
        ).forEach { type ->
            val node = factory.createElementNode(type)
            val psi = factory.createCompositeElement(node)
            check(psi is SqlStatement) {
                "Platform statement node $type must stay runnable under the StarRocks element factory."
            }
        }
        check(SqlCompositeElementTypes.SQL_SELECT_STATEMENT in StarRocksStatementElementSets.STATEMENT_TYPES) {
            "Statement scope detection must use platform SELECT statements, not legacy StarRocks query nodes."
        }
        check(SqlCompositeElementTypes.SQL_INSERT_STATEMENT in StarRocksStatementElementSets.STATEMENT_TYPES) {
            "Statement scope detection must use platform INSERT statements, not legacy StarRocks DML nodes."
        }
        val legacyStatementNames = setOf(
            "STARROCKS_QUERY_STATEMENT",
            "STARROCKS_DML_STATEMENT",
            "STARROCKS_UNKNOWN_STATEMENT"
        )
        check(StarRocksStatementElementSets.STATEMENT_TYPES.none { it.toString() in legacyStatementNames }) {
            "Statement scope detection must not depend on legacy lightweight parser statement nodes."
        }
        listOf(
            "TABLE_NAME" to StarRocksElementTypes.TABLE_NAME,
            "COLUMN_NAME" to StarRocksElementTypes.COLUMN_NAME,
            "CTE_NAME" to StarRocksElementTypes.CTE_NAME,
            "CTE_COLUMN_NAME" to StarRocksElementTypes.CTE_COLUMN_NAME,
            "TABLE_ALIAS" to StarRocksElementTypes.TABLE_ALIAS,
            "TABLE_ALIAS_COLUMN_NAME" to StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME,
            "SELECT_ALIAS" to StarRocksElementTypes.SELECT_ALIAS,
            "WINDOW_NAME" to StarRocksElementTypes.WINDOW_NAME
        ).forEach { (name, type) ->
            check(type is StarRocksNamedStubElementType) {
                "StarRocks $name must be a registered stub element type."
            }
            check(type.externalId == "sql.STARROCKS_$name") {
                "StarRocks $name stub external id is unstable."
            }
        }
        listOf(
            StarRocksStubElementTypes.STARROCKS_TABLE_NAME,
            StarRocksStubElementTypes.STARROCKS_COLUMN_NAME,
            StarRocksStubElementTypes.STARROCKS_CTE_NAME,
            StarRocksStubElementTypes.STARROCKS_CTE_COLUMN_NAME,
            StarRocksStubElementTypes.STARROCKS_TABLE_ALIAS,
            StarRocksStubElementTypes.STARROCKS_TABLE_ALIAS_COLUMN_NAME,
            StarRocksStubElementTypes.STARROCKS_SELECT_ALIAS,
            StarRocksStubElementTypes.STARROCKS_WINDOW_NAME
        ).forEach { type ->
            check(type.externalId.startsWith("sql.STARROCKS_")) {
                "StarRocks stub holder exposed an unstable external id: ${type.externalId}"
            }
        }
        check(StarRocksNamedStubElementType::class.java.declaredFields.none { it.name == "externalName" }) {
            "StarRocks stub external ids must not depend on subclass constructor fields during IStubElementType initialization."
        }
        check(StarRocksTableNameIndex.INDEX_NAME == "starrocks.table.name") {
            "StarRocks table name stub index id is unstable."
        }
        check(StarRocksColumnNameIndex.INDEX_NAME == "starrocks.column.name") {
            "StarRocks column name stub index id is unstable."
        }
        check(StarRocksNamedStubElement.normalizeName("`dws`.`sample_orders`") == "dws.sample_orders") {
            "StarRocks named stubs must normalize qualified backtick identifiers."
        }
        check(StarRocksNamedStubElement.normalizeName("`order``id`") == "order`id") {
            "StarRocks named stubs must unescape backtick identifiers."
        }
        check(StarRocksDialect.INSTANCE.isOperatorSupported(null)) {
            "StarRocks dialect must tolerate the nullable operator token passed by the platform SqlParser."
        }
        check(StarRocksStubIndexKeys.tableKeys("DWS.Sample_Orders").containsAll(
            listOf("DWS.Sample_Orders", "dws.sample_orders", "Sample_Orders", "sample_orders")
        )) {
            "StarRocks table name index keys must support full, short, and normalized lookups."
        }
    }

    private fun validateDatabaseIntegration(projectDir: File) {
        val typeSystem = StarRocksTypeSystem()
        check(typeSystem.getNormalizedTypeName("integer") == "INT") {
            "StarRocks type system should normalize INTEGER to INT."
        }
        check(typeSystem.getNormalizedTypeName("decimal(10, 2)") == "DECIMAL128") {
            "StarRocks type system should normalize DECIMAL to DECIMAL128."
        }
        check(typeSystem.getNormalizedTypeName("array<bigint>") == "ARRAY") {
            "StarRocks type system should recognize ARRAY complex types."
        }
        check(typeSystem.getDefaultTypeName(DasTypeCategory.INTEGER) == "BIGINT") {
            "StarRocks integer default type should be BIGINT."
        }
        check("JSON" in StarRocksTypeSystem.SCALAR_TYPES && "STRUCT" in StarRocksTypeSystem.COMPLEX_TYPES) {
            "StarRocks type catalog should include JSON and STRUCT."
        }

        val quoted = StarRocksDefinitionProvider.quoteIdentifier("dws`schema")
        check(quoted == "`dws``schema`") { "StarRocks identifier quoting should escape backticks." }
        check(StarRocksDdlStatements.showCreateStatement(ObjectKind.TABLE, "`db`.`tbl`") == "SHOW CREATE TABLE `db`.`tbl`") {
            "StarRocks table DDL provider should use SHOW CREATE TABLE."
        }
        check(StarRocksDdlStatements.showCreateStatement(ObjectKind.MAT_VIEW, "`db`.`mv`") ==
            "SHOW CREATE MATERIALIZED VIEW `db`.`mv`") {
            "StarRocks materialized view DDL provider should use SHOW CREATE MATERIALIZED VIEW."
        }

        val pluginXml = projectDir.resolve("src/main/resources/META-INF/plugin.xml").readText()
        listOf(
            "<typeSystem dbms=\"STARROCKS\"",
            "<definitionProvider dbms=\"STARROCKS\"",
            "<introspector dbms=\"STARROCKS\"",
            "<jdbcSourceLoader dbms=\"STARROCKS\"",
            "<jdbcMetadataWrapper dbms=\"STARROCKS\"",
            "<sql.dialectCodeStyleProvider implementation=\"com.github.ycyz.starrocks.datagrip.format.StarRocksCodeStyleProvider\"",
            "<lang.formatter language=\"StarRocks\" implementationClass=\"com.github.ycyz.starrocks.datagrip.format.StarRocksFormattingModelBuilder\"",
            "<sql.formatterHelper language=\"StarRocks\" implementationClass=\"com.github.ycyz.starrocks.datagrip.format.StarRocksFormatterHelper\"",
            "<stubElementTypeHolder class=\"com.github.ycyz.starrocks.datagrip.lang.StarRocksStubElementTypes\" externalIdPrefix=\"sql.\"",
            "<stubIndex implementation=\"com.github.ycyz.starrocks.datagrip.lang.StarRocksTableNameIndex\"",
            "<stubIndex implementation=\"com.github.ycyz.starrocks.datagrip.lang.StarRocksColumnNameIndex\""
        ).forEach { marker ->
            check(marker in pluginXml) { "plugin.xml is missing database integration marker $marker." }
        }
    }

    private fun validateDmlFamilies(sql: String) {
        val families = splitStatements(sql).map { StarRocksStatementWordsClassifier.classify(statementWords(it)) }
        check(families == listOf(
            StarRocksStatementFamily.DML,
            StarRocksStatementFamily.DML,
            StarRocksStatementFamily.DML,
            StarRocksStatementFamily.DML
        )) {
            "DML mutations fixture did not classify all mutation statements as DML: $families"
        }
    }

    private fun splitStatements(sql: String): List<String> {
        val result = mutableListOf<String>()
        var start = 0
        var index = 0
        var parenDepth = 0
        var quote: Char? = null
        while (index < sql.length) {
            val char = sql[index]
            if (quote != null) {
                if (char == quote) {
                    if (index + 1 < sql.length && sql[index + 1] == quote) {
                        index += 2
                        continue
                    }
                    quote = null
                } else if (char == '\\') {
                    index++
                }
                index++
                continue
            }
            when (char) {
                '\'', '"' -> quote = char
                '(' -> parenDepth++
                ')' -> if (parenDepth > 0) parenDepth--
                ';' -> if (parenDepth == 0) {
                    sql.substring(start, index).trim().takeIf { it.isNotBlank() }?.let(result::add)
                    start = index + 1
                }
            }
            index++
        }
        sql.substring(start).trim().takeIf { it.isNotBlank() }?.let(result::add)
        return result
    }

    private fun statementWords(statement: String): List<String> {
        val words = mutableListOf<String>()
        var index = 0
        while (index < statement.length) {
            val char = statement[index]
            when {
                char == '\'' || char == '"' -> index = consumeQuoted(statement, index, char)
                char == '`' -> index = consumeQuoted(statement, index, char)
                char == '-' && statement.getOrNull(index + 1) == '-' -> {
                    index += 2
                    while (index < statement.length && statement[index] != '\n' && statement[index] != '\r') {
                        index++
                    }
                }
                char == '/' && statement.getOrNull(index + 1) == '*' -> {
                    index += 2
                    while (index + 1 < statement.length && !(statement[index] == '*' && statement[index + 1] == '/')) {
                        index++
                    }
                    index = (index + 2).coerceAtMost(statement.length)
                }
                char == '_' || char.isLetter() -> {
                    val start = index
                    index++
                    while (index < statement.length && (statement[index] == '_' || statement[index].isLetterOrDigit())) {
                        index++
                    }
                    words += statement.substring(start, index).uppercase()
                }
                else -> index++
            }
        }
        return words
    }

    private fun consumeQuoted(text: String, start: Int, quote: Char): Int {
        var index = start + 1
        while (index < text.length) {
            val char = text[index]
            index++
            if (char == quote) {
                if (index < text.length && text[index] == quote) {
                    index++
                    continue
                }
                break
            }
            if (char == '\\' && index < text.length) {
                index++
            }
        }
        return index
    }
}

private data class ScenarioMetadata(
    val milestone: StarRocksGrammarMilestone,
    val features: Set<StarRocksFeature>
)

package com.github.ycyz.starrocks.datagrip

import com.github.ycyz.starrocks.datagrip.completion.StarRocksCompletionCatalog
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.github.ycyz.starrocks.datagrip.highlight.StarRocksSyntaxHighlighter
import com.github.ycyz.starrocks.datagrip.lang.StarRocksColumnNameIndex
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksHighlightingLexer
import com.github.ycyz.starrocks.datagrip.lang.StarRocksHighlightTokenTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.github.ycyz.starrocks.datagrip.lang.StarRocksTableNameIndex
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.ASTNode
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.sql.editor.SqlColors
import com.intellij.sql.editor.SqlCodeBlockProviderUtils
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlStatement
import com.intellij.sql.util.SqlTokenRegistry
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import javax.swing.Icon

class StarRocksIdeFixtureTest : BasePlatformTestCase() {
    fun testEditorFixtureOpensStarRocksFileAndBuildsPsi() {
        val file = configureStarRocksText(SET_WINDOW_SQL)

        assertSame(StarRocksDialect.INSTANCE, file.language)
        assertNotNull(myFixture.editor)
        myFixture.doHighlighting()

        assertContainsElement(file, SqlCompositeElementTypes.SQL_SELECT_STATEMENT)
    }

    fun testPlainSelectUsesPlatformSelectStatementPsi() {
        val file = configureStarRocksText("SELECT order_id, amount FROM dws.sample_orders WHERE amount > 0;")

        assertContainsElement(file, SqlCompositeElementTypes.SQL_SELECT_STATEMENT)
    }

    fun testPlainSelectWithCommonClausesHighlightsWithoutErrors() {
        val file = configureStarRocksText(
            """
            select
                sum(pay_amt),
                sum(settle_amt)
            from dws.dws_trade_online_di
            WHERE biz_date >= '2026-01-01'
              AND biz_date <= '2026-06-01';

            SELECT *
            FROM dwd.middle_store_item_require_price_history
            WHERE right(require_price, 3) > 0
            LIMIT 10;
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()

        assertContainsElement(file, SqlCompositeElementTypes.SQL_SELECT_STATEMENT)
        assertNoErrorHighlights(highlights)
    }

    fun testPlainSelectWithVariablesUsesPlatformSelectStatementPsi() {
        val file = configureStarRocksText("SELECT @tenant, @@session.query_timeout, :limit, ${'$'}{biz_date}, ? FROM t;")

        myFixture.doHighlighting()

        assertContainsElement(file, SqlCompositeElementTypes.SQL_SELECT_STATEMENT)
    }

    fun testPlainInsertUsesPlatformInsertStatementPsi() {
        val file = configureStarRocksText("INSERT INTO dws.sample_orders SELECT order_id, amount FROM ods.orders;")
        val statements = PsiTreeUtil.findChildrenOfType(file, SqlStatement::class.java).toList()

        assertTrue(
            "Expected plain INSERT to produce platform SqlStatement PSI. Actual element types: ${elementTypeNames(file)}",
            statements.isNotEmpty()
        )
        assertTrue(
            "Expected platform SqlStatement PSI to contain INSERT text. Actual statements: ${statements.map { it.text }}",
            statements.any { it.text.contains("INSERT", ignoreCase = true) }
        )
    }

    fun testInsertValuesBuildsStructuredValuesPsi() {
        val file = configureStarRocksText("INSERT INTO dws.sample_orders VALUES (1, 10.5), (2, 20.5);")

        assertContainsElement(file, SqlCompositeElementTypes.SQL_INSERT_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.VALUES_CLAUSE)
        assertElementCountAtLeast(file, StarRocksElementTypes.VALUES_ROW, 2)
    }

    fun testStarRocksStatementEntrypointsUseConcretePsi() {
        val file = configureStarRocksText(
            """
            SHOW DATABASES;
            VALUES (1, 2, 3);
            USE dws;
            EXPLAIN SELECT order_id FROM dws.sample_orders;
            DESC dws.sample_orders;
            ADMIN SHOW FRONTEND CONFIG;
            ANALYZE TABLE dws.sample_orders;
            KILL 10001;
            SYNC;
            ALTER TABLE dws.sample_orders ADD COLUMN remark VARCHAR(20);
            ALTER VIEW dws.v_sample_orders AS SELECT order_id FROM dws.sample_orders;
            ALTER MATERIALIZED VIEW dws.mv_sample_orders ACTIVE;
            DROP TABLE dws.old_orders;
            DROP VIEW dws.old_view;
            DROP MATERIALIZED VIEW dws.old_mv;
            LOAD LABEL db1.label1 (DATA INFILE("s3://bucket/path/file.csv") INTO TABLE fact_order) WITH BROKER;
            CANCEL LOAD FROM db1 WHERE LABEL = "label1";
            CREATE RESOURCE spark_resource PROPERTIES ("type" = "spark");
            EXPORT TABLE dws.sample_orders TO "s3://bucket/export/";
            BACKUP SNAPSHOT dws.snapshot1 TO repo_s3 ON (dws.sample_orders);
            """.trimIndent()
        )

        val statements = PsiTreeUtil.findChildrenOfType(file, SqlStatement::class.java).toList()

        assertEquals(
            "Every StarRocks statement entry should produce runnable SqlStatement PSI. Actual statements: " +
                statements.map { "${it.node.elementType}:${it.text.take(60)}" } +
                " Element types: ${elementTypeNames(file)}",
            20,
            statements.size
        )
        assertContainsElement(file, SqlCompositeElementTypes.SQL_SELECT_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.VALUES_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.VALUES_ROW)
        assertContainsElement(file, StarRocksElementTypes.TABLE_DDL_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.VIEW_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.ADMIN_STATEMENT)
        assertContainsElement(file, SqlCompositeElementTypes.SQL_USE_SCHEMA_STATEMENT)
        assertContainsElement(file, SqlCompositeElementTypes.SQL_EXPLAIN_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.LOAD_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.RESOURCE_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.EXPORT_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.BACKUP_RESTORE_STATEMENT)
    }

    fun testTopLevelStatementsUsePlatformSqlStatementPsi() {
        val file = configureStarRocksText(LOCAL_TABLE_REFERENCE_SQL)

        val statements = PsiTreeUtil.findChildrenOfType(file, SqlStatement::class.java).toList()

        assertTrue("Expected platform SQL statements in StarRocks PSI.", statements.isNotEmpty())
        assertTrue(
            "Run current statement action needs top-level PSI to implement SqlStatement. Actual classes: ${statements.map { it::class.java.name }}",
            statements.all { it is SqlStatement }
        )
        assertContainsElement(file, SqlCompositeElementTypes.SQL_SELECT_STATEMENT)

        val statementElement = file.findElementAt(file.text.indexOf("h.amount"))
        val containingStatement = PsiTreeUtil.getParentOfType(statementElement, SqlStatement::class.java)

        assertNotNull("Run current statement action should find a SqlStatement at the caret.", containingStatement)
        assertTrue(
            "Expected caret statement to contain the SELECT query. Actual text: ${containingStatement?.text}",
            containingStatement?.text?.contains("SELECT o.order_id, h.amount") == true
        )
    }

    fun testProblematicInsertWithCtesHighlightsWithoutHanging() {
        val file = configureStarRocksText(PROBLEMATIC_INSERT_WITH_CTES_SQL)

        val highlights = myFixture.doHighlighting()

        assertSame(StarRocksDialect.INSTANCE, file.language)
        assertTrue("Highlighting should complete for DML WITH query files.", highlights.isNotEmpty())
    }

    fun testRealisticInsertWithCtesAndWindowFramesHighlightsWithoutHanging() {
        val file = configureStarRocksText(REALISTIC_INSERT_WITH_CTES_AND_WINDOWS_SQL)

        val highlights = myFixture.doHighlighting()

        assertSame(StarRocksDialect.INSTANCE, file.language)
        assertTrue("Highlighting should complete for realistic scheduler INSERT files.", highlights.isNotEmpty())
    }

    fun testParserBuildsNestedQueryExpressionNodes() {
        val file = configureStarRocksText(NESTED_QUERY_SQL)

        assertSame(StarRocksDialect.INSTANCE, file.language)
        assertElementCountAtLeast(file, StarRocksElementTypes.SUBQUERY_EXPRESSION, 3)
        assertElementCountAtLeast(file, StarRocksElementTypes.SELECT_CLAUSE, 5)
        assertElementCountAtLeast(file, StarRocksElementTypes.FROM_CLAUSE, 5)
        assertElementCountAtLeast(file, StarRocksElementTypes.WHERE_CLAUSE, 4)
    }

    fun legacyParserMarksTableReferenceNames() {
        val file = configureStarRocksText(LOCAL_TABLE_REFERENCE_SQL)

        val references = psiElements(file, StarRocksElementTypes.TABLE_REFERENCE_NAME).map { it.text }

        assertTrue("Expected FROM table reference names in PSI: $references", "dws.sample_orders" in references)
        assertTrue("Expected DML target table reference name in PSI: $references", "sample_orders" in references)
    }

    fun legacyParserUsesStubPsiForTableAndColumnNames() {
        val file = configureStarRocksText(CREATE_TABLE_SQL)

        val tableNames = namedStubNames(file, StarRocksElementTypes.TABLE_NAME)
        val columnNames = namedStubNames(file, StarRocksElementTypes.COLUMN_NAME)

        assertTrue("Expected StarRocks table name PSI to use named stubs: $tableNames", "dws.sample_orders" in tableNames)
        assertTrue("Expected StarRocks column name PSI to use named stubs: $columnNames", columnNames.containsAll(listOf("order_id", "amount")))
    }

    fun testParserUsesStubPsiForLocalNamedDefinitions() {
        val setWindowFile = configureStarRocksText(SET_WINDOW_SQL)

        val cteNames = namedStubNames(setWindowFile, StarRocksElementTypes.CTE_NAME)
        val windowNames = namedStubNames(setWindowFile, StarRocksElementTypes.WINDOW_NAME)

        assertTrue("Expected StarRocks CTE name PSI to use named stubs: $cteNames", "base" in cteNames)
        assertTrue("Expected StarRocks window name PSI to use named stubs: $windowNames", "recent_orders" in windowNames)

        val tableAliasFile = configureStarRocksText(LOCAL_TABLE_REFERENCE_SQL)

        val aliases = namedStubNames(tableAliasFile, StarRocksElementTypes.TABLE_ALIAS)

        assertTrue("Expected StarRocks table alias PSI to use named stubs: $aliases", aliases.containsAll(listOf("o", "h")))
    }

    fun testParserMarksSelectAliasesAsNamedStubs() {
        val file = configureStarRocksText(SELECT_ALIAS_SQL)

        val aliases = namedStubNames(file, StarRocksElementTypes.SELECT_ALIAS)
        val selectItemColumnReferences = psiElements(file, StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            .filter { hasAncestor(it, StarRocksElementTypes.SELECT_ITEM) }
            .map { it.text }

        assertTrue(
            "Expected SELECT aliases to use named stubs: $aliases",
            aliases.containsAll(listOf("total_amount", "order_key", "gross_amount", "rn"))
        )
        assertFalse("SELECT alias should not be parsed as a column reference inside SELECT items: $selectItemColumnReferences", "total_amount" in selectItemColumnReferences)
        assertFalse("Implicit SELECT alias should not be parsed as a column reference inside SELECT items: $selectItemColumnReferences", "order_key" in selectItemColumnReferences)
        assertFalse("Expression SELECT alias should not be parsed as a column reference inside SELECT items: $selectItemColumnReferences", "gross_amount" in selectItemColumnReferences)
    }

    fun testOrderByReferencesResolveToSelectAliases() {
        val file = configureStarRocksText(SELECT_ALIAS_SQL)

        assertColumnReferenceResolvesToElementType(file, "total_amount", "total_amount", StarRocksElementTypes.SELECT_ALIAS)
        assertColumnReferenceResolvesToElementType(file, "order_key", "order_key", StarRocksElementTypes.SELECT_ALIAS)
        assertColumnReferenceResolvesToElementType(file, "gross_amount", "gross_amount", StarRocksElementTypes.SELECT_ALIAS)
    }

    fun legacyNamedStubIndexesExposeTableAndColumnNames() {
        val file = configureStarRocksText(MIXED_CASE_CREATE_TABLE_SQL)
        val scope = GlobalSearchScope.fileScope(project, file.virtualFile)

        assertStubIndexContains(StarRocksTableNameIndex.KEY, "dws.sample_orders", "DWS.Sample_Orders", scope)
        assertStubIndexContains(StarRocksTableNameIndex.KEY, "sample_orders", "DWS.Sample_Orders", scope)
        assertStubIndexContains(StarRocksColumnNameIndex.KEY, "order_id", "Order_ID", scope)
    }

    fun legacyTableReferencesResolveToLocalCreateTableStubs() {
        val file = configureStarRocksText(LOCAL_TABLE_REFERENCE_SQL)
        val scope = GlobalSearchScope.fileScope(project, file.virtualFile)

        assertStubIndexContains(StarRocksTableNameIndex.KEY, "dws.sample_orders", "dws.sample_orders", scope)

        assertTableReferenceResolves(file, "dws.sample_orders", "dws.sample_orders")
        assertTableReferenceResolves(file, "sample_orders", "dws.sample_orders")
    }

    fun testTableReferencesResolveToLocalCtes() {
        val file = configureStarRocksText(CTE_REFERENCE_SQL)

        val cteNames = psiElements(file, StarRocksElementTypes.CTE_NAME).map { it.text }

        assertTrue("Expected CTE names in PSI: $cteNames", "base" in cteNames)
        assertTableReferenceResolves(file, "base", "base")
    }

    fun testCteColumnsResolveToQueryOutputs() {
        val file = configureStarRocksText(CTE_OUTPUT_REFERENCE_SQL)

        assertTableReferenceResolves(file, "base", "base")
        assertQualifiedColumnPrefixResolves(file, "b", "b")
        assertQualifiedColumnReferenceResolvesToElementType(file, "b", "order_key", "order_key", StarRocksElementTypes.SELECT_ALIAS)
        assertQualifiedColumnReferenceResolvesToElementType(file, "b", "amount", "amount", StarRocksElementTypes.COLUMN_REFERENCE_NAME)
        assertColumnReferenceResolvesToElementType(file, "order_key", "order_key", StarRocksElementTypes.SELECT_ALIAS)
    }

    fun testCteColumnListOverridesQueryOutputNames() {
        val file = configureStarRocksText(CTE_COLUMN_LIST_REFERENCE_SQL)

        val cteColumnNames = namedStubNames(file, StarRocksElementTypes.CTE_COLUMN_NAME)

        assertContainsElement(file, StarRocksElementTypes.CTE_COLUMN_LIST)
        assertTrue(
            "Expected explicit CTE column names to use named stubs: $cteColumnNames",
            cteColumnNames.containsAll(listOf("order_key", "net_amount"))
        )
        assertQualifiedColumnPrefixResolves(file, "b", "b")
        assertQualifiedColumnReferenceResolvesToElementType(file, "b", "order_key", "order_key", StarRocksElementTypes.CTE_COLUMN_NAME)
        assertQualifiedColumnReferenceResolvesToElementType(file, "b", "net_amount", "net_amount", StarRocksElementTypes.CTE_COLUMN_NAME)
        assertColumnReferenceResolvesToElementType(file, "order_key", "order_key", StarRocksElementTypes.CTE_COLUMN_NAME)
    }

    fun legacyParserMarksTableAliasesAndQualifiedColumnPrefixes() {
        val file = configureStarRocksText(LOCAL_TABLE_REFERENCE_SQL)

        val aliases = psiElements(file, StarRocksElementTypes.TABLE_ALIAS).map { it.text }
        val prefixes = psiElements(file, StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX).map { it.text }
        val columnReferences = psiElements(file, StarRocksElementTypes.COLUMN_REFERENCE_NAME).map { it.text }

        assertTrue("Expected table aliases in PSI: $aliases", aliases.containsAll(listOf("o", "h")))
        assertTrue("Expected qualified column prefixes in PSI: $prefixes", prefixes.containsAll(listOf("o", "h")))
        assertTrue(
            "Expected qualified column reference names in PSI: $columnReferences",
            columnReferences.containsAll(listOf("order_id", "amount"))
        )
    }

    fun legacyQualifiedColumnPrefixesResolveToTableAliases() {
        val file = configureStarRocksText(LOCAL_TABLE_REFERENCE_SQL)

        assertQualifiedColumnPrefixResolves(file, "o", "o")
        assertQualifiedColumnPrefixResolves(file, "h", "h")
    }

    fun legacyQualifiedColumnReferencesResolveToLocalCreateTableColumns() {
        val file = configureStarRocksText(LOCAL_TABLE_REFERENCE_SQL)

        assertColumnReferenceResolves(file, "order_id", "order_id")
        assertColumnReferenceResolves(file, "amount", "amount")
    }

    fun testDerivedTableColumnsResolveToSubqueryOutputs() {
        val file = configureStarRocksText(DERIVED_TABLE_REFERENCE_SQL)

        assertElementCountAtLeast(file, StarRocksElementTypes.SUBQUERY_EXPRESSION, 1)
        assertQualifiedColumnPrefixResolves(file, "d", "d")
        assertQualifiedColumnReferenceResolvesToElementType(file, "d", "order_key", "order_key", StarRocksElementTypes.SELECT_ALIAS)
        assertQualifiedColumnReferenceResolvesToElementType(file, "d", "amount", "amount", StarRocksElementTypes.COLUMN_REFERENCE_NAME)
        assertColumnReferenceResolvesToElementType(file, "total_amount", "total_amount", StarRocksElementTypes.SELECT_ALIAS)
    }

    fun legacyTableFunctionAliasColumnsResolveToAliasColumnList() {
        val file = configureStarRocksText(TABLE_FUNCTION_ALIAS_COLUMN_REFERENCE_SQL)

        val aliasColumnNames = namedStubNames(file, StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME)
        val functionCalls = psiElements(file, StarRocksElementTypes.FUNCTION_CALL).map { it.text }

        assertContainsElement(file, StarRocksElementTypes.TABLE_ALIAS_COLUMN_LIST)
        assertTrue("Expected UNNEST to parse as table function. Actual function calls: $functionCalls", functionCalls.any { it.startsWith("UNNEST") })
        assertTrue(
            "Expected table function output alias columns to use named stubs: $aliasColumnNames",
            "tag" in aliasColumnNames
        )
        assertQualifiedColumnPrefixResolves(file, "u", "u")
        assertQualifiedColumnReferenceResolvesToElementType(file, "u", "tag", "tag", StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME)
        assertColumnReferenceResolvesToElementType(file, "tag", "tag", StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME)
    }

    fun legacyUnqualifiedColumnReferencesResolveToSingleSourceTableColumns() {
        val file = configureStarRocksText(UNQUALIFIED_COLUMN_REFERENCE_SQL)

        val columnReferences = psiElements(file, StarRocksElementTypes.COLUMN_REFERENCE_NAME).map { it.text }

        assertTrue(
            "Expected unqualified column references in PSI: $columnReferences",
            columnReferences.containsAll(listOf("order_id", "amount"))
        )
        assertColumnReferenceResolves(file, "order_id", "order_id")
        assertColumnReferenceResolves(file, "amount", "amount")
    }

    fun testParserBuildsOrderByAndLimitExpressionNodes() {
        val file = configureStarRocksText(ORDER_BY_REFERENCE_SQL)

        val orderingItems = psiElements(file, StarRocksElementTypes.ORDERING_ITEM).map { it.text }
        val limitExpressions = psiElements(file, StarRocksElementTypes.LIMIT_EXPRESSION).map { it.text }

        assertContainsElement(file, StarRocksElementTypes.ORDER_BY_CLAUSE)
        assertTrue("Expected ORDER BY items in PSI: $orderingItems", orderingItems.size >= 2)
        assertTrue("Expected LIMIT expression in PSI: $limitExpressions", limitExpressions.any { it.contains("10") })
        assertColumnReferenceResolves(file, "amount", "amount")
        assertColumnReferenceResolves(file, "order_id", "order_id")
    }

    fun testNamedWindowReferencesResolveToWindowDefinitions() {
        val file = configureStarRocksText(SET_WINDOW_SQL)

        val windowNames = psiElements(file, StarRocksElementTypes.WINDOW_NAME).map { it.text }
        val windowReferences = psiElements(file, StarRocksElementTypes.WINDOW_REFERENCE_NAME).map { it.text }

        assertTrue("Expected named window definitions in PSI: $windowNames", "recent_orders" in windowNames)
        assertTrue("Expected named window references in PSI: $windowReferences", "recent_orders" in windowReferences)
        assertWindowReferenceResolves(file, "recent_orders", "recent_orders")
    }

    fun legacyParserMarksPropertyKeysAndValues() {
        val file = configureStarRocksText(CREATE_TABLE_WITH_PROPERTIES_SQL)

        val propertyKeys = psiElements(file, StarRocksElementTypes.PROPERTY_KEY).map { it.text }
        val propertyValues = psiElements(file, StarRocksElementTypes.PROPERTY_VALUE).map { it.text }

        assertTrue(
            "Expected StarRocks property keys in PSI: $propertyKeys",
            propertyKeys.containsAll(listOf("\"compression\"", "\"enable_persistent_index\""))
        )
        assertTrue(
            "Expected StarRocks property values in PSI: $propertyValues",
            propertyValues.containsAll(listOf("\"LZ4\"", "\"true\""))
        )
    }

    fun testParserBuildsCreateTableClausePsi() {
        val file = configureStarRocksText(
            """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT NOT NULL,
                biz_date DATE NOT NULL,
                amount DECIMAL(18, 2)
            )
            PRIMARY KEY(order_id, biz_date)
            COMMENT 'sample orders'
            PARTITION BY biz_date
            DISTRIBUTED BY HASH(order_id) BUCKETS 8
            PROPERTIES (
                "compression" = "LZ4"
            );
            """.trimIndent()
        )
        val keyColumns = psiElements(file, StarRocksElementTypes.KEY_COLUMN).map { it.text }

        assertContainsElement(file, StarRocksElementTypes.KEY_MODEL_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.PARTITION_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.PARTITION_EXPRESSION)
        assertContainsElement(file, StarRocksElementTypes.DISTRIBUTION_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.DISTRIBUTION_EXPRESSION)
        assertContainsElement(file, StarRocksElementTypes.BUCKETS_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.COMMENT_CLAUSE)
        assertTrue("Expected key model columns in PSI: $keyColumns", keyColumns.containsAll(listOf("order_id", "biz_date")))
    }

    fun testParserBuildsCreateViewPlatformStatementAndResolvesLocalView() {
        val file = configureStarRocksText(CREATE_VIEW_SQL)

        val tableNames = namedStubNames(file, StarRocksElementTypes.TABLE_NAME)
        val columnNames = namedStubNames(file, StarRocksElementTypes.COLUMN_NAME)

        assertContainsElement(file, SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.TABLE_COLUMN_LIST)
        assertContainsElement(file, StarRocksElementTypes.AS_SELECT_CLAUSE)
        assertTrue("Expected CREATE VIEW name to use table-name stubs: $tableNames", "dws.v_sample_order_totals" in tableNames)
        assertTrue("Expected CREATE VIEW columns to use column-name stubs: $columnNames", columnNames.containsAll(listOf("order_id", "total_amount")))
        assertTableReferenceResolves(file, "dws.v_sample_order_totals", "dws.v_sample_order_totals")
        assertColumnReferenceResolves(file, "total_amount", "total_amount")
    }

    fun testSyntaxHighlighterFactoryUsesStarRocksHighlightingLexer() {
        val file = configureStarRocksText(
            "SELECT JSON_LENGTH(payload), BITMAP_COUNT(bitmap_col), CAST(id AS BIGINT), @tenant, @@session.query_timeout, :limit, ${'$'}{biz_date}, ? FROM t;"
        )

        val highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(
            StarRocksDialect.INSTANCE,
            project,
            file.virtualFile
        )
        assertInstanceOf(highlighter, StarRocksSyntaxHighlighter::class.java)
        assertInstanceOf(highlighter.highlightingLexer, StarRocksHighlightingLexer::class.java)
        assertContainsHighlight(highlighter, StarRocksHighlightTokenTypes.FUNCTION, SqlColors.SQL_PROCEDURE)
        assertContainsHighlight(highlighter, StarRocksHighlightTokenTypes.DATA_TYPE, SqlColors.SQL_TYPE)
        assertContainsHighlight(highlighter, StarRocksHighlightTokenTypes.VARIABLE, SqlColors.SQL_VARIABLE)
        assertContainsHighlight(highlighter, StarRocksHighlightTokenTypes.PARAMETER, SqlColors.SQL_PARAMETER)
        val selectToken = SqlTokenRegistry.getType("SELECT")
        assertContainsHighlight(highlighter, selectToken, SqlColors.SQL_KEYWORD)

        val tokens = lexTokenTypes(highlighter, file.text)
        assertTrue("Function calls should use StarRocks function highlight tokens.", StarRocksHighlightTokenTypes.FUNCTION in tokens)
        assertTrue("Data types should use StarRocks data type highlight tokens.", StarRocksHighlightTokenTypes.DATA_TYPE in tokens)
        assertTrue("Variables should use StarRocks variable highlight tokens.", StarRocksHighlightTokenTypes.VARIABLE in tokens)
        assertTrue("Parameters should use StarRocks parameter highlight tokens.", StarRocksHighlightTokenTypes.PARAMETER in tokens)
        assertTrue("Keywords should use registered platform SQL tokens.", selectToken in tokens)
    }

    fun testPlatformSqlBlockHighlighterLoadsAfterStarRocksTokenInitialization() {
        configureStarRocksText("SELECT IF(flag, 1, 0) FROM dws.sample_orders;")

        assertTrue(
            "Platform SQL block highlighter should accept common IF keyword after StarRocks token initialization.",
            SqlCodeBlockProviderUtils.STARTERS.contains(SqlTokenRegistry.getType("IF"))
        )
    }

    fun testSyntaxHighlighterHighlightsUserDefinedFunctionCalls() {
        val file = configureStarRocksText(
            "SELECT my_udf(payload), analytics.custom_score(id) FROM dws.sample_orders;"
        )

        val highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(
            StarRocksDialect.INSTANCE,
            project,
            file.virtualFile
        )
        val functionTexts = lexTokenTexts(highlighter, file.text, StarRocksHighlightTokenTypes.FUNCTION)

        assertTrue("User-defined function call names should use function highlighting. Actual function tokens: $functionTexts", "my_udf" in functionTexts)
        assertTrue("Qualified user-defined function names should highlight the call segment. Actual function tokens: $functionTexts", "custom_score" in functionTexts)
    }

    fun testSyntaxHighlighterHighlightsFunctionLikeKeywords() {
        val file = configureStarRocksText(
            "SELECT EXTRACT(DAY FROM ts), GROUPING(k), GROUPING_ID(k), PERCENTILE(v, 0.95), SUM(v) OVER (PARTITION BY k) FROM t;"
        )

        val highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(
            StarRocksDialect.INSTANCE,
            project,
            file.virtualFile
        )
        val functionTexts = lexTokenTexts(highlighter, file.text, StarRocksHighlightTokenTypes.FUNCTION)

        assertTrue(
            "Function-like keywords should use function highlighting. Actual function tokens: $functionTexts",
            functionTexts.containsAll(listOf("EXTRACT", "GROUPING", "GROUPING_ID", "PERCENTILE", "SUM"))
        )
        assertFalse("Window clause keyword OVER should stay a keyword, not a function. Actual function tokens: $functionTexts", "OVER" in functionTexts)
    }

    fun testParserBuildsStarRocksComplexTypePsi() {
        val file = configureStarRocksText(
            """
            CREATE TABLE IF NOT EXISTS dws.complex_type_sample (
                id BIGINT NOT NULL,
                tags ARRAY<VARCHAR(32)>,
                attributes MAP<STRING, JSON>,
                profile STRUCT<name VARCHAR(64), score BIGINT>,
                PRIMARY KEY(id)
            );
            """.trimIndent()
        )
        val tableNames = namedStubNames(file, StarRocksElementTypes.TABLE_NAME)
        val columnNames = namedStubNames(file, StarRocksElementTypes.COLUMN_NAME)

        assertContainsElement(file, SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.TABLE_COLUMN_LIST)
        assertContainsElement(file, StarRocksElementTypes.COLUMN_DEFINITION)
        assertContainsElement(file, SqlCompositeElementTypes.SQL_TYPE_ELEMENT)
        assertTrue("Generated CREATE TABLE should expose table-name stub PSI. Actual: $tableNames", "dws.complex_type_sample" in tableNames)
        assertTrue(
            "Generated CREATE TABLE should expose column-name stub PSI without treating PRIMARY KEY columns as definitions. Actual: $columnNames",
            columnNames.containsAll(listOf("id", "tags", "attributes", "profile")) && columnNames.count { it == "id" } == 1
        )
    }

    fun testGeneratedCreateTableNamedStubsAreIndexed() {
        val file = configureStarRocksText(
            """
            CREATE TABLE IF NOT EXISTS DWS.Sample_Orders (
                Order_ID BIGINT NOT NULL,
                Amount DECIMAL(18, 2)
            );
            """.trimIndent()
        )
        val scope = GlobalSearchScope.fileScope(project, file.virtualFile)

        assertStubIndexContains(StarRocksTableNameIndex.KEY, "dws.sample_orders", "DWS.Sample_Orders", scope)
        assertStubIndexContains(StarRocksTableNameIndex.KEY, "sample_orders", "DWS.Sample_Orders", scope)
        assertStubIndexContains(StarRocksColumnNameIndex.KEY, "order_id", "Order_ID", scope)
    }

    fun testCompletionSuggestsGeneratedCreateTableNamesInFromClause() {
        configureStarRocksText(
            """
            CREATE TABLE IF NOT EXISTS dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            SELECT order_id
            FROM <caret>;
            """.trimIndent()
        )

        val lookupStrings = completionLookupStrings()

        assertTrue(
            "FROM table completion should suggest generated CREATE TABLE names. Actual lookups: $lookupStrings",
            "dws.sample_orders" in lookupStrings && "sample_orders" in lookupStrings
        )
    }

    fun testGeneratedGrammarBuildsPlatformMaterializedViewStatementPsi() {
        val file = configureStarRocksText(
            """
            CREATE MATERIALIZED VIEW dws.mv_sample_orders
            PARTITION BY date_trunc('day', order_date)
            DISTRIBUTED BY HASH(order_id) BUCKETS 8
            REFRESH ASYNC
            PROPERTIES (
                "replication_num" = "1"
            )
            AS SELECT order_id, order_date, sum(amount) AS total_amount
            FROM dws.sample_orders
            GROUP BY order_id, order_date;
            """.trimIndent()
        )

        assertContainsElement(file, SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT)
        assertContainsElement(file, StarRocksElementTypes.PARTITION_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.DISTRIBUTION_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.BUCKETS_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.REFRESH_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.PROPERTIES_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.AS_SELECT_CLAUSE)
    }

    fun testGeneratedGrammarBuildsStarRocksQualifyAndUnnestPsi() {
        val file = configureStarRocksText(
            """
            SELECT o.order_id, tag
            FROM dws.sample_orders o
            JOIN UNNEST(o.tags) AS u(tag)
            QUALIFY row_number() OVER (PARTITION BY o.order_id ORDER BY o.updated_at DESC) = 1;
            """.trimIndent()
        )

        val statements = PsiTreeUtil.findChildrenOfType(file, SqlStatement::class.java).toList()
        val functionCalls = psiElements(file, SqlCompositeElementTypes.SQL_FUNCTION_CALL).map { it.text }
        val aliasNames = namedStubNames(file, StarRocksElementTypes.TABLE_ALIAS)
        val aliasColumnNames = namedStubNames(file, StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME)

        assertTrue("Expected StarRocks query to stay a runnable SqlStatement.", statements.isNotEmpty())
        assertContainsElement(file, SqlCompositeElementTypes.SQL_SELECT_STATEMENT)
        assertContainsElement(file, SqlCompositeElementTypes.SQL_QUALIFY_CLAUSE)
        assertContainsElement(file, StarRocksElementTypes.QUALIFY_EXPRESSION)
        assertTrue("Expected UNNEST to parse as a table function. Actual function calls: $functionCalls", functionCalls.any { it.startsWith("UNNEST") })
        assertTrue("Expected UNNEST alias to use named stub PSI. Actual aliases: $aliasNames", "u" in aliasNames)
        assertTrue("Expected UNNEST output column to use named stub PSI. Actual alias columns: $aliasColumnNames", "tag" in aliasColumnNames)
    }

    fun testGeneratedGrammarBuildsStarRocksInsertWithUnnestPsi() {
        val file = configureStarRocksText(
            """
            INSERT INTO dws.sample_order_tags
            SELECT o.order_id, tag
            FROM dws.sample_orders o
            JOIN UNNEST(o.tags) AS u(tag);
            """.trimIndent()
        )

        val functionCalls = psiElements(file, SqlCompositeElementTypes.SQL_FUNCTION_CALL).map { it.text }
        val aliasColumnNames = namedStubNames(file, StarRocksElementTypes.TABLE_ALIAS_COLUMN_NAME)

        assertContainsElement(file, SqlCompositeElementTypes.SQL_INSERT_STATEMENT)
        assertTrue("Expected UNNEST to parse inside INSERT query. Actual function calls: $functionCalls", functionCalls.any { it.startsWith("UNNEST") })
        assertTrue("Expected UNNEST output column to use named stub PSI. Actual alias columns: $aliasColumnNames", "tag" in aliasColumnNames)
    }

    fun legacySemanticAnnotatorHighlightsTablesColumnsAndAliases() {
        val file = configureStarRocksText(LOCAL_TABLE_REFERENCE_SQL)

        val highlights = myFixture.doHighlighting()

        assertSemanticHighlight(highlights, file, "dws.sample_orders", SqlColors.SQL_TABLE)
        assertSemanticHighlight(highlights, file, "order_id", SqlColors.SQL_COLUMN)
        assertSemanticHighlight(highlights, file, "amount", SqlColors.SQL_COLUMN)
        assertSemanticHighlight(highlights, file, "o", SqlColors.SQL_LOCAL_ALIAS)
        assertSemanticHighlight(highlights, file, "h", SqlColors.SQL_LOCAL_ALIAS)
    }

    fun legacySemanticAnnotatorHighlightsOrderByColumnReferences() {
        val file = configureStarRocksText(ORDER_BY_REFERENCE_SQL)

        val highlights = myFixture.doHighlighting()

        assertSemanticHighlight(highlights, file, "amount", SqlColors.SQL_COLUMN)
        assertSemanticHighlight(highlights, file, "order_id", SqlColors.SQL_COLUMN)
    }

    fun legacySemanticAnnotatorHighlightsNamedWindows() {
        val file = configureStarRocksText(SET_WINDOW_SQL)

        val highlights = myFixture.doHighlighting()

        assertSemanticHighlight(highlights, file, "recent_orders", SqlColors.SQL_LOCAL_ALIAS)
    }

    fun legacySemanticAnnotatorHighlightsSelectAliases() {
        val file = configureStarRocksText(SELECT_ALIAS_SQL)

        val highlights = myFixture.doHighlighting()

        assertSemanticHighlight(highlights, file, "total_amount", SqlColors.SQL_LOCAL_ALIAS)
        assertSemanticHighlight(highlights, file, "order_key", SqlColors.SQL_LOCAL_ALIAS)
        assertSemanticHighlight(highlights, file, "gross_amount", SqlColors.SQL_LOCAL_ALIAS)
    }

    fun testSemanticAnnotatorHighlightsSelectAliasDefinitionsWithoutResolvingOrderByReferences() {
        val file = configureStarRocksText(SELECT_ALIAS_SQL)

        val highlights = myFixture.doHighlighting()

        assertNoErrorHighlights(highlights)
    }

    fun legacySemanticAnnotatorHighlightsPropertyKeys() {
        val file = configureStarRocksText(CREATE_TABLE_WITH_PROPERTIES_SQL)

        val highlights = myFixture.doHighlighting()

        assertSemanticHighlight(highlights, file, "\"compression\"", SqlColors.SQL_PARAMETER)
        assertSemanticHighlight(highlights, file, "\"enable_persistent_index\"", SqlColors.SQL_PARAMETER)
    }

    fun legacyCompletionContributorExposesOfficialBuiltinFunctions() {
        configureStarRocksText("SELECT JSON_<caret> FROM t;")

        val lookupElements = myFixture.completeBasic().orEmpty()
        val lookupStrings = lookupElements.flatMap { it.allLookupStrings }
        val documentText = myFixture.editor.document.text

        assertTrue("Scenario sanity check: completion catalog should contain JSON_LENGTH.", "JSON_LENGTH" in StarRocksCompletionCatalog.FUNCTIONS)
        assertTrue(
            "Basic completion should expose JSON_LENGTH from the official builtin function set. Actual lookups: $lookupStrings. Document: $documentText",
            "JSON_LENGTH" in lookupStrings || "json_length" in lookupStrings
        )
    }

    fun legacyCompletionContributorSuggestsLocalTablesInFromClause() {
        configureStarRocksText(TABLE_COMPLETION_SQL)

        val lookupStrings = completionLookupStrings()

        assertTrue(
            "FROM table completion should suggest local table names. Actual lookups: $lookupStrings",
            "dws.sample_orders" in lookupStrings && "sample_orders" in lookupStrings
        )
    }

    fun legacyCompletionContributorSuggestsVisibleColumnsInSelectClause() {
        configureStarRocksText(COLUMN_COMPLETION_SQL)

        val lookupStrings = completionLookupStrings()

        assertTrue(
            "SELECT column completion should suggest columns from the visible local table. Actual lookups: $lookupStrings",
            lookupStrings.containsAll(listOf("order_id", "amount"))
        )
    }

    fun testCompletionContributorSuggestsSelectAliasesInOrderByClause() {
        configureStarRocksText(ORDER_BY_ALIAS_COMPLETION_SQL)

        val lookupStrings = completionLookupStrings()

        assertTrue(
            "ORDER BY completion should suggest SELECT aliases. Actual lookups: $lookupStrings",
            lookupStrings.containsAll(listOf("total_amount", "order_key"))
        )
    }

    fun legacyCompletionContributorSuggestsDerivedTableColumns() {
        configureStarRocksText(DERIVED_TABLE_COLUMN_COMPLETION_SQL)

        val lookupStrings = completionLookupStrings()

        assertTrue(
            "Column completion should suggest derived table output columns. Actual lookups: $lookupStrings",
            lookupStrings.containsAll(listOf("order_key", "amount"))
        )
    }

    fun legacyCompletionContributorSuggestsCteOutputColumns() {
        configureStarRocksText(CTE_COLUMN_COMPLETION_SQL)

        val lookupStrings = completionLookupStrings()

        assertTrue(
            "Column completion should suggest CTE query output columns. Actual lookups: $lookupStrings",
            lookupStrings.containsAll(listOf("order_key", "amount"))
        )
    }

    fun legacyCompletionContributorSuggestsExplicitCteColumns() {
        configureStarRocksText(CTE_COLUMN_LIST_COMPLETION_SQL)

        val lookupStrings = completionLookupStrings()

        assertTrue(
            "Column completion should suggest explicit CTE column names. Actual lookups: $lookupStrings",
            lookupStrings.containsAll(listOf("order_key", "net_amount"))
        )
    }

    fun legacyCompletionContributorSuggestsTableFunctionAliasColumns() {
        configureStarRocksText(TABLE_FUNCTION_ALIAS_COLUMN_COMPLETION_SQL)

        val lookupStrings = completionLookupStrings()

        assertTrue(
            "Column completion should suggest UNNEST table function output alias columns. Actual lookups: $lookupStrings",
            "tag" in lookupStrings
        )
    }

    fun legacyCompletionContributorSuggestsPropertiesOnlyInPropertiesClause() {
        configureStarRocksText(PROPERTY_COMPLETION_SQL)

        val lookupStrings = completionLookupStrings()

        assertTrue(
            "PROPERTIES completion should suggest StarRocks property keys. Actual lookups: $lookupStrings",
            lookupStrings.containsAll(listOf("compression", "enable_persistent_index"))
        )
    }

    fun testCompletionDoesNotSuggestPropertiesOutsidePropertiesClause() {
        configureStarRocksText("SELECT <caret> FROM dws.sample_orders;")

        val lookupStrings = completionLookupStrings()

        assertFalse(
            "StarRocks property keys should stay scoped to PROPERTIES PSI. Actual lookups: $lookupStrings",
            "compression" in lookupStrings || "enable_persistent_index" in lookupStrings
        )
    }

    fun testFormatterBridgeRunsForOpenedStarRocksFile() {
        val file = configureStarRocksText(SET_WINDOW_SQL)

        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(file)
        }

        val formatted = myFixture.editor.document.text
        assertTrue("Formatter should preserve UNION ALL in compound queries.", formatted.contains("UNION ALL", ignoreCase = true))
        assertTrue("Formatter should preserve named WINDOW clauses.", formatted.contains("WINDOW recent_orders AS", ignoreCase = true))
    }

    fun testFormatterChangesCompactQueryLayout() {
        configureStarRocksText(COMPACT_QUERY_FORMATTER_SQL)
        val original = myFixture.editor.document.text

        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        val formatted = myFixture.editor.document.text

        assertTrue("Formatter should actively change compact StarRocks query layout. Actual text: $formatted", original != formatted)
        assertTrue("Formatted query should keep SELECT clause.", formatted.contains("SELECT", ignoreCase = true))
        assertTrue("Formatted query should keep FROM clause.", formatted.contains("FROM", ignoreCase = true))
        assertTrue("Formatted query should keep WHERE clause.", formatted.contains("WHERE", ignoreCase = true))
    }

    fun testFormatterKeepsStarRocksDdlStable() {
        val file = configureStarRocksText(STARROCKS_FORMATTER_DDL_SQL)
        val original = myFixture.editor.document.text

        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(file)
        }
        val firstFormatted = myFixture.editor.document.text

        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(file)
        }
        val secondFormatted = myFixture.editor.document.text

        assertEquals("StarRocks DDL formatter must not rewrite unsupported DDL layout.", original, firstFormatted)
        assertEquals("Repeated StarRocks DDL formatting must be idempotent.", firstFormatted, secondFormatted)
    }

    private fun configureStarRocksText(sql: String): PsiFile {
        return myFixture.configureByText(StarRocksTestFileType, sql)
    }

    private fun assertContainsElement(file: PsiFile, elementType: IElementType) {
        assertTrue(
            "Expected ${elementType.debugName} in StarRocks PSI tree.",
            containsElementType(file.node, elementType)
        )
    }

    private fun assertNoErrorHighlights(highlights: List<HighlightInfo>) {
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue(
            "Expected no error highlights. Actual: ${errors.map { "${it.startOffset}-${it.endOffset}: ${it.description}" }}",
            errors.isEmpty()
        )
    }

    private fun assertElementCountAtLeast(file: PsiFile, elementType: IElementType, expectedCount: Int) {
        val actualCount = countElementType(file.node, elementType)
        assertTrue(
            "Expected at least $expectedCount ${elementType.debugName} nodes in StarRocks PSI tree, found $actualCount.",
            actualCount >= expectedCount
        )
    }

    private fun containsElementType(node: ASTNode?, elementType: IElementType): Boolean {
        if (node == null) {
            return false
        }
        if (node.elementType == elementType) {
            return true
        }
        return node.getChildren(null).any { containsElementType(it, elementType) }
    }

    private fun countElementType(node: ASTNode?, elementType: IElementType): Int {
        if (node == null) {
            return 0
        }
        val self = if (node.elementType == elementType) 1 else 0
        return self + node.getChildren(null).sumOf { countElementType(it, elementType) }
    }

    private fun elementTypeNames(file: PsiFile): List<String> {
        val result = linkedSetOf<String>()
        collectElementTypeNames(file.node, result)
        return result.take(40)
    }

    private fun collectElementTypeNames(node: ASTNode?, result: MutableSet<String>) {
        if (node == null || result.size >= 40) {
            return
        }
        result += node.elementType.debugName
        node.getChildren(null).forEach { collectElementTypeNames(it, result) }
    }

    private fun psiElements(file: PsiFile, elementType: IElementType): List<PsiElement> {
        val result = mutableListOf<PsiElement>()
        collectPsiElements(file.node, elementType, result)
        return result
    }

    private fun namedStubNames(file: PsiFile, elementType: IElementType): List<String> {
        return psiElements(file, elementType)
            .filterIsInstance<StarRocksNamedStubElement>()
            .map { it.name }
    }

    private fun collectPsiElements(node: ASTNode?, elementType: IElementType, result: MutableList<PsiElement>) {
        if (node == null) {
            return
        }
        if (node.elementType == elementType) {
            result += node.psi
        }
        node.getChildren(null).forEach { collectPsiElements(it, elementType, result) }
    }

    private fun hasAncestor(element: PsiElement, elementType: IElementType): Boolean {
        var current = element.parent
        while (current != null) {
            if (current.node?.elementType == elementType) {
                return true
            }
            current = current.parent
        }
        return false
    }

    private fun assertStubIndexContains(
        indexKey: StubIndexKey<String, StarRocksNamedStubElement>,
        lookupName: String,
        expectedPsiName: String,
        scope: GlobalSearchScope
    ) {
        val names = StubIndex.getElements(indexKey, lookupName, project, scope, StarRocksNamedStubElement::class.java)
            .map { it.name }
        assertTrue(
            "Expected $lookupName in ${indexKey.name} to resolve $expectedPsiName. Actual names: $names",
            expectedPsiName in names
        )
    }

    private fun assertTableReferenceResolves(
        file: PsiFile,
        referenceText: String,
        expectedTableName: String
    ) {
        val resolvedNames = psiElements(file, StarRocksElementTypes.TABLE_REFERENCE_NAME)
            .filter { it.text == referenceText }
            .mapNotNull { referenceName -> referenceName.references.firstOrNull()?.resolve() }
            .map { target ->
                when (target) {
                    is StarRocksNamedStubElement -> target.name
                    else -> target.text
                }
            }
        val referenceClasses = psiElements(file, StarRocksElementTypes.TABLE_REFERENCE_NAME)
            .filter { it.text == referenceText }
            .flatMap { referenceName -> referenceName.references.map { it::class.java.name } }
        assertTrue(
            "Expected table reference $referenceText to resolve to $expectedTableName. Actual targets: $resolvedNames. References: $referenceClasses",
            expectedTableName in resolvedNames
        )
    }

    private fun assertQualifiedColumnPrefixResolves(
        file: PsiFile,
        referenceText: String,
        expectedAlias: String
    ) {
        val resolvedNames = psiElements(file, StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX)
            .filter { it.text == referenceText }
            .mapNotNull { referenceName -> referenceName.references.firstOrNull()?.resolve() }
            .map { it.text }
        val referenceClasses = psiElements(file, StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX)
            .filter { it.text == referenceText }
            .flatMap { referenceName -> referenceName.references.map { it::class.java.name } }
        assertTrue(
            "Expected qualified column prefix $referenceText to resolve to alias $expectedAlias. Actual targets: $resolvedNames. References: $referenceClasses",
            expectedAlias in resolvedNames
        )
    }

    private fun assertColumnReferenceResolves(
        file: PsiFile,
        referenceText: String,
        expectedColumnName: String
    ) {
        val resolvedNames = psiElements(file, StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            .filter { it.text == referenceText }
            .mapNotNull { referenceName -> referenceName.references.firstOrNull()?.resolve() }
            .map { target ->
                when (target) {
                    is StarRocksNamedStubElement -> target.name
                    else -> target.text
                }
            }
        val referenceClasses = psiElements(file, StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            .filter { it.text == referenceText }
            .flatMap { referenceName -> referenceName.references.map { it::class.java.name } }
        assertTrue(
            "Expected column reference $referenceText to resolve to $expectedColumnName. Actual targets: $resolvedNames. References: $referenceClasses",
            expectedColumnName in resolvedNames
        )
    }

    private fun assertColumnReferenceResolvesToElementType(
        file: PsiFile,
        referenceText: String,
        expectedName: String,
        expectedElementType: IElementType
    ) {
        val resolvedTargets = psiElements(file, StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            .filter { it.text == referenceText }
            .mapNotNull { referenceName -> referenceName.references.firstOrNull()?.resolve() }
        val targetDescriptions = resolvedTargets.map { target ->
            val name = when (target) {
                is StarRocksNamedStubElement -> target.name
                else -> target.text
            }
            "${target.node?.elementType?.debugName}:$name"
        }
        assertTrue(
            "Expected column reference $referenceText to resolve to $expectedElementType/$expectedName. Actual targets: $targetDescriptions",
            resolvedTargets.any { target ->
                val name = when (target) {
                    is StarRocksNamedStubElement -> target.name
                    else -> target.text
                }
                target.node?.elementType == expectedElementType && name == expectedName
            }
        )
    }

    private fun assertQualifiedColumnReferenceResolvesToElementType(
        file: PsiFile,
        qualifierText: String,
        referenceText: String,
        expectedName: String,
        expectedElementType: IElementType
    ) {
        val resolvedTargets = psiElements(file, StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            .filter { it.text == referenceText && previousQualifiedPrefixText(it) == qualifierText }
            .mapNotNull { referenceName -> referenceName.references.firstOrNull()?.resolve() }
        val targetDescriptions = resolvedTargets.map { target ->
            val name = when (target) {
                is StarRocksNamedStubElement -> target.name
                else -> target.text
            }
            "${target.node?.elementType?.debugName}:$name"
        }
        assertTrue(
            "Expected qualified column reference $qualifierText.$referenceText to resolve to $expectedElementType/$expectedName. Actual targets: $targetDescriptions",
            resolvedTargets.any { target ->
                val name = when (target) {
                    is StarRocksNamedStubElement -> target.name
                    else -> target.text
                }
                target.node?.elementType == expectedElementType && name == expectedName
            }
        )
    }

    private fun previousQualifiedPrefixText(element: PsiElement): String? {
        var current = element.prevSibling
        while (current != null) {
            val type = current.node?.elementType
            if (type == StarRocksElementTypes.QUALIFIED_COLUMN_PREFIX) {
                return current.text
            }
            if (!current.text.isBlank() && current.text != ".") {
                return null
            }
            current = current.prevSibling
        }
        return null
    }

    private fun assertWindowReferenceResolves(
        file: PsiFile,
        referenceText: String,
        expectedWindowName: String
    ) {
        val resolvedNames = psiElements(file, StarRocksElementTypes.WINDOW_REFERENCE_NAME)
            .filter { it.text == referenceText }
            .mapNotNull { referenceName -> referenceName.references.firstOrNull()?.resolve() }
            .map { it.text }
        val referenceClasses = psiElements(file, StarRocksElementTypes.WINDOW_REFERENCE_NAME)
            .filter { it.text == referenceText }
            .flatMap { referenceName -> referenceName.references.map { it::class.java.name } }
        assertTrue(
            "Expected window reference $referenceText to resolve to $expectedWindowName. Actual targets: $resolvedNames. References: $referenceClasses",
            expectedWindowName in resolvedNames
        )
    }

    private fun assertContainsHighlight(
        highlighter: SyntaxHighlighter,
        tokenType: IElementType,
        expected: TextAttributesKey
    ) {
        assertTrue(
            "Expected $expected highlight for ${tokenType.debugName}.",
            expected in highlighter.getTokenHighlights(tokenType)
        )
    }

    private fun assertSemanticHighlight(
        highlights: List<HighlightInfo>,
        file: PsiFile,
        expectedText: String,
        expected: TextAttributesKey
    ) {
        val matchingHighlights = highlights
            .filter { it.forcedTextAttributesKey == expected }
            .map { file.text.substring(it.startOffset, it.endOffset) }
        assertTrue(
            "Expected semantic highlight $expected for `$expectedText`. Actual highlighted texts: $matchingHighlights",
            expectedText in matchingHighlights
        )
    }

    private fun assertSemanticHighlightCountAtLeast(
        highlights: List<HighlightInfo>,
        file: PsiFile,
        expectedText: String,
        expected: TextAttributesKey,
        expectedCount: Int
    ) {
        val matchingHighlights = highlights
            .filter { it.forcedTextAttributesKey == expected }
            .map { file.text.substring(it.startOffset, it.endOffset) }
        val actualCount = matchingHighlights.count { it == expectedText }
        assertTrue(
            "Expected at least $expectedCount semantic highlights $expected for `$expectedText`. Actual highlighted texts: $matchingHighlights",
            actualCount >= expectedCount
        )
    }

    private fun completionLookupStrings(): Set<String> {
        return myFixture.completeBasic()
            .orEmpty()
            .flatMap { it.allLookupStrings }
            .toSet()
    }

    private fun lexTokenTypes(highlighter: SyntaxHighlighter, sql: String): Set<IElementType> {
        val lexer = highlighter.highlightingLexer
        val result = mutableSetOf<IElementType>()
        lexer.start(sql)
        while (lexer.tokenType != null) {
            lexer.tokenType?.let(result::add)
            lexer.advance()
        }
        return result
    }

    private fun lexTokenTexts(highlighter: SyntaxHighlighter, sql: String, tokenType: IElementType): List<String> {
        val lexer = highlighter.highlightingLexer
        val result = mutableListOf<String>()
        lexer.start(sql)
        while (lexer.tokenType != null) {
            if (lexer.tokenType == tokenType) {
                result += sql.substring(lexer.tokenStart, lexer.tokenEnd)
            }
            lexer.advance()
        }
        return result
    }

    private object StarRocksTestFileType : LanguageFileType(StarRocksDialect.INSTANCE) {
        override fun getName(): String = "StarRocks Test SQL"
        override fun getDescription(): String = "StarRocks SQL test file"
        override fun getDefaultExtension(): String = "starrocks"
        override fun getIcon(): Icon = StarRocksIcons.Dialect
    }

    private companion object {
        private val SET_WINDOW_SQL = """
            WITH base AS (
                SELECT store_id, order_id, event_time
                FROM dws.current_orders
            )
            SELECT
                store_id,
                order_id,
                row_number() OVER recent_orders AS rn
            FROM base
            WINDOW recent_orders AS (PARTITION BY store_id ORDER BY event_time DESC)
            UNION ALL
            SELECT
                store_id,
                order_id,
                row_number() OVER recent_orders AS rn
            FROM dws.history_orders
            WINDOW recent_orders AS (PARTITION BY store_id ORDER BY event_time DESC);
        """.trimIndent()

        private val NESTED_QUERY_SQL = """
            WITH active_users AS (
                SELECT user_id
                FROM dim_users
                WHERE status = 'ACTIVE'
            )
            SELECT
                o.order_id,
                (
                    SELECT max(event_time)
                    FROM order_events e
                    WHERE e.order_id = o.order_id
                ) AS latest_event_time
            FROM fact_orders o
            WHERE EXISTS (
                SELECT 1
                FROM active_users u
                WHERE u.user_id = o.user_id
            )
              AND o.store_id IN (
                SELECT store_id
                FROM dim_store
                WHERE region = 'north'
            );
        """.trimIndent()

        private val PROBLEMATIC_INSERT_WITH_CTES_SQL = """
            SET @biz_time = '${'$'}[yyyy-MM-dd HH:mm:ss-2/24]';
            SET @biz_date = date_trunc('day', @biz_time);

            INSERT INTO dws.dws_trade_sale_by_store_item_hour_ri
            WITH sale_data AS (
                SELECT
                    biz_date,
                    sale.store_id,
                    item_id,
                    sale_time,
                    sale_amt,
                    item_qty,
                    order_id
                FROM dwm.dwm_trade_sale_ri_v2 sale
                INNER JOIN dwd.dim_store_ra store ON store.store_id = sale.store_id
                WHERE sale.biz_date = @biz_date
            ),
            item_hour AS (
                SELECT
                    s.biz_date,
                    s.store_id,
                    s.item_id,
                    h.biz_hour
                FROM (
                    SELECT DISTINCT biz_date, store_id, item_id
                    FROM sale_data
                ) s
                CROSS JOIN (
                    SELECT CAST(unnest AS INT) AS biz_hour
                    FROM (
                        SELECT [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23] AS date_list
                    ) a, UNNEST(date_list)
                ) h
            ),
            sale_hour AS (
                SELECT
                    biz_date,
                    hour(sale_time) AS biz_hour,
                    store_id,
                    item_id,
                    SUM(sale_amt) AS sale_amt,
                    SUM(item_qty) AS item_qty,
                    COUNT(DISTINCT order_id) AS order_qty
                FROM sale_data
                GROUP BY biz_date, biz_hour, store_id, item_id
            ),
            sale_hour_acc AS (
                SELECT
                    item_hour.biz_date AS biz_date,
                    item_hour.biz_hour AS biz_hour,
                    item_hour.store_id AS store_id,
                    item_hour.item_id AS item_id,
                    SUM(COALESCE(sale_hour.sale_amt, 0)) OVER (
                        PARTITION BY item_hour.store_id, item_hour.item_id, item_hour.biz_date
                        ORDER BY item_hour.biz_hour
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS sale_amt_acc,
                    NOW() AS etl_time
                FROM item_hour
                LEFT JOIN sale_hour
                    ON sale_hour.biz_date = item_hour.biz_date
                    AND sale_hour.biz_hour = item_hour.biz_hour
                    AND sale_hour.store_id = item_hour.store_id
                    AND sale_hour.item_id = item_hour.item_id
            )
            SELECT sale_hour_acc.*
            FROM sale_hour_acc
            WHERE (sale_hour_acc.biz_date + INTERVAL sale_hour_acc.biz_hour HOUR) < NOW();
        """.trimIndent()

        private val REALISTIC_INSERT_WITH_CTES_AND_WINDOWS_SQL = """
            -- name dws_trade_sale_by_store_item_hour_ri
            -- type JZSQL
            -- author scheduler
            -- create time 2025-8-21 10:31:26
            -- desc

            set @biz_time = '${'$'}[yyyy-MM-dd HH:mm:ss-2/24]';
            SET @biz_date = date_trunc('day', @biz_time);

            INSERT INTO dws.dws_trade_sale_by_store_item_hour_ri
            -- INSERT OVERWRITE dws.dws_trade_sale_by_store_item_hour_ri PARTITION (biz_date = '${'$'}[yyyy-MM-dd]')
            WITH sale_data AS (
                SELECT
                    biz_date,
                    sale.store_id,
                    item_id,
                    sale_time,
                    sale_amt,
                    item_qty,
                    gp_amt AS gross_margin_amt,
                    dy_amt,
                    order_id,
                    member_card_no
                FROM dwm.dwm_trade_sale_ri_v2 sale
                INNER JOIN dwd.dim_store_ra store
                    ON store.store_id = sale.store_id
                WHERE (store.biz_type IN ('cvs', 'fusion') OR store.fsg_cvs_pkg IS NOT NULL)
                  AND sale.biz_date = @biz_date
            ),
            item_hour AS (
                SELECT
                    s.biz_date,
                    s.store_id,
                    s.item_id,
                    h.biz_hour
                FROM (
                    SELECT DISTINCT biz_date, store_id, item_id
                    FROM sale_data
                ) s
                CROSS JOIN (
                    SELECT CAST(unnest AS INT) AS biz_hour
                    FROM (
                        SELECT [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23] AS date_list
                    ) a, UNNEST(date_list)
                ) h
            ),
            sale_hour AS (
                SELECT
                    biz_date,
                    hour(sale_time) AS biz_hour,
                    store_id,
                    item_id,
                    SUM(sale_amt) AS sale_amt,
                    SUM(item_qty) AS item_qty,
                    SUM(gross_margin_amt) AS gross_margin_amt,
                    SUM(sale_amt - gross_margin_amt) AS item_cost,
                    SUM(dy_amt) AS dy_amt,
                    COUNT(DISTINCT order_id) AS order_qty,
                    COUNT(DISTINCT member_card_no) AS member_qty
                FROM sale_data
                GROUP BY biz_date, biz_hour, store_id, item_id
            ),
            sale_min_hour AS (
                SELECT biz_date, store_id, item_id, MIN(biz_hour) AS min_biz_hour
                FROM sale_hour
                GROUP BY biz_date, store_id, item_id
            ),
            sale_hour_acc AS (
                SELECT
                    item_hour.biz_date AS biz_date,
                    item_hour.biz_hour AS biz_hour,
                    item_hour.store_id AS store_id,
                    item_hour.item_id AS item_id,
                    COALESCE(sale_amt, 0) AS sale_amt,
                    COALESCE(item_qty, 0) AS item_qty,
                    COALESCE(gross_margin_amt, 0) AS gross_margin_amt,
                    COALESCE(item_cost, 0) AS item_cost,
                    COALESCE(dy_amt, 0) AS dy_amt,
                    COALESCE(order_qty, 0) AS order_qty,
                    COALESCE(member_qty, 0) AS member_qty,
                    SUM(COALESCE(sale_hour.sale_amt, 0)) OVER (
                        PARTITION BY item_hour.store_id, item_hour.item_id, item_hour.biz_date
                        ORDER BY item_hour.biz_hour
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS sale_amt_acc,
                    SUM(COALESCE(sale_hour.item_qty, 0)) OVER (
                        PARTITION BY item_hour.store_id, item_hour.item_id, item_hour.biz_date
                        ORDER BY item_hour.biz_hour
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS item_qty_acc,
                    SUM(COALESCE(sale_hour.gross_margin_amt, 0)) OVER (
                        PARTITION BY item_hour.store_id, item_hour.item_id, item_hour.biz_date
                        ORDER BY item_hour.biz_hour
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS gross_margin_amt_acc,
                    SUM(COALESCE(sale_hour.item_cost, 0)) OVER (
                        PARTITION BY item_hour.store_id, item_hour.item_id, item_hour.biz_date
                        ORDER BY item_hour.biz_hour
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS item_cost_acc,
                    SUM(COALESCE(sale_hour.dy_amt, 0)) OVER (
                        PARTITION BY item_hour.store_id, item_hour.item_id, item_hour.biz_date
                        ORDER BY item_hour.biz_hour
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS dy_amt_acc,
                    SUM(COALESCE(sale_hour.order_qty, 0)) OVER (
                        PARTITION BY item_hour.store_id, item_hour.item_id, item_hour.biz_date
                        ORDER BY item_hour.biz_hour
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS order_qty_acc,
                    SUM(COALESCE(sale_hour.member_qty, 0)) OVER (
                        PARTITION BY item_hour.store_id, item_hour.item_id, item_hour.biz_date
                        ORDER BY item_hour.biz_hour
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS member_qty_acc,
                    NOW() AS etl_time
                FROM item_hour
                LEFT JOIN sale_hour
                    ON sale_hour.biz_date = item_hour.biz_date
                    AND sale_hour.biz_hour = item_hour.biz_hour
                    AND sale_hour.store_id = item_hour.store_id
                    AND sale_hour.item_id = item_hour.item_id
            )
            SELECT sale_hour_acc.*
            FROM sale_hour_acc
            FULL JOIN sale_min_hour
                ON sale_hour_acc.store_id = sale_min_hour.store_id
                AND sale_hour_acc.item_id = sale_min_hour.item_id
                AND sale_hour_acc.biz_date = sale_min_hour.biz_date
            WHERE (sale_hour_acc.biz_date + INTERVAL sale_hour_acc.biz_hour HOUR) < NOW()
              AND sale_hour_acc.biz_hour >= sale_min_hour.min_biz_hour
        """.trimIndent()

        private val CREATE_TABLE_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT NOT NULL,
                amount DECIMAL(18, 2)
            )
            DUPLICATE KEY(order_id)
            DISTRIBUTED BY HASH(order_id) BUCKETS 8;
        """.trimIndent()

        private val CREATE_TABLE_WITH_PROPERTIES_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT NOT NULL,
                amount DECIMAL(18, 2)
            )
            DUPLICATE KEY(order_id)
            DISTRIBUTED BY HASH(order_id) BUCKETS 8
            PROPERTIES (
                "compression" = "LZ4",
                "enable_persistent_index" = "true"
            );
        """.trimIndent()

        private val MIXED_CASE_CREATE_TABLE_SQL = """
            CREATE TABLE DWS.Sample_Orders (
                Order_ID BIGINT NOT NULL,
                Amount DECIMAL(18, 2)
            );
        """.trimIndent()

        private val LOCAL_TABLE_REFERENCE_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            SELECT o.order_id, h.amount
            FROM dws.sample_orders AS o
            JOIN dws.sample_orders h ON h.order_id = o.order_id;

            INSERT OVERWRITE TABLE sample_orders
            SELECT order_id, amount
            FROM dws.sample_orders;
        """.trimIndent()

        private val UNQUALIFIED_COLUMN_REFERENCE_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            SELECT order_id, amount
            FROM dws.sample_orders
            WHERE order_id > 0
            GROUP BY amount;
        """.trimIndent()

        private val DERIVED_TABLE_REFERENCE_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            SELECT
                d.order_key,
                d.amount,
                total_amount
            FROM (
                SELECT
                    o.order_id AS order_key,
                    o.amount,
                    sum(o.amount) AS total_amount
                FROM dws.sample_orders o
                GROUP BY o.order_id, o.amount
            ) d;
        """.trimIndent()

        private val CTE_OUTPUT_REFERENCE_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            WITH base AS (
                SELECT
                    o.order_id AS order_key,
                    o.amount
                FROM dws.sample_orders o
            )
            SELECT
                b.order_key,
                b.amount,
                order_key
            FROM base b;
        """.trimIndent()

        private val CTE_COLUMN_LIST_REFERENCE_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            WITH base(order_key, net_amount) AS (
                SELECT
                    o.order_id,
                    o.amount
                FROM dws.sample_orders o
            )
            SELECT
                b.order_key,
                b.net_amount,
                order_key
            FROM base b;
        """.trimIndent()

        private val TABLE_FUNCTION_ALIAS_COLUMN_REFERENCE_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                tags ARRAY<VARCHAR(32)>
            );

            SELECT
                u.tag,
                tag
            FROM dws.sample_orders o
            JOIN UNNEST(o.tags) AS u(tag)
            WHERE tag IS NOT NULL;
        """.trimIndent()

        private val ORDER_BY_REFERENCE_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            SELECT order_id, amount
            FROM dws.sample_orders
            ORDER BY amount DESC, order_id ASC
            LIMIT 10;
        """.trimIndent()

        private val SELECT_ALIAS_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            SELECT
                amount AS total_amount,
                order_id order_key,
                amount + order_id gross_amount,
                row_number() OVER recent_orders AS rn
            FROM dws.sample_orders
            WINDOW recent_orders AS (PARTITION BY order_id ORDER BY amount DESC)
            ORDER BY total_amount DESC, order_key ASC, gross_amount;
        """.trimIndent()

        private val TABLE_COMPLETION_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            SELECT order_id
            FROM <caret>;
        """.trimIndent()

        private val COLUMN_COMPLETION_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            SELECT <caret>
            FROM dws.sample_orders;
        """.trimIndent()

        private val ORDER_BY_ALIAS_COMPLETION_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            SELECT
                amount AS total_amount,
                order_id order_key
            FROM dws.sample_orders
            ORDER BY <caret>;
        """.trimIndent()

        private val DERIVED_TABLE_COLUMN_COMPLETION_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            SELECT *
            FROM (
                SELECT
                    o.order_id AS order_key,
                    o.amount
                FROM dws.sample_orders o
            ) d
            WHERE <caret>;
        """.trimIndent()

        private val CTE_COLUMN_COMPLETION_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            WITH base AS (
                SELECT
                    o.order_id AS order_key,
                    o.amount
                FROM dws.sample_orders o
            )
            SELECT *
            FROM base
            WHERE <caret>;
        """.trimIndent()

        private val CTE_COLUMN_LIST_COMPLETION_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            WITH base(order_key, net_amount) AS (
                SELECT
                    o.order_id,
                    o.amount
                FROM dws.sample_orders o
            )
            SELECT *
            FROM base b
            WHERE <caret>;
        """.trimIndent()

        private val TABLE_FUNCTION_ALIAS_COLUMN_COMPLETION_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                tags ARRAY<VARCHAR(32)>
            );

            SELECT *
            FROM dws.sample_orders o
            JOIN UNNEST(o.tags) AS u(tag)
            WHERE <caret>;
        """.trimIndent()

        private val COMPACT_QUERY_FORMATTER_SQL = "SELECT order_id,amount FROM dws.sample_orders WHERE amount>0 ORDER BY amount DESC"

        private val PROPERTY_COMPLETION_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT
            )
            DUPLICATE KEY(order_id)
            DISTRIBUTED BY HASH(order_id) BUCKETS 8
            PROPERTIES (
                <caret>
            );
        """.trimIndent()

        private val CTE_REFERENCE_SQL = """
            WITH base AS (
                SELECT order_id
                FROM dws.sample_orders
            )
            SELECT order_id
            FROM base;
        """.trimIndent()

        private val CREATE_VIEW_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT,
                amount DECIMAL(18, 2)
            );

            CREATE VIEW dws.v_sample_order_totals
            (
                order_id COMMENT 'order id',
                total_amount COMMENT 'total amount'
            )
            COMMENT 'sample order totals'
            AS
            SELECT
                order_id,
                sum(amount) AS total_amount
            FROM dws.sample_orders
            GROUP BY order_id;

            SELECT total_amount
            FROM dws.v_sample_order_totals;
        """.trimIndent()

        private val STARROCKS_FORMATTER_DDL_SQL = """
            CREATE TABLE dws.sample_orders (
                order_id BIGINT NOT NULL,
                amount DECIMAL(18, 2),
                tags ARRAY<VARCHAR(32)>
            )
            DUPLICATE KEY(order_id)
            PARTITION BY date_trunc('day', order_time)
            DISTRIBUTED BY HASH(order_id) BUCKETS 8
            PROPERTIES (
                "compression" = "LZ4",
                "enable_persistent_index" = "true"
            );

            CREATE MATERIALIZED VIEW dws.mv_sample_orders
            DISTRIBUTED BY HASH(order_id) BUCKETS 8
            REFRESH ASYNC
            PROPERTIES (
                "replication_num" = "1"
            )
            AS SELECT order_id, sum(amount) AS total_amount
            FROM dws.sample_orders
            GROUP BY order_id;
        """.trimIndent()
    }
}

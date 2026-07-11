package com.github.ycyz.starrocks.datagrip

import com.github.ycyz.starrocks.datagrip.completion.StarRocksCompletionScope
import com.github.ycyz.starrocks.datagrip.completion.StarRocksCompletionContext
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksNamedStubElement
import com.github.ycyz.starrocks.datagrip.lang.StarRocksParserDefinition
import com.github.ycyz.starrocks.datagrip.lang.StarRocksParserLexer
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.elementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.sql.psi.SqlCompositeElementTypes
import java.io.File

class StarRocksParsingTest : BasePlatformTestCase() {
    fun testBasicSelectParsesWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("SELECT 1")
        assertParsesWithoutPsiErrors("SELECT 1;")
        assertParsesWithoutPsiErrors("SELECT COUNT(*);")
        assertParsesWithoutPsiErrors("SELECT COUNT (*);")
        assertParsesWithoutPsiErrors("SELECT COUNT(*) FROM sales;")
        assertParsesWithoutPsiErrors(
            """
                SELECT
                    DATE_FORMAT(biz_date, '%Y-%m') AS biz_m,
                    COUNT(*)
                FROM dwm.dwm_trade_sale_ri_v2
                GROUP BY biz_m;
            """.trimIndent()
        )
    }

    fun testNonReservedKeywordsParseAsIdentifiers() {
        assertParsesWithoutPsiErrors(
            """
                SELECT tag, warehouse, datacache
                FROM tag
                WHERE tag = 'active';
            """.trimIndent()
        )
    }

    fun testNaturalJoinParsesWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("SELECT * FROM orders NATURAL JOIN customers;")
    }

    fun testSemiAndAntiJoinsParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors(
            "SELECT * FROM orders o LEFT ANTI JOIN test_orders t ON o.id = t.id;"
        )
        assertParsesWithoutPsiErrors(
            """
                CREATE TABLE inventory_history (biz_date DATE, item_id BIGINT)
                PARTITION BY RANGE(biz_date) (
                    START ('2025-04-01') END ('2035-01-01') EVERY (INTERVAL 1 DAY)
                )
                DISTRIBUTED BY HASH(item_id) BUCKETS 11;
            """.trimIndent()
        )
        assertParsesWithoutPsiErrors(
            "SELECT result.comment, properties, task FROM audit_result result;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT \"column-uuid\" FROM \"table-uuid\" WHERE is_del IS FALSE;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT * FROM orders o LEFT SEMI JOIN active_orders a ON o.id = a.id;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT * FROM orders o RIGHT ANTI JOIN archived_orders a ON o.id = a.id;"
        )
    }

    fun testMultiValueInListParsesWithoutPsiErrors() {
        val lexer = StarRocksParserLexer()
        lexer.start("${ '$' }{aff_biz_date}")
        assertSame(com.github.ycyz.starrocks.datagrip.lang.StarRocksHighlightTokenTypes.PARAMETER, lexer.tokenType)
        assertEquals("${ '$' }{aff_biz_date}".length, lexer.tokenEnd)
        assertParsesWithoutPsiErrors("SELECT * FROM orders WHERE status IN (2, 7, 9);")
        assertParsesWithoutPsiErrors("SELECT * FROM stores WHERE name IN ('正常营业', '营建中');")
        assertParsesWithoutPsiErrors("SELECT * FROM sales WHERE biz_date IN (${ '$' }{aff_biz_date});")
        assertParsesWithoutPsiErrors("SELECT * FROM sales WHERE biz_month <= ${ '$' }[yyyyMM];")
    }

    fun testStarRocksExpressionExtensionsParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("SELECT * FROM requests WHERE status NOT LIKE '%拒绝%';")
        assertParsesWithoutPsiErrors("SELECT * FROM requests WHERE status NOT BETWEEN 2 AND 7;")
        assertParsesWithoutPsiErrors(
            "SELECT GROUP_CONCAT(CASE WHEN active = 1 THEN user_name END SEPARATOR ',') FROM users;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT CAST(price AS DECIMAL(6, 3)) * CAST(quantity AS DECIMAL(6, 3)) FROM sales;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT SUM(CASE WHEN active = 1 THEN 1 ELSE 0 END * quantity) FROM sales;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT CURRENT_DATE(), CURRENT_TIMESTAMP(), CURRENT_DATE, CURRENT_TIMESTAMP FROM sales;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT GROUP_CONCAT(task_context ORDER BY task_rule_id DESC SEPARATOR ',') FROM tasks;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT CASE business_type WHEN 'PERSONAL' THEN '个人' ELSE business_type END business_type FROM stores;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT CASE WHEN turnover_date IS NULL THEN 'missing' ELSE 'normal' END flag FROM inventory;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT FIRST_VALUE(open_date IGNORE NULLS) OVER (PARTITION BY store_id ORDER BY open_date) FROM stores;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT DATE_ADD(created_at, INTERVAL CAST(duration_days AS INT) DAY) FROM events;"
        )
        assertParsesWithoutPsiErrors("SELECT [0, 1, 2, 3] AS hours;")
        assertParsesWithoutPsiErrors(
            "SELECT CASE WHEN (((YEAR(found_date)) = (YEAR(biz_date))) AND ((MONTH(found_date)) = (MONTH(biz_date)))) THEN 'yes' ELSE 'no' END FROM stores;"
        )
    }

    fun testLateralUnnestParsesWithoutPsiErrors() {
        assertParsesWithoutPsiErrors(
            """
                SELECT unnest
                FROM payloads
                CROSS JOIN LATERAL UNNEST(CAST(data AS ARRAY<JSON>));
            """.trimIndent()
        )
    }

    fun testDerivedTablesParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("SELECT * FROM (SELECT order_id FROM orders) nested_orders;")
        assertParsesWithoutPsiErrors(
            "SELECT * FROM orders a FULL JOIN (SELECT order_id FROM archived_orders) b ON a.order_id = b.order_id;"
        )
    }

    fun testCommonTableExpressionsParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("WITH base AS (SELECT 1) SELECT * FROM base;")
        assertParsesWithoutPsiErrors(
            "WITH ids AS (SELECT id FROM active_ids UNION ALL SELECT id FROM archived_ids) SELECT * FROM ids;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT a.id FROM active_ids a LEFT JOIN users u ON a.id = u.id UNION SELECT id FROM archived_ids;"
        )
        assertParsesWithoutPsiErrors(
            "WITH grouped AS (SELECT store_id FROM sales GROUP BY store_id ORDER BY store_id) SELECT * FROM grouped;"
        )
        assertParsesWithoutPsiErrors(
            "INSERT OVERWRITE sales PARTITION (p${ '$' }[yyyyMMdd-1]) WITH base AS (SELECT 1 AS id) SELECT * FROM base;"
        )
        assertParsesWithoutPsiErrors(
            "INSERT OVERWRITE TABLE sales PARTITION (biz_date = '${ '$' }[yyyy-MM-dd-1]') SELECT * FROM source_sales;"
        )
    }

    fun testKeywordFunctionWithWhitespaceParsesWithoutPsiErrors() {
        assertParsesWithoutPsiErrors(
            "SELECT * FROM inventory QUALIFY DENSE_RANK () OVER (ORDER BY biz_date DESC) = 1;"
        )
    }

    fun testWindowOrderingAndFramesParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors(
            "SELECT ROW_NUMBER() OVER (ORDER BY CASE WHEN active = 1 THEN 1 ELSE 9 END ASC) FROM users;"
        )
        assertParsesWithoutPsiErrors(
            "SELECT AVG(amount) OVER (PARTITION BY store_id ORDER BY biz_date ROWS BETWEEN 7 PRECEDING AND 1 PRECEDING) FROM sales;"
        )
    }

    fun testStringLiteralSelectAliasesParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("SELECT biz_date AS \"业务日期\" FROM sales;")
    }

    fun testForcedMaterializedViewRefreshParsesWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("REFRESH MATERIALIZED VIEW ads.daily_sales FORCE;")
        assertParsesWithoutPsiErrors(
            "REFRESH MATERIALIZED VIEW ads.daily_sales PARTITION START ('2026-07-01') END ('2026-07-12') FORCE;"
        )
    }

    fun testStarRocksOlapTableClausesParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors(
            """
                CREATE TABLE IF NOT EXISTS dws.sales (
                    biz_date DATE NOT NULL COMMENT 'business date',
                    store_id VARCHAR(65533) NOT NULL COMMENT 'store'
                ) ENGINE = OLAP
                PRIMARY KEY (biz_date, store_id)
                COMMENT "sales"
                PARTITION BY date_trunc('day', biz_date)
                DISTRIBUTED BY HASH(store_id) BUCKETS 12
                PROPERTIES (
                    "replication_num" = "3",
                    "enable_persistent_index" = "true"
                );
            """.trimIndent()
        )
        assertParsesWithoutPsiErrors(
            """
                CREATE TABLE member_identity (
                    identity_type VARCHAR(64),
                    uuid VARCHAR(64)
                ) ENGINE = OLAP
                PRIMARY KEY (identity_type, uuid)
                PARTITION BY LIST(identity_type) (
                    PARTITION p_weixin VALUES IN ("weixin_union_id"),
                    PARTITION p_mobile VALUES IN ("mobile")
                );
            """.trimIndent()
        )
        assertParsesWithoutPsiErrors("CREATE TABLE locations (left_distance NUMERIC(20, 4));")
        assertParsesWithoutPsiErrors(
            """
                CREATE TABLE stock (
                    biz_date DATE,
                    store_code VARCHAR(64),
                    item_code VARCHAR(64)
                ) ENGINE = OLAP
                PRIMARY KEY (biz_date, store_code, item_code)
                PARTITION BY date_trunc('year', biz_date)
                DISTRIBUTED BY HASH(biz_date, store_code, item_code) BUCKETS 12
                PROPERTIES ("replication_num" = "3");
            """.trimIndent()
        )
    }

    fun testSessionSetStatementParsesWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("SET SESSION new_planner_optimize_timeout = 10000;")
        assertParsesWithoutPsiErrors("BEGIN; START TRANSACTION; COMMIT; ROLLBACK;")
        assertParsesWithoutPsiErrors("CREATE USER 'etl_user' IDENTIFIED BY 'pw';")
        assertParsesWithoutPsiErrors("ALTER USER 'etl_user' IDENTIFIED BY 'pw2';")
        assertParsesWithoutPsiErrors("CREATE ROLE analyst_role;")
        assertParsesWithoutPsiErrors("ALTER ROLE analyst_role SET COMMENT \"read only analysts\";")
        assertParsesWithoutPsiErrors("SET PASSWORD FOR 'etl_user' = PASSWORD('pw3');")
        assertParsesWithoutPsiErrors("GRANT SELECT_PRIV ON TABLE dws.sample_orders TO ROLE analyst_role;")
        assertParsesWithoutPsiErrors("REVOKE SELECT_PRIV ON TABLE dws.sample_orders FROM ROLE analyst_role;")
        assertParsesWithoutPsiErrors("CALL refresh_order_stats();")
    }

    fun testUnicodeIdentifiersParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors(
            """
                SELECT
                    biz_date AS 交易日期,
                    物流中心编码 AS warehouse_center_code,
                    count(DISTINCT 物料代码) AS 商品数
                FROM 库存明细;
            """.trimIndent()
        )
        assertParsesWithoutPsiErrors("SELECT 7天日均, metrics.200公里以上箱数, .7 AS ratio FROM metrics;")
    }

    fun testCteUpdateParsesWithoutPsiErrors() {
        assertParsesWithoutPsiErrors(
            """
                WITH temp AS (SELECT order_id, settle_amt FROM settlements)
                UPDATE payments
                SET settle_amt = temp.settle_amt
                FROM temp
                WHERE payments.order_id = temp.order_id;
            """.trimIndent()
        )
    }

    fun testCteWithConsecutiveLeftJoinsParsesWithoutPsiErrors() {
        assertParsesWithoutPsiErrors(
            """
                WITH online AS (
                    SELECT a.biz_date, di.item_id, ds.store_id
                    FROM dwd.sale a
                    LEFT JOIN dwd.item di ON a.item_code = di.item_code
                    LEFT JOIN dwd.store ds ON a.store_code = ds.store_code
                    WHERE a.biz_date >= @start_date
                )
                SELECT * FROM online;
            """.trimIndent()
        )
    }

    fun testEveryDeclaredScenarioBuildsAnErrorFreePsiTree() {
        scenarioFixtureFiles().forEach { fixture ->
            assertParsesWithoutPsiErrors(fixture.readText(), fixture.path)
        }
    }

    fun testParserLexerTokensMatchGeneratedParserTokens() {
        val lexer = StarRocksParserLexer()
        lexer.start("SELECT 1;")
        val tokens = mutableListOf<Any>()
        while (lexer.tokenType != null) {
            lexer.tokenType
                ?.takeIf { it != TokenType.WHITE_SPACE }
                ?.let(tokens::add)
            lexer.advance()
        }

        assertEquals(
            listOf(
                StarRocksElementTypes.SELECT,
                StarRocksElementTypes.SQL_INTEGER_TOKEN,
                StarRocksElementTypes.SQL_SEMICOLON
            ),
            tokens
        )
    }

    fun testScreenshotQueryParsesWithoutPsiErrors() {
        val sql = """
            SELECT
                DATE_FORMAT(biz_date, '%Y-%m') AS biz_m,
                COUNT(*)
            FROM dwm.dwm_trade_sale_ri_v2
            GROUP BY biz_m;

            SHOW PARTITIONS FROM dws.dws_trade_sale_by_store_day_saletime_di;

            SELECT *
            FROM (
                SELECT
                    DATE_FORMAT(biz_date, '%Y-%m-%d') AS biz_m,
                    order_id,
                    order_detail_id
                FROM dwm.dwm_trade_sale_ri
                WHERE biz_date = '2025-10-27'
                GROUP BY biz_m, order_id, order_detail_id
            ) a1
            FULL JOIN (
                SELECT
                    DATE_FORMAT(biz_date, '%Y-%m-%d') AS biz_m,
                    order_id,
                    order_detail_id
                FROM dwm.dwm_trade_sale_ri_v2
            ) a2
            ON a1.order_id = a2.order_id;
        """.trimIndent()

        val lexer = StarRocksParserLexer()
        lexer.start(sql)
        assertSame(
            "Parser lexer SELECT token must be the same token consumed by the generated parser.",
            StarRocksElementTypes.SELECT,
            lexer.tokenType
        )

        assertParsesWithoutPsiErrors(sql)
    }

    fun testLocalColumnReferenceResolvesToCreateTableColumn() {
        val file = createPsiFile(
            """
                CREATE TABLE orders (order_id BIGINT, amount DOUBLE);
                SELECT order_id FROM orders;
            """.trimIndent()
        )
        val referenceName = elementsOfType(file, StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            .single { it.text.equals("order_id", ignoreCase = true) }
        val target = referenceName.reference?.resolve()

        assertNotNull("Local SELECT column should resolve to the preceding CREATE TABLE column.", target)
        assertEquals(StarRocksElementTypes.COLUMN_NAME, target?.node?.elementType)
        assertEquals("order_id", StarRocksNamedStubElement.normalizeName(target?.text))
    }

    fun testCompletionScopeUsesTheSameResolvedColumns() {
        val file = createPsiFile(
            """
                CREATE TABLE orders (order_id BIGINT, amount DOUBLE);
                SELECT order_ FROM orders;
            """.trimIndent()
        )
        val position = elementsOfType(file, StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            .single { it.text.equals("order_", ignoreCase = true) }
        val variants = StarRocksCompletionScope.columnNames(position, file)

        assertTrue("Completion must include columns exposed by local resolution: $variants", "order_id" in variants)
        assertTrue("Completion must include every visible local table column: $variants", "amount" in variants)
    }

    fun testOrderByCompletionExposesSelectAliases() {
        val file = createPsiFile("SELECT order_id AS order_key FROM orders ORDER BY order_;")
        val position = elementsOfType(file, StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            .last { it.text.equals("order_", ignoreCase = true) }
        val variants = StarRocksCompletionScope.selectAliasNames(position, file)

        assertTrue("ORDER BY completion must expose SELECT aliases: $variants", "order_key" in variants)
    }

    fun testCompletionContextPrefersPsiStructure() {
        val tableFile = createPsiFile("SELECT * FROM orders;")
        val tablePosition = elementsOfType(tableFile, SqlCompositeElementTypes.SQL_TABLE_REFERENCE).single()
        assertTrue(StarRocksCompletionContext.isTable(tablePosition))
        assertFalse(StarRocksCompletionContext.isColumn(tablePosition))

        val whereFile = createPsiFile("SELECT order_id FROM orders WHERE order_;")
        val wherePosition = elementsOfType(whereFile, StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            .last { it.text.equals("order_", ignoreCase = true) }
        assertTrue(StarRocksCompletionContext.isColumn(wherePosition))
        assertFalse(StarRocksCompletionContext.isTable(wherePosition))

        val orderFile = createPsiFile("SELECT order_id AS order_key FROM orders ORDER BY order_;")
        val orderPosition = elementsOfType(orderFile, StarRocksElementTypes.COLUMN_REFERENCE_NAME)
            .last { it.text.equals("order_", ignoreCase = true) }
        assertTrue(StarRocksCompletionContext.isOrderBy(orderPosition))
        assertTrue(StarRocksCompletionContext.isColumn(orderPosition))
    }

    fun testNamedElementRenameUsesStarRocksPsiFactory() {
        val file = createPsiFile("SELECT 1 AS old_name;")
        val alias = elementsOfType(file, StarRocksElementTypes.SELECT_ALIAS)
            .filterIsInstance<StarRocksNamedStubElement>()
            .single()

        WriteCommandAction.runWriteCommandAction(project) {
            alias.setName("new_name")
        }

        assertTrue(file.text.contains("new_name"))
        assertFalse(file.text.contains("old_name"))
    }

    fun testPlatformFormatterCanReformatStarRocksPsi() {
        val file = createPsiFile("SELECT order_id FROM orders WHERE order_id=1;")

        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(file)
        }

        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue("Reformatted StarRocks PSI must remain parseable: $errors", errors.isEmpty())
        assertTrue(file.text.contains("SELECT", ignoreCase = true))
    }

    private fun assertParsesWithoutPsiErrors(sql: String, fileName: String = "query.sql") {
        val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(StarRocksDialect.INSTANCE)
        assertTrue(
            "StarRocks files must use the plugin parser definition.",
            parserDefinition is StarRocksParserDefinition
        )
        val parserLexer = parserDefinition.createLexer(project)
        parserLexer.start(sql)
        if (sql.startsWith("SELECT")) {
            assertSame(
                "The parser definition must expose the generated parser's SELECT token.",
                StarRocksElementTypes.SELECT,
                parserLexer.tokenType
            )
        }
        val file = PsiFileFactory.getInstance(project)
            .createFileFromText(fileName, StarRocksDialect.INSTANCE, sql)
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)

        assertTrue(
            errors.joinToString(separator = "\n") { error ->
                val line = sql.take(error.textOffset).count { it == '\n' } + 1
                val lineStart = sql.lastIndexOf('\n', (error.textOffset - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
                val column = error.textOffset - lineStart + 1
                val snippetStart = (error.textOffset - 30).coerceAtLeast(0)
                val snippetEnd = (error.textOffset + 30).coerceAtMost(sql.length)
                val snippet = sql.substring(snippetStart, snippetEnd).replace('\n', ' ')
                val parentText = error.parent?.text?.replace('\n', ' ')?.take(220)
                "${error.errorDescription} at $line:$column in ${error.parent?.elementType}: `${error.text}` near `$snippet` parent `$parentText`"
            },
            errors.isEmpty()
        )
    }

    private fun createPsiFile(sql: String) = PsiFileFactory.getInstance(project)
        .createFileFromText("query.sql", StarRocksDialect.INSTANCE, sql)

    private fun elementsOfType(root: PsiElement, elementType: Any): List<PsiElement> {
        return PsiTreeUtil.collectElements(root) { it.node?.elementType == elementType }.toList()
    }

    private fun scenarioFixtureFiles(): List<File> {
        val testDataDir = File(System.getProperty("user.dir"), "src/testData/sql")
        val manifest = testDataDir.resolve("scenarios.properties")
        assertTrue("Missing scenario fixture manifest: ${manifest.absolutePath}", manifest.isFile)
        return manifest.readLines()
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .map { line -> testDataDir.resolve(line.substringBefore('=').trim()) }
            .onEach { fixture -> assertTrue("Missing scenario fixture: ${fixture.absolutePath}", fixture.isFile) }
    }
}

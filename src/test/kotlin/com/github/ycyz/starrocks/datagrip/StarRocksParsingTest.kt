package com.github.ycyz.starrocks.datagrip

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementFactory
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
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.sql.psi.SqlAsExpression
import com.intellij.sql.psi.SqlColumnDefinition
import com.intellij.sql.psi.SqlFunctionCallExpression
import com.intellij.sql.psi.SqlReferenceExpression
import com.intellij.sql.dialects.mysql.MysqlDialect
import com.intellij.database.model.ObjectKind
import com.github.ycyz.starrocks.datagrip.database.StarRocksDbms
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.database.dialects.generic.model.GenericDatabase
import com.intellij.database.dialects.generic.model.GenericModel
import com.intellij.database.dialects.generic.model.GenericSchema
import com.intellij.database.dialects.generic.model.GenericTable
import com.intellij.database.model.ModelFactory
import com.intellij.database.psi.DbDataSource
import com.intellij.database.psi.DbPsiFacade
import com.intellij.database.util.VirtualFileDataSourceProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.sql.dialects.SqlDataSourceMappings
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

    fun testTableSwapAndTableStatusScriptParsesWithoutPsiErrors() {
        assertParsesWithoutPsiErrors(
            """
                ALTER TABLE dws.current_sales SWAP WITH replacement_sales;
                SHOW TABLE STATUS FROM dws LIKE 'current_sales';
                CREATE TABLE final_table (
                    biz_date DATE NOT NULL,
                    order_id VARCHAR(36) NOT NULL
                ) ENGINE = OLAP
                PRIMARY KEY (biz_date, order_id)
                DISTRIBUTED BY HASH(order_id) BUCKETS 11
                PROPERTIES ("replication_num" = "3");
            """.trimIndent()
        )
    }

    fun testScreenshotCreateTableColumnsResolveInKeyPartitionAndDistributionClauses() {
        val file = createPsiFile(
            """
                CREATE TABLE `dwm_trade_sale_ri_v2` (
                    `biz_date` DATE NOT NULL COMMENT "业务日期",
                    `order_id` VARCHAR(36) NOT NULL COMMENT "订单ID",
                    `order_detail_id` VARCHAR(36) NOT NULL COMMENT "子订单ID",
                    `remark` VARCHAR(65533) NULL COMMENT "备注",
                    `etl_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "etl时间"
                ) ENGINE=OLAP
                PRIMARY KEY (`biz_date`, `order_id`, `order_detail_id`)
                COMMENT "交易_销售_业务宽表"
                PARTITION BY date_trunc('day', biz_date)
                DISTRIBUTED BY HASH(`order_id`, `order_detail_id`) BUCKETS 11
                PROPERTIES ("compression" = "LZ4");
            """.trimIndent()
        )
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue("Screenshot CREATE TABLE syntax must parse without errors: $errors\n${psiSummary(file)}", errors.isEmpty())

        val definitions = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_DEFINITION)
            .filterIsInstance<SqlColumnDefinition>()
            .associateBy { it.name }
        val clauseReferences = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
        assertEquals(2, clauseReferences.count { it.name == "biz_date" })
        assertEquals(2, clauseReferences.count { it.name == "order_id" })
        assertEquals(2, clauseReferences.count { it.name == "order_detail_id" })
        clauseReferences.forEach { reference ->
            assertSame(
                "DDL clause column ${reference.text} must resolve to its CREATE TABLE definition.",
                definitions[reference.name],
                reference.resolve()
            )
        }
    }

    fun testOlapEngineIsRecognizedAsStarRocksKeyword() {
        val lexer = StarRocksParserLexer()
        lexer.start("OLAP")
        assertSame(StarRocksElementFactory.token("OLAP"), lexer.tokenType)
        assertParsesWithoutPsiErrors("CREATE TABLE engine_probe (id BIGINT) ENGINE=OLAP;")
    }

    fun testEtlIdentifierAndSeparatorExtensionsParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors(
            """
                WITH 1st AS (SELECT 1 AS amount), 2nd AS (SELECT 2 AS amount)
                SELECT amount AS 券抵用金额（元） FROM 1st;
                WITH label AS (SELECT 1 AS id), location AS (SELECT 1 AS id)
                SELECT label.id FROM label JOIN location ON label.id = location.id;
                SELECT source.value FROM json_each('{}') AS source;
                SELECT lag(driver, 1) IGNORE NULLS OVER (PARTITION BY store_code ORDER BY biz_date)
                FROM delivery;
                SELECT value FROM source, LATERAL json_each(payload);
                SELECT ods.ds.gid FROM ods.ods_hd_erp_hd40_store AS ds;
                SELECT metrics.7qty, metrics.28qty FROM metrics;
                ALTER DATABASE dws SET DATA QUOTA 1024G;
                SELECT 1;;
            """.trimIndent()
        )
    }

    fun testParserLexerEmitsParametersAsSingleTokens() {
        listOf("?", ":biz_date", "${'$'}{biz_date}", "${'$'}[biz_date]").forEach { parameter ->
            val lexer = StarRocksParserLexer()
            lexer.start(parameter)
            assertSame(
                "Parser parameters must use the dedicated StarRocks parameter token.",
                com.github.ycyz.starrocks.datagrip.lang.StarRocksHighlightTokenTypes.PARAMETER,
                lexer.tokenType
            )
            assertEquals("Parameter placeholders must be emitted as one token.", parameter.length, lexer.tokenEnd)
        }
    }

    fun testColumnReferencesUsePlatformSqlPsi() {
        val file = createPsiFile(
            """
                CREATE TABLE orders (order_id BIGINT, amount DOUBLE);
                SELECT order_id FROM orders;
            """.trimIndent()
        )
        val definition = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_DEFINITION)
            .filterIsInstance<SqlColumnDefinition>()
            .single { it.name.equals("order_id", ignoreCase = true) }
        val referenceName = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single { it.text.equals("order_id", ignoreCase = true) }
        assertNotNull("Platform SQL column references must expose a SqlReference.", referenceName.reference)
        assertEquals("Column definitions must expose their platform name element.", "order_id", definition.nameElement?.name)
    }

    fun testAliasesUsePlatformSqlPsiAndResolveLocally() {
        val file = createPsiFile(
            "SELECT o.order_id AS order_key FROM orders AS o ORDER BY order_key;"
        )
        val selectAlias = elementsOfType(file, StarRocksElementTypes.SQL_AS_EXPRESSION)
            .filterIsInstance<SqlAsExpression>()
            .single { it.name == "order_key" }
        val tableAlias = elementsOfType(file, StarRocksElementTypes.SQL_AS_EXPRESSION)
            .filterIsInstance<SqlAsExpression>()
            .single { it.name == "o" }
        assertEquals("order_key", selectAlias.name)
        assertEquals("o", tableAlias.name)

        val qualifiedColumn = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single { it.text == "o.order_id" }
        val qualifier = qualifiedColumn.qualifierExpression as? SqlReferenceExpression
        assertNotNull("Qualified columns must expose the platform qualifier reference.", qualifier)
        assertSame("Table qualifiers must resolve through the platform alias PSI.", tableAlias, qualifier?.resolve())

        val orderByAlias = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single { it.text == "order_key" }
        assertSame("ORDER BY aliases must resolve through the platform alias PSI.", selectAlias, orderByAlias.resolve())
    }

    fun testGroupByAliasAndDerivedTableAliasesResolveLocally() {
        val file = createPsiFile(
            """
                SELECT DATE_FORMAT(biz_date, '%Y-%m') AS biz_m, COUNT(*)
                FROM (SELECT biz_date FROM sales) AS source_sales
                GROUP BY biz_m;
            """.trimIndent()
        )
        val aliases = elementsOfType(file, StarRocksElementTypes.SQL_AS_EXPRESSION)
            .filterIsInstance<SqlAsExpression>()
        val selectAlias = aliases.single { it.name == "biz_m" }
        val groupByAlias = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single { it.text == "biz_m" }
        assertSame("GROUP BY aliases must resolve through platform alias PSI.", selectAlias, groupByAlias.resolve())

        val tableAlias = elementsOfType(file, StarRocksElementTypes.SQL_AS_EXPRESSION)
            .filterIsInstance<SqlAsExpression>()
            .single { it.name == "source_sales" }
        assertEquals("source_sales", tableAlias.name)
    }

    fun testNamedWindowsUsePlatformDefinitionsAndReferences() {
        val file = createPsiFile(
            "SELECT row_number() OVER w FROM orders WINDOW w AS (ORDER BY order_id);"
        )
        val references = elementsOfType(file, StarRocksElementTypes.SQL_WINDOW_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
        assertEquals(2, references.size)
        val definition = elementsOfType(file, StarRocksElementTypes.SQL_GENERIC_DEFINITION).single()
        assertSame("Named window references must resolve to the platform window definition.", definition, references.first().resolve())
    }

    fun testDialectPublishesStarRocksTypesToPlatformCompletion() {
        val types = StarRocksDialect.INSTANCE.builtInTypes
        listOf("BIGINT", "DECIMAL128", "JSON", "ARRAY", "MAP", "STRUCT").forEach { type ->
            assertTrue("StarRocks dialect must publish $type through the platform type catalog.", type in types)
        }
    }

    fun testDialectPublishesFunctionsToPlatformCompletion() {
        assertTrue(
            "StarRocks built-in functions must be loaded through the platform function catalog.",
            StarRocksDialect.INSTANCE.supportedFunctions.contains("ABS")
        )
        listOf("COUNT", "DATE_FORMAT").forEach { function ->
            assertTrue(
                "StarRocks built-in function $function must be loaded through the platform function catalog.",
                StarRocksDialect.INSTANCE.supportedFunctions.contains(function)
            )
        }
    }

    fun testBuiltinFunctionCallsResolveThroughPlatformPsi() {
        val file = createPsiFile("SELECT DATE_FORMAT(biz_date, '%Y-%m'), COUNT(*) FROM sales;")
        val calls = elementsOfType(file, StarRocksElementTypes.SQL_FUNCTION_CALL)
        assertEquals(2, calls.size)
        val functionCalls = calls.filterIsInstance<SqlFunctionCallExpression>()
        assertEquals(2, functionCalls.size)
        functionCalls.forEach { call ->
            val nameElement = call.nameElement
            assertNotNull("Built-in function ${call.text} must expose a platform name element.", nameElement)
            val nameReference = nameElement?.reference
            assertNotNull("Built-in function ${call.text} must expose a platform name reference.", nameReference)
            if (call.text.startsWith("DATE_FORMAT")) {
                assertNotNull("DATE_FORMAT must resolve through the dialect catalog.", nameReference?.resolve())
            }
        }
    }

    fun testQualifiedTableNamesExposePlatformReferenceChain() {
        val file = createPsiFile("SELECT * FROM dws.dws_trade_sale_by_store_item_day_ri_v2;")
        val tableReference = elementsOfType(file, StarRocksElementTypes.SQL_TABLE_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single()
        assertEquals("dws.dws_trade_sale_by_store_item_day_ri_v2", tableReference.text)
        assertNotNull("Qualified table names must expose a platform reference.", tableReference.reference)
        assertNotNull("Qualified table names must expose a schema qualifier.", tableReference.qualifierExpression)
    }

    fun testStarRocksReferencePsiMatchesPlatformShape() {
        val sql = "SELECT DATE_FORMAT(t.biz_date, '%Y-%m') FROM dws.sales AS t GROUP BY t.biz_date;"
        val starRocks = createPsiFile(sql)
        val mysql = PsiFileFactory.getInstance(project)
            .createFileFromText("mysql.sql", MysqlDialect.INSTANCE, sql)

        val starRocksTable = elementsOfType(starRocks, StarRocksElementTypes.SQL_TABLE_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single()
        val mysqlTable = elementsOfType(mysql, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single()
        assertEquals(mysqlTable.javaClass, starRocksTable.javaClass)
        assertNotNull(starRocksTable.qualifierExpression)
        assertEquals(mysqlTable.qualifierExpression?.text, starRocksTable.qualifierExpression?.text)
        val starRocksTableQualifier = starRocksTable.qualifierExpression as SqlReferenceExpression
        val mysqlTableQualifier = mysqlTable.qualifierExpression as SqlReferenceExpression
        assertEquals(mysqlTableQualifier.kind, starRocksTableQualifier.kind)
        assertEquals(mysqlTableQualifier.referenceElementType, starRocksTableQualifier.referenceElementType)
        listOf(ObjectKind.SCHEMA, ObjectKind.TABLE).forEach { kind ->
            assertEquals(
                "Table reference part mismatch for $kind",
                mysqlTable.getReferencePart(kind),
                starRocksTable.getReferencePart(kind)
            )
        }

        val starRocksColumns = elementsOfType(starRocks, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .filter { it.qualifierExpression != null }
        assertTrue(starRocksColumns.isNotEmpty())
        assertTrue(starRocksColumns.all { it.qualifierExpression?.text == "t" })
        val starRocksColumn = starRocksColumns.first { it.text == "t.biz_date" }
        val mysqlColumn = elementsOfType(mysql, SqlCompositeElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .first { it.text == "t.biz_date" }
        assertEquals(mysqlColumn.name, starRocksColumn.name)
        assertEquals(mysqlColumn.identifier?.text, starRocksColumn.identifier?.text)
        assertEquals(mysqlColumn.referenceElementType, starRocksColumn.referenceElementType)
        assertEquals(mysqlColumn.kind, starRocksColumn.kind)
        listOf(ObjectKind.TABLE, ObjectKind.COLUMN).forEach { kind ->
            assertEquals(
                "Column reference part mismatch for $kind",
                mysqlColumn.getReferencePart(kind),
                starRocksColumn.getReferencePart(kind)
            )
        }

        assertTrue(
            "StarRocks SELECT must expose the same platform statement PSI shape.\n${psiSummary(starRocks)}",
            elementsOfType(starRocks, StarRocksElementTypes.SQL_SELECT_STATEMENT).isNotEmpty()
        )

        listOf(
            SqlCompositeElementTypes.SQL_SELECT_STATEMENT,
            SqlCompositeElementTypes.SQL_QUERY_EXPRESSION,
            SqlCompositeElementTypes.SQL_FROM_CLAUSE,
            SqlCompositeElementTypes.SQL_TABLE_EXPRESSION,
            SqlCompositeElementTypes.SQL_COLUMN_REFERENCE
        ).forEach { type ->
            val starRocksClasses = elementsOfType(starRocks, type).map { it.javaClass }.toSet()
            val mysqlClasses = elementsOfType(mysql, type).map { it.javaClass }.toSet()
            assertEquals("Platform PSI implementation mismatch for $type", mysqlClasses, starRocksClasses)
        }
    }

    fun testStarRocksOrdinaryObjectResolvePolicyMatchesPlatformDialect() {
        val sql = "SELECT t.biz_date FROM dws.sales AS t;"
        val starRocksFile = createPsiFile(sql)
        val mysqlFile = PsiFileFactory.getInstance(project)
            .createFileFromText("mysql.sql", MysqlDialect.INSTANCE, sql)
        val starRocksColumn = elementsOfType(starRocksFile, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single()
        val mysqlColumn = elementsOfType(mysqlFile, SqlCompositeElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single()
        val starRocksTable = elementsOfType(starRocksFile, StarRocksElementTypes.SQL_TABLE_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single()
        val mysqlTable = elementsOfType(mysqlFile, SqlCompositeElementTypes.SQL_TABLE_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single()

        assertEquals(
            MysqlDialect.INSTANCE.shallResolve(mysqlColumn, ObjectKind.COLUMN),
            StarRocksDialect.INSTANCE.shallResolve(starRocksColumn, ObjectKind.COLUMN)
        )
        assertEquals(
            MysqlDialect.INSTANCE.getParentDbTypes(mutableSetOf(), ObjectKind.COLUMN),
            StarRocksDialect.INSTANCE.getParentDbTypes(mutableSetOf(), ObjectKind.COLUMN)
        )
        assertEquals(
            MysqlDialect.INSTANCE.shallResolve(mysqlTable, ObjectKind.TABLE),
            StarRocksDialect.INSTANCE.shallResolve(starRocksTable, ObjectKind.TABLE)
        )
        assertEquals(
            MysqlDialect.INSTANCE.getParentDbTypes(mutableSetOf(), ObjectKind.TABLE),
            StarRocksDialect.INSTANCE.getParentDbTypes(mutableSetOf(), ObjectKind.TABLE)
        )
    }

    fun testColumnResolvesAgainstStarRocksDataSourceModel() {
        val model = ModelFactory.BLACK_HOLE.createModel(StarRocksDbms.INSTANCE, GenericModel::class.java)
        val database = model.root.databases.createOrGet("") as GenericDatabase
        database.isCurrent = true
        val schema = database.schemas.createOrGet("dws") as GenericSchema
        val table = schema.tables.createOrGet("sales") as GenericTable
        val column = table.columns.createOrGet("biz_date")
        val storeIdColumn = table.columns.createOrGet("store_id")
        val storesTable = schema.tables.createOrGet("stores") as GenericTable
        val storesStoreIdColumn = storesTable.columns.createOrGet("store_id")

        val dataSource = LocalDataSource.temporary().also {
            it.name = "StarRocks test"
            it.model = model
            it.introspectionScope = com.intellij.database.util.TreePattern(
                com.intellij.database.util.TreePatternUtils.create(
                    null as Array<com.intellij.database.model.ObjectName>?,
                    ObjectKind.DATABASE,
                    com.intellij.database.util.TreePatternUtils.create(
                        null as Array<com.intellij.database.model.ObjectName>?,
                        ObjectKind.SCHEMA
                    )
                )
            )
        }
        val manager = LocalDataSourceManager.getInstance(project)
        manager.addDataSource(dataSource)
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        val dbDataSource = DbPsiFacade.getInstance(project).findDataSource(dataSource.uniqueId)
        assertNotNull(dbDataSource)
        assertSame("DbPsi data source must expose the test model.", model, dbDataSource?.model)
        val schemaPsi = dbDataSource?.findElement(schema)
        val tablePsi = dbDataSource?.findElement(table)
        val columnPsi = dbDataSource?.findElement(column)
        val storeIdColumnPsi = dbDataSource?.findElement(storeIdColumn)
        val storesStoreIdColumnPsi = dbDataSource?.findElement(storesStoreIdColumn)
        assertNotNull("The test schema must be present in the DbPsi model.", schemaPsi)
        assertNotNull("The test table must be present in the DbPsi model.", tablePsi)
        assertNotNull("The test column must be present in the DbPsi model.", columnPsi)
        assertNotNull("The test store_id column must be present in the DbPsi model.", storeIdColumnPsi)

        VirtualFileDataSourceProvider.EP.point.registerExtension(
            object : VirtualFileDataSourceProvider() {
                override fun getDataSource(project: com.intellij.openapi.project.Project, file: VirtualFile): DbDataSource? {
                    return dbDataSource
                }
            },
            testRootDisposable
        )

        val file = createPsiFile("SELECT biz_date, s.biz_date FROM dws.sales AS s;")
        assertTrue(
            "The integration SQL file must be bound to the StarRocks data source.",
            SqlDataSourceMappings.getInstance(project).getDataSources(file).contains(dbDataSource)
        )
        val tableReference = elementsOfType(file, StarRocksElementTypes.SQL_TABLE_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single()
        assertSame("Data-source table must resolve before resolving its columns.", tablePsi, tableReference.resolve())
        assertSame(
            "The schema qualifier in a qualified table name must resolve independently.",
            schemaPsi,
            (tableReference.qualifierExpression as? SqlReferenceExpression)?.resolve()
        )
        val tableType = tableReference.dasType as? com.intellij.sql.psi.SqlTableType
        assertNotNull("Resolved table reference must expose SqlTableType.", tableType)
        assertEquals("Resolved table type must contain the introspected columns.", 2, tableType?.columnCount)
        val references = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
        assertEquals(2, references.size)
        references.forEach { reference ->
            assertSame(
                "Data-source column ${reference.text} must resolve through StarRocks dialect.",
                columnPsi,
                reference.resolve()
            )
        }

        val showPartitionsFile = createPsiFile("SHOW PARTITIONS FROM dws.sales;")
        val showPartitionsTable = elementsOfType(showPartitionsFile, StarRocksElementTypes.SQL_TABLE_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single()
        assertSame("SHOW PARTITIONS table must resolve.", tablePsi, showPartitionsTable.resolve())

        val showStatusFile = createPsiFile("SHOW TABLE STATUS FROM dws LIKE 'sales';")
        val showStatusSchema = elementsOfType(showStatusFile, StarRocksElementTypes.SQL_SCHEMA_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single()
        assertSame("SHOW TABLE STATUS schema must resolve.", schemaPsi, showStatusSchema.resolve())

        val swapFile = createPsiFile("ALTER TABLE dws.sales SWAP WITH dws.stores;")
        val swapTables = elementsOfType(swapFile, StarRocksElementTypes.SQL_TABLE_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
        assertEquals(2, swapTables.size)
        assertSame(tablePsi, swapTables[0].resolve())
        assertSame(dbDataSource?.findElement(storesTable), swapTables[1].resolve())

        listOf(
            "CREATE BITMAP INDEX idx_sales ON dws.sales (biz_date, store_id);",
            "INSERT INTO dws.sales (biz_date, store_id) SELECT biz_date, store_id FROM dws.sales;",
            "ANALYZE TABLE dws.sales (biz_date, store_id);",
            "ANALYZE TABLE dws.sales UPDATE HISTOGRAM ON biz_date, store_id;",
            "DESCRIBE dws.sales biz_date;",
            "UPDATE dws.sales SET biz_date = biz_date WHERE store_id = 1;",
            "DELETE FROM dws.sales WHERE biz_date = '2026-01-01';",
            "ALTER TABLE dws.sales DROP COLUMN biz_date;"
        ).forEach { sql ->
            val statementFile = createPsiFile(sql)
            val statementErrors = PsiTreeUtil.findChildrenOfType(statementFile, PsiErrorElement::class.java)
            assertTrue("Object-bearing statement must parse: $sql $statementErrors", statementErrors.isEmpty())
            val statementColumns = listOf(
                StarRocksElementTypes.SQL_COLUMN_REFERENCE,
                StarRocksElementTypes.SQL_COLUMN_SHORT_REFERENCE
            ).flatMap { type -> elementsOfType(statementFile, type) }
                .filterIsInstance<SqlReferenceExpression>()
            assertTrue("Statement must expose column references: $sql", statementColumns.isNotEmpty())
            statementColumns.forEach { reference ->
                val expected = when (reference.name) {
                    "biz_date" -> columnPsi
                    "store_id" -> storeIdColumnPsi
                    else -> null
                }
                assertSame("Column ${reference.text} must resolve in: $sql", expected, reference.resolve())
            }
        }

        val usingFile = createPsiFile(
            "SELECT s.biz_date FROM dws.sales AS s JOIN dws.stores AS t USING (store_id);"
        )
        val usingReference = elementsOfType(usingFile, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single { it.text == "store_id" }
        val usingTargets = usingReference.multiResolve(false).mapNotNull { it.element }.toSet()
        assertTrue(
            "JOIN USING column must resolve against at least one joined table: $usingTargets",
            usingTargets.any { it == storeIdColumnPsi || it == storesStoreIdColumnPsi }
        )

        val derivedFile = createPsiFile(
            "SELECT x.biz_m FROM (SELECT biz_date AS biz_m FROM dws.sales) AS x;"
        )
        val derivedAlias = elementsOfType(derivedFile, StarRocksElementTypes.SQL_AS_EXPRESSION)
            .filterIsInstance<SqlAsExpression>()
            .single { it.name == "biz_m" }
        val derivedColumn = elementsOfType(derivedFile, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single { it.text == "x.biz_m" }
        val derivedTableAlias = elementsOfType(derivedFile, StarRocksElementTypes.SQL_AS_EXPRESSION)
            .filterIsInstance<SqlAsExpression>()
            .single { it.name == "x" }
        assertTrue(
            "A derived-table alias must expose the subquery's SqlTableType.",
            derivedTableAlias.dasType is com.intellij.sql.psi.SqlTableType
        )
        assertSame(
            "Columns created with SELECT AS must resolve through a derived-table alias.",
            derivedAlias,
            derivedColumn.resolve()
        )

        val cteFile = createPsiFile(
            "WITH x AS (SELECT biz_date AS biz_m FROM dws.sales) SELECT x.biz_m FROM x;"
        )
        val cteAlias = elementsOfType(cteFile, StarRocksElementTypes.SQL_AS_EXPRESSION)
            .filterIsInstance<SqlAsExpression>()
            .single { it.name == "biz_m" }
        val cteColumn = elementsOfType(cteFile, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .single { it.text == "x.biz_m" }
        assertSame(
            "Columns created with SELECT AS must resolve through a CTE name.",
            cteAlias,
            cteColumn.resolve()
        )

        val multiCteFile = createPsiFile(
            "WITH a AS (SELECT biz_date FROM dws.sales), b AS (SELECT store_id FROM dws.stores) SELECT a.biz_date FROM a;"
        )
        val unresolvedMultiCteColumns = elementsOfType(multiCteFile, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .filter { it.resolve() == null }
            .map { it.text }
        assertTrue(
            "Fields from multiple CTEs must resolve without a JOIN: $unresolvedMultiCteColumns",
            unresolvedMultiCteColumns.isEmpty()
        )

        val directFullJoinFile = createPsiFile(
            "SELECT s.biz_date, t.store_id FROM dws.sales AS s FULL JOIN dws.stores AS t ON s.store_id = t.store_id;"
        )
        val unresolvedDirectFullJoinColumns = elementsOfType(directFullJoinFile, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .filter { it.resolve() == null }
            .map { it.text }
        assertTrue(
            "Fields from physical tables in a FULL JOIN must resolve: $unresolvedDirectFullJoinColumns",
            unresolvedDirectFullJoinColumns.isEmpty()
        )

        val fullJoinFile = createPsiFile(
            """
                WITH sales_cte AS (
                    SELECT biz_date, store_id FROM dws.sales
                ), stores_cte AS (
                    SELECT store_id FROM dws.stores
                )
                SELECT s.biz_date, t.store_id
                FROM sales_cte AS s
                FULL JOIN stores_cte AS t ON s.store_id = t.store_id;
            """.trimIndent()
        )
        val unresolvedFullJoinColumns = elementsOfType(fullJoinFile, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .filter { it.resolve() == null }
            .map { it.text }
        assertTrue(
            "Every field exposed by CTEs in a FULL JOIN must resolve: $unresolvedFullJoinColumns\n${psiSummary(fullJoinFile)}",
            unresolvedFullJoinColumns.isEmpty()
        )
    }

    fun testPlatformColumnDefinitionRenameUpdatesIdentifier() {
        val file = createPsiFile("CREATE TABLE rename_probe (old_name BIGINT);")
        val columns = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_DEFINITION)
            .filterIsInstance<SqlColumnDefinition>()
        assertEquals(psiSummary(file), 1, columns.size)
        val column = columns.single()
        val nameElement = requireNotNull(column.nameElement) {
            "Platform column definitions must expose their identifier as a SqlNameElement."
        }

        WriteCommandAction.runWriteCommandAction(project) {
            nameElement.setName("new_name")
        }

        assertTrue(file.text.contains("new_name"))
        assertFalse(file.text.contains("old_name"))
    }

    fun testExternalEtlProjectHasNoSyntaxErrors() {
        val rootPath = System.getenv("STARROCKS_ETL_ROOT") ?: return
        val root = File(rootPath)
        assertTrue("ETL project root does not exist: ${root.absolutePath}", root.isDirectory)
        val failures = mutableListOf<String>()
        val sqlFiles = root.walkTopDown()
            .filter { it.isFile && it.extension.equals("sql", ignoreCase = true) }
            .filterNot { sqlFile ->
                val sql = sqlFile.readText()
                sql.lineSequence().take(20).any { line ->
                    line.contains("-- type SERVERLESS_SPARK", ignoreCase = true)
                } || (
                    Regex("(?i)\\bFROM\\s+HD[0-9]+\\.").containsMatchIn(sql) &&
                        Regex("(?i)\\bTO_DATE\\s*\\(").containsMatchIn(sql)
                    )
            }
            .sortedBy { it.relativeTo(root).invariantSeparatorsPath }
            .toList()
        assertTrue("No SQL files found under ${root.absolutePath}", sqlFiles.isNotEmpty())

        sqlFiles.forEach { sqlFile ->
            val sql = sqlFile.readText()
            val file = PsiFileFactory.getInstance(project)
                .createFileFromText(sqlFile.name, StarRocksDialect.INSTANCE, sql)
            PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
                .take(10)
                .forEach { error ->
                    val line = sql.substring(0, error.textOffset.coerceAtMost(sql.length)).count { it == '\n' } + 1
                    val snippetStart = (error.textOffset - 40).coerceAtLeast(0)
                    val snippetEnd = (error.textOffset + 80).coerceAtMost(sql.length)
                    val snippet = sql.substring(snippetStart, snippetEnd).replace(Regex("\\s+"), " ")
                    failures += "${sqlFile.relativeTo(root).invariantSeparatorsPath}:$line: ${error.errorDescription} near `$snippet`"
                }
            elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
                .filter { it.text.startsWith("@") }
                .forEach { reference ->
                    failures += "${sqlFile.relativeTo(root).invariantSeparatorsPath}: " +
                        "SET/user variable was incorrectly parsed as a column reference: `${reference.text}`"
                }
        }

        assertTrue(
            "ETL PSI scan found ${failures.size} syntax errors in ${sqlFiles.size} SQL files:\n" +
                failures.take(300).joinToString("\n"),
            failures.isEmpty()
        )
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

    fun testSetAndExpressionVariablesAreNotColumnReferences() {
        val file = createPsiFile(
            "SET @start_date = '2026-01-01'; SELECT @start_date, date_sub(@start_date, 1);"
        )
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue("StarRocks variables must parse without errors: $errors", errors.isEmpty())
        assertTrue(
            "@ variables must never be exposed as column references.",
            elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE).none { it.text.startsWith("@") }
        )
        val variables = elementsOfType(file, StarRocksElementTypes.STARROCKS_VARIABLE_REFERENCE)
        assertEquals(3, variables.size)
        assertTrue(variables.all { it !is SqlReferenceExpression })
    }

    fun testUnnestColumnAliasResolvesThroughTableProcedureExpression() {
        val file = createPsiFile(
            """
                SELECT t.point_val, point_val
                FROM source_data AS s
                CROSS JOIN LATERAL UNNEST(split(s.sell_points, ',')) AS t(point_val);
            """.trimIndent()
        )
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue("LATERAL UNNEST with a column alias must parse: $errors", errors.isEmpty())
        assertEquals(
            1,
            elementsOfType(file, StarRocksElementTypes.SQL_TABLE_PROCEDURE_CALL_EXPRESSION).size
        )
        val aliases = elementsOfType(file, StarRocksElementTypes.STARROCKS_COLUMN_ALIAS_DEFINITION)
            .filterIsInstance<com.intellij.sql.psi.SqlColumnAliasDefinition>()
        assertEquals(psiSummary(file), 1, aliases.size)
        val alias = aliases.single()
        assertEquals(psiSummary(file), "point_val", alias.name)
        val outputReferences = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .filter { it.name == "point_val" }
        assertEquals(2, outputReferences.size)
        outputReferences.forEach { reference ->
            assertSame("UNNEST output ${reference.text} must resolve to its column alias.", alias, reference.resolve())
        }
    }

    fun testUnnestDefaultOutputColumnResolves() {
        val file = createPsiFile(
            """
                SELECT unnest, get_json_string(unnest, '$.id')
                FROM source_data AS s
                CROSS JOIN LATERAL UNNEST(s.data);
            """.trimIndent()
        )
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue("LATERAL UNNEST with its default output name must parse: $errors", errors.isEmpty())
        val tableExpression = elementsOfType(file, StarRocksElementTypes.SQL_TABLE_PROCEDURE_CALL_EXPRESSION).single()
        assertTrue(psiSummary(file), tableExpression is com.intellij.sql.psi.SqlExplicitTableExpression)
        val tableType = (tableExpression as com.intellij.sql.psi.SqlExpression).dasType
            as com.intellij.database.types.DasTableType
        assertEquals(1, tableType.columnCount)
        assertTrue(tableType.getColumnName(0).equals("unnest", ignoreCase = true))
        val outputReferences = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
            .filter { it.name.equals("unnest", ignoreCase = true) }
        assertEquals(2, outputReferences.size)
        outputReferences.forEach { reference ->
            assertNotNull(
                "The default UNNEST output ${reference.text} must resolve through the table expression.",
                reference.resolve()
            )
        }
    }

    fun testCommaUnnestCanReferencePreviousDerivedTableAndExposeDefaultColumn() {
        val file = createPsiFile(
            """
                SELECT CAST(unnest AS INT) AS biz_hour
                FROM (
                    SELECT [0, 1, 2, 3] AS date_list
                ) a, UNNEST(date_list);
            """.trimIndent()
        )
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue("Implicit lateral comma UNNEST must parse: $errors\n${psiSummary(file)}", errors.isEmpty())
        val references = elementsOfType(file, StarRocksElementTypes.SQL_COLUMN_REFERENCE)
            .filterIsInstance<SqlReferenceExpression>()
        val dateList = references.single { it.name == "date_list" }
        val unnest = references.single { it.name.equals("unnest", ignoreCase = true) }
        assertNotNull("UNNEST argument must resolve to the preceding derived-table output.\n${psiSummary(file)}", dateList.resolve())
        assertNotNull("Default UNNEST output must resolve in the SELECT list.\n${psiSummary(file)}", unnest.resolve())
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

    private fun psiSummary(root: PsiElement): String = PsiTreeUtil.collectElements(root) { true }
        .joinToString("\n") { element ->
            "${element.node?.elementType}: ${element.javaClass.name} `${element.text.take(80)}`"
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

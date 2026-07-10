package com.github.ycyz.starrocks.datagrip

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.github.ycyz.starrocks.datagrip.lang.StarRocksParserDefinition
import com.github.ycyz.starrocks.datagrip.lang.StarRocksParserLexer
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

class StarRocksParsingTest : BasePlatformTestCase() {
    fun testBasicSelectParsesWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("SELECT 1;")
        assertParsesWithoutPsiErrors("SELECT COUNT(*);")
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

    fun testDerivedTablesParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("SELECT * FROM (SELECT order_id FROM orders) nested_orders;")
        assertParsesWithoutPsiErrors(
            "SELECT * FROM orders a FULL JOIN (SELECT order_id FROM archived_orders) b ON a.order_id = b.order_id;"
        )
    }

    fun testCommonTableExpressionsParseWithoutPsiErrors() {
        assertParsesWithoutPsiErrors("WITH base AS (SELECT 1) SELECT * FROM base;")
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

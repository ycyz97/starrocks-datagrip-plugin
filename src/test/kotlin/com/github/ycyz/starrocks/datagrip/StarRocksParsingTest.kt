package com.github.ycyz.starrocks.datagrip

import com.github.ycyz.starrocks.datagrip.completion.StarRocksCompletionScope
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

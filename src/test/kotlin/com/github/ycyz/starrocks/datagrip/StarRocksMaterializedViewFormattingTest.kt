package com.github.ycyz.starrocks.datagrip

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.github.ycyz.starrocks.datagrip.format.StarRocksCodeStyleSettings
import com.github.ycyz.starrocks.datagrip.format.StarRocksFormatterHelper
import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.intellij.application.options.CodeStyle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.sql.formatter.settings.SqlCodeStyleSettings
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

class StarRocksMaterializedViewFormattingTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        val manager = CodeStyleSettingsManager.getInstance(project)
        manager.setTemporarySettings(manager.currentSettings.clone())
    }

    override fun tearDown() {
        try {
            CodeStyleSettingsManager.getInstance(project).dropTemporarySettings()
        } finally {
            super.tearDown()
        }
    }

    fun testShowCreateFormattingWithGeneralStyle() = checkShowCreateFormatting(true)

    fun testShowCreateFormattingWithDialectStyle() = checkShowCreateFormatting(false)

    private fun checkShowCreateFormatting(useGeneralStyle: Boolean) {
        val sql = File("src/testData/format/materialized-view-show-create.sql").readText()
        val file = parse(sql)
        val settings = CodeStyle.getSettings(file)
        val dialectStyle = settings.getCustomSettings(StarRocksCodeStyleSettings::class.java)
        dialectStyle.USE_GENERAL_STYLE = useGeneralStyle
        val style = if (useGeneralStyle) settings.getCustomSettings(SqlCodeStyleSettings::class.java) else dialectStyle
        style.TABLE_COLLAPSE = false
        style.TABLE_OPENING = 1
        style.TABLE_CONTENT = 2
        style.TABLE_CLOSING = 3
        style.VIEW_WRAP_AS = true
        style.VIEW_WRAP_QUERY = true
        style.VIEW_INDENT_QUERY = false
        assertNoErrors(file)
        val originalTokens = tokens(file)
        assertEquals(53, aliases(file).size)
        reformat(file)
        assertNoErrors(file)
        assertEquals("Formatting must preserve every SQL token", originalTokens, tokens(file))
        assertEquals("Every declared column must survive formatting", 53, aliases(file).size)
        aliases(file).forEach { alias ->
            assertFalse("A column and its comment must stay together: ${alias.text}", alias.text.contains('\n'))
            assertTrue(alias.text.contains("COMMENT"))
            val line = file.text.substring(0, alias.textOffset).substringAfterLast('\n')
            assertEquals("Columns must use one indent", "    ", line)
        }
        val formatted = file.text
        reformat(file)
        assertEquals("Formatting must be idempotent", formatted, file.text)
    }

    fun testPartitionExpressionsWithAndWithoutOuterParentheses() {
        for (expression in listOf("biz_date", "date_trunc('day', biz_date)")) {
            for (partition in listOf(expression, "($expression)")) {
                val file = parse("CREATE MATERIALIZED VIEW mv PARTITION BY $partition REFRESH MANUAL AS SELECT biz_date FROM t;")
                assertNoErrors(file)
                reformat(file)
                assertNoErrors(file)
                assertEquals(1, PsiTreeUtil.collectElements(file) {
                    it.node?.elementType == StarRocksElementTypes.PARTITION_CLAUSE
                }.size)
            }
        }
    }

    fun testMalformedParenthesizedPartitionsStillReportErrors() {
        for (partition in listOf("()", "(biz_date", "(biz_date +)", "(date_trunc('day', biz_date)")) {
            val file = parse("CREATE MATERIALIZED VIEW mv PARTITION BY $partition REFRESH MANUAL AS SELECT biz_date FROM t;")
            assertFalse("Malformed partition must not be silently accepted: $partition",
                PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java).isEmpty())
        }
    }

    fun testOnlyDeclarationColumnsUseViewColumnFormatting() {
        val matcher = StarRocksFormatterHelper().complexBlockCreation.single().matcher
        for (kind in listOf("VIEW", "MATERIALIZED VIEW")) {
            val file = parse("CREATE $kind v (a COMMENT 'column') AS WITH c(a) AS (SELECT 1) SELECT t.a FROM c AS t(a);")
            assertNoErrors(file)
            val lists = PsiTreeUtil.collectElements(file) {
                it.node?.elementType == SqlCompositeElementTypes.SQL_COLUMN_ALIAS_LIST
            }.sortedBy { it.textOffset }
            assertEquals("Declaration, CTE, and table aliases must remain structured", 3, lists.size)
            assertTrue(matcher.matches(lists.first().node))
            lists.drop(1).forEach { assertFalse("Query aliases must keep their platform layout", matcher.matches(it.node)) }
            reformat(file)
            assertNoErrors(file)
            assertTrue(file.text.contains("c(a)"))
            assertTrue(file.text.contains("t(a)"))
        }
    }

    private fun aliases(file: PsiFile) = PsiTreeUtil.collectElements(file) {
        it.node?.elementType == StarRocksElementTypes.STARROCKS_COLUMN_ALIAS_DEFINITION &&
            it.parent?.parent?.node?.elementType == SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT
    }

    private fun tokens(file: PsiFile) = PsiTreeUtil.collectElements(file) {
        it.firstChild == null && it.node?.elementType != TokenType.WHITE_SPACE
    }.map { it.text }

    private fun parse(sql: String) = PsiFileFactory.getInstance(project)
        .createFileFromText("materialized-view.sql", StarRocksDialect.INSTANCE, sql.replace("\r\n", "\n"))

    private fun assertNoErrors(file: PsiFile) {
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue("Unexpected parse errors: ${errors.map { it.errorDescription }}\n${file.text}", errors.isEmpty())
    }

    private fun reformat(file: PsiFile) {
        WriteCommandAction.runWriteCommandAction(project) { CodeStyleManager.getInstance(project).reformat(file) }
    }
}

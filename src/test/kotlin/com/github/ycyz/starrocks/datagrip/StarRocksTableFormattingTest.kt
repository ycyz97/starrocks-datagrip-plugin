package com.github.ycyz.starrocks.datagrip

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.application.options.CodeStyle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.sql.dialects.SqlDialectMappings
import com.intellij.sql.formatter.settings.SqlCodeStyleSettings
import com.intellij.sql.psi.SqlCompositeElementTypes
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.FileContentUtil

class StarRocksTableFormattingTest : BasePlatformTestCase() {
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

    fun testOnlineSaleTableFormatting() {
        val sql = """
            CREATE TABLE IF NOT EXISTS dwd.dwd_trade_sale_online_di_v2 (
                biz_date DATE NOT NULL COMMENT '业务日期',
                uuid string NOT NULL COMMENT 'UUID标识',
                order_id string NULL COMMENT '原订单号',
                shop_id string NULL COMMENT '平台id',
                shop_name string NULL COMMENT '平台名称',
                refund_id string NULL COMMENT '平台退货单号',
                refund_order TINYINT NULL COMMENT '是否退单',
                item_code string NULL COMMENT '线上销售码',
                create_time DATETIME NULL COMMENT '平台创建时间',
                modified_time DATETIME NULL COMMENT '最后修改时间',
                tenant_id string NULL COMMENT '租户ID',
                settle_date DATETIME NULL COMMENT '结算日期',
                state string NULL COMMENT '状态',
                customer_name string NULL COMMENT '客户名称',
                customer_mobile string NULL COMMENT '客户手机号',
                store_code string NULL COMMENT '平台门店代码',
                delivery_state string NULL COMMENT '配送状态',
                receiver_name string NULL COMMENT '收货人姓名',
                receiver_tel string NULL COMMENT '收货人电话',
                front_order_id string NULL COMMENT '第三方单号',
                settle_commission DECIMAL(18, 6) NULL COMMENT '平台佣金',
                delivery_amt DECIMAL(18, 6) NULL COMMENT '配送运费',
                discount_amt DECIMAL(18, 6) NULL COMMENT '促销优惠额',
                pay_amt DECIMAL(18, 6) NULL COMMENT '实付金额',
                settle_amt DECIMAL(18, 6) NULL COMMENT '结算金额',
                item_qty DECIMAL(18, 6) NULL COMMENT '确认数量',
                receive_time DATETIME NULL COMMENT '数据接收时间',
                etl_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'etl时间'
            ) ENGINE = OLAP PRIMARY KEY ( biz_date, `uuid` )
                COMMENT "交易_线上_线上销售明细"
                PARTITION BY DATE_TRUNC('day', biz_date)
                DISTRIBUTED BY HASH ( uuid) BUCKETS 11
                PROPERTIES (
                    "replication_num" = "3",
                    "in_memory" = "false",
                    "enable_persistent_index" = "true",
                    "replicated_storage" = "true",
                    "compression" = "LZ4"
                    );
        """.trimIndent()
        val file = PsiFileFactory.getInstance(project)
            .createFileFromText("comparison.sql", StarRocksDialect.INSTANCE, sql)
        val style = CodeStyle.getSettings(file).getCustomSettings(SqlCodeStyleSettings::class.java)
        style.KEYWORD_CASE = 2
        style.TYPE_CASE = 3
        style.CUSTOM_TYPE_CASE = 3
        style.TABLE_OPENING = 1
        style.TABLE_CONTENT = 2
        style.TABLE_CLOSING = 3
        assertTrue(PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java).isEmpty())
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(file)
        }
        assertEquals("Column definitions must survive case conversion and formatting", 28,
            PsiTreeUtil.collectElements(file) { it.node?.elementType == SqlCompositeElementTypes.SQL_COLUMN_DEFINITION }.size)
        assertTrue("Formatted SQL must remain parseable", PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java).isEmpty())
        assertTrue("Case conversion must actually run", Regex("uuid\\s+STRING NOT NULL COMMENT 'UUID标识'").containsMatchIn(file.text))
        assertTrue("DECIMAL parameters must stay together", file.text.contains("DECIMAL(18, 6)"))
        val formatted = file.text
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(file)
        }
        assertEquals("Formatting must be idempotent", formatted, file.text)
    }

    fun testEditingColumnTypePreservesColumnListStructure() {
        myFixture.configureByText("column-edit.sql", "CREATE TABLE t (id BIGINT, amount DECIMAL(18, 2) COMMENT 'amount');")
        val virtualFile = myFixture.file.virtualFile
        SqlDialectMappings.getInstance(project).setMapping(virtualFile, StarRocksDialect.INSTANCE)
        FileContentUtil.reparseFiles(project, listOf(virtualFile), true)
        assertEquals(2, PsiTreeUtil.collectElements(myFixture.file) {
            it.node?.elementType == SqlCompositeElementTypes.SQL_COLUMN_DEFINITION
        }.size)
        WriteCommandAction.runWriteCommandAction(project) {
            val document = myFixture.editor.document
            val offset = document.text.indexOf("18, 2")
            document.replaceString(offset, offset + 5, "20, 6")
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
        assertTrue(PsiTreeUtil.findChildrenOfType(myFixture.file, PsiErrorElement::class.java).isEmpty())
        assertEquals("An editor change must preserve column definitions", 2,
            PsiTreeUtil.collectElements(myFixture.file) { it.node?.elementType == SqlCompositeElementTypes.SQL_COLUMN_DEFINITION }.size)
        assertTrue(myFixture.file.text.contains("DECIMAL(20, 6) COMMENT 'amount'"))
    }
}

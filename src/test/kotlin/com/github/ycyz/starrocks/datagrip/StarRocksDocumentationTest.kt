package com.github.ycyz.starrocks.datagrip

import com.github.ycyz.starrocks.datagrip.database.StarRocksDbms
import com.github.ycyz.starrocks.datagrip.documentation.StarRocksDocumentationTargetProvider
import com.github.ycyz.starrocks.datagrip.documentation.StarRocksTableDocumentationTarget
import com.intellij.database.Dbms
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.database.dialects.generic.model.GenericDatabase
import com.intellij.database.dialects.generic.model.GenericModel
import com.intellij.database.dialects.generic.model.GenericSchema
import com.intellij.database.dialects.generic.model.GenericTable
import com.intellij.database.model.ModelFactory
import com.intellij.database.psi.DbPsiFacade
import com.intellij.database.psi.DbTableImpl
import com.intellij.database.psi.documentation.DbTableDocumentationTarget
import com.intellij.database.util.DbImplUtilCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import java.sql.SQLException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicInteger

class StarRocksDocumentationTest : BasePlatformTestCase() {
    private val dataSources = mutableListOf<LocalDataSource>()

    override fun tearDown() {
        try {
            dataSources.forEach { LocalDataSourceManager.getInstance(project).removeDataSource(it) }
        } finally {
            super.tearDown()
        }
    }

    fun testStarRocksProviderIsSelectedBeforePlatformTableDocumentation() {
        val table = table()
        val selected = PsiDocumentationTargetProvider.EP_NAME.extensionList.firstNotNullOfOrNull {
            it.documentationTarget(table, null)
        }
        assertTrue("The installed extension must handle native StarRocks table documentation", selected is StarRocksTableDocumentationTarget)
        val genericTable = table(Dbms.UNKNOWN)
        assertTrue("Other DBMSes must retain the platform provider", PsiDocumentationTargetProvider.EP_NAME.extensionList
            .firstNotNullOfOrNull { it.documentationTarget(genericTable, null) } is DbTableDocumentationTarget)
        assertNull(StarRocksDocumentationTargetProvider().documentationTarget(genericTable, null))
    }

    fun testNativeDdlLoadsOffTheUiThreadWithoutReadLockAndPreservesStarRocksClauses() {
        val table = table()
        assertTrue(table.documentationBase.toString().contains("auto-generated definition"))
        val calls = AtomicInteger()
        val target = StarRocksTableDocumentationTarget(table) {
            val app = ApplicationManager.getApplication()
            assertFalse("JDBC must not run on the UI thread", app.isDispatchThread)
            assertFalse("JDBC must not hold the PSI read lock", app.isReadAccessAllowed)
            calls.incrementAndGet()
            """
                CREATE TABLE ods.hover_probe (id BIGINT COMMENT '<script>column</script>')
                ENGINE=OLAP PRIMARY KEY(id)
                DISTRIBUTED BY HASH(id) BUCKETS 3
                PROPERTIES("replication_num"="3");
            """.trimIndent()
        }
        val result = target.computeDocumentation()
        assertEquals("Creating a documentation target must not execute SQL", 0, calls.get())
        val html = evaluate(result)!!
        assertEquals(1, calls.get())
        assertFalse(html.contains("auto-generated definition"))
        listOf("ENGINE", "OLAP", "PRIMARY", "DISTRIBUTED", "BUCKETS", "replication_num").forEach {
            assertTrue("Original DDL must retain $it", html.contains(it))
        }
        assertFalse("SQL comments must not inject HTML", html.contains("<script>"))
        assertTrue("Data source names must be escaped", html.contains("StarRocks &amp; hover"))
    }

    fun testUnavailableNativeDdlRetainsLabeledPlatformFallback() {
        val table = table()
        for (loader in listOf<(DbTableImpl) -> String>({ "" }, { throw SQLException("offline") })) {
            val html = evaluate(StarRocksTableDocumentationTarget(table, loader).computeDocumentation())!!
            assertTrue(html.contains("auto-generated definition"))
            assertTrue(html.contains("hover_probe"))
        }
    }

    fun testCancellationIsNotTurnedIntoGeneratedDocumentation() {
        val cancelled = ProcessCanceledException()
        val result = StarRocksTableDocumentationTarget(table()) { throw cancelled }.computeDocumentation()
        try {
            evaluate(result)
            fail("Cancellation must propagate")
        } catch (error: ExecutionException) {
            assertSame(cancelled, error.cause)
        }
    }

    fun testPointerRestoresTheNativeDocumentationTarget() {
        val calls = AtomicInteger()
        val target = StarRocksTableDocumentationTarget(table()) {
            calls.incrementAndGet()
            "CREATE TABLE ods.hover_probe (id BIGINT) ENGINE=OLAP;"
        }
        val restored = target.createPointer().dereference()
        assertTrue(restored is StarRocksTableDocumentationTarget)
        assertFalse(evaluate(restored!!.computeDocumentation()!!)!!.contains("auto-generated definition"))
        assertEquals(1, calls.get())
    }

    private fun table(dbms: Dbms = StarRocksDbms.INSTANCE): DbTableImpl {
        val model = ModelFactory.BLACK_HOLE.createModel(dbms, GenericModel::class.java)
        val database = model.root.databases.createOrGet("") as GenericDatabase
        database.isCurrent = true
        val schema = database.schemas.createOrGet("ods") as GenericSchema
        val table = schema.tables.createOrGet("hover_probe") as GenericTable
        table.columns.createOrGet("id")
        val source = LocalDataSource.temporary().also {
            it.name = "StarRocks & hover"
            it.model = model
        }
        dataSources += source
        LocalDataSourceManager.getInstance(project).addDataSource(source)
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        val dbSource = DbPsiFacade.getInstance(project).findDataSource(source.uniqueId)!!
        return DbImplUtilCore.findElement(dbSource, table) as DbTableImpl
    }

    // Exercise the platform's actual async wrapper, including its thread switch.
    // These result implementations are internal; production code uses public APIs only.
    @Suppress("UNCHECKED_CAST")
    private fun evaluate(result: DocumentationResult): String? {
        val supplier = result.javaClass.getMethod("getSupplier").invoke(result)
            as suspend () -> DocumentationResult.Documentation?
        val future = CompletableFuture.supplyAsync {
            runBlocking {
                val data = supplier()
                data?.javaClass?.getMethod("getHtml")?.invoke(data) as? String
            }
        }
        PlatformTestUtil.waitWithEventsDispatching("native DDL documentation", { future.isDone }, 15)
        return future.get()
    }
}

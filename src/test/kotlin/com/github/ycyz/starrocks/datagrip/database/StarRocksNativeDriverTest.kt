package com.github.ycyz.starrocks.datagrip.database

import com.intellij.database.dataSource.DatabaseConnectionCore
import com.intellij.database.dataSource.DatabaseConnectionPoint
import com.intellij.database.dataSource.DatabaseDriverManager
import com.intellij.database.dataSource.artifacts.DatabaseArtifactManager
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.model.ObjectKind
import com.intellij.database.remote.jdbc.helpers.GenericJdbcHelper
import com.intellij.database.remote.jdbc.impl.RemoteDriverImpl
import com.intellij.database.util.DbImplUtilCore
import com.intellij.database.util.ObjectPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.starrocks.cj.jdbc.Driver
import java.lang.reflect.Proxy
import java.sql.SQLException
import java.util.Properties

class StarRocksNativeDriverTest : BasePlatformTestCase() {
    fun testPlatformLoadsNativeDriverAndLatestStableArtifact() {
        val driver = DatabaseDriverManager.getInstance().getDriver("starrocks")!!
        assertEquals("com.starrocks.cj.jdbc.Driver", driver.driverClass)
        assertTrue(driver.sampleUrl, driver.sampleUrl.startsWith("jdbc:starrocks:"))
        assertEquals("StarRocks ConnectorJ", driver.artifacts.single().id)
        assertNull(driver.artifacts.single().artifactVersion)
        assertEquals("stable", driver.artifacts.single().channel)
        assertEquals("1.1.1", DatabaseArtifactManager.resolveVersion(driver, driver.artifacts.single())!!.version.toString())
        assertFalse(driver.driverProperties.containsKey("databaseTerm"))
        assertFalse(driver.driverProperties.containsKey("allowMultiQueries"))
        assertEquals("false", driver.driverProperties["useLocalSessionState"])
    }

    fun testNativeDriverExposesTwoNamespaceLevelsByDefault() {
        val connection = starRocksJdbcConnection("jdbc:starrocks://localhost:9030/default_catalog.ads", emptyMap())
        assertTrue(connection.metaData.schemaTerm.isNotEmpty())
        assertTrue(connection.metaData.catalogTerm.isNotEmpty())
        assertFalse(GenericJdbcHelper("StarRocks", "3.3.22", connection, javaClass.classLoader).jdbcCatalogIsSchema)
        assertTrue(Driver().acceptsURL("jdbc:starrocks://localhost:9030/default_catalog.ads"))
        assertFalse(Driver().acceptsURL("jdbc:mysql://localhost:9030/ads"))
    }

    fun testActualDriverAndPlatformSendSeparateCatalogAndDatabaseCommands() {
        // A wire-level protocol fixture, not a real StarRocks server. It exercises
        // the real driver and Database Tools namespace dispatcher without mocking setters.
        NamespaceProtocolServer().use { server ->
            val remote = RemoteDriverImpl.wrap(Driver(), Driver::class.java.classLoader, false).connect(
                "jdbc:starrocks://127.0.0.1:${server.port}/", Properties().apply {
                setProperty("user", "test")
                setProperty("password", "")
                setProperty("sslMode", "DISABLED")
                setProperty("useLocalSessionState", "false")
                setProperty("allowMultiQueries", "false")
                setProperty("socketTimeout", "5000")
                }, emptyMap())
            try {
                val source = LocalDataSource.temporary().apply { info.setDbms(StarRocksDbms.INSTANCE) }
                val point = proxy<DatabaseConnectionPoint> { name -> if (name == "getDataSource") source else error(name) }
                val core = proxy<DatabaseConnectionCore> { name -> when (name) {
                    "getRemoteConnection" -> remote
                    "getConnectionPoint" -> point
                    "getDbms" -> StarRocksDbms.INSTANCE
                    else -> error(name)
                } }
                for ((catalog, database) in listOf("paimon_catalog_fs" to "work_temp", "external_two" to "work_temp", "default_catalog" to "ads", "external_two" to "work_temp")) {
                    server.commands.clear()
                    val path = ObjectPath.create(catalog, ObjectKind.DATABASE).append(database, ObjectKind.SCHEMA)
                    assertTrue(DbImplUtilCore.switchSchemaThroughJdbcImpl(path, core))
                    assertEquals(listOf("SET CATALOG `$catalog`", "USE `$catalog`.`$database`"), server.commands.toList())
                    assertEquals(catalog, server.catalog)
                    assertEquals(database, server.database)
                    assertEquals(catalog, StarRocksDatabaseDialect().tryToLoadSearchPath(core)!!.current.parent.name)
                }
                server.commands.clear()
                server.reject = "SET CATALOG `denied`"
                try {
                    DbImplUtilCore.switchSchemaThroughJdbcImpl(ObjectPath.create("denied", ObjectKind.DATABASE).append("ads", ObjectKind.SCHEMA), core)
                    fail("Expected a server privilege error")
                } catch (error: SQLException) {
                    assertEquals("42000", error.sqlState)
                }
                assertEquals(listOf("SET CATALOG `denied`"), server.commands.toList())
                assertEquals("external_two", server.catalog)
                server.commands.clear()
                server.reject = "USE `default_catalog`.`denied`"
                // DataGrip 261's standard RemoteConnectionImpl.setSchema uses a
                // reflective helper that swallows invocation exceptions. Record this
                // platform limitation and verify server state rather than trusting the return.
                DbImplUtilCore.switchSchemaThroughJdbcImpl(ObjectPath.create("default_catalog", ObjectKind.DATABASE).append("denied", ObjectKind.SCHEMA), core)
                assertEquals(listOf("SET CATALOG `default_catalog`", "USE `default_catalog`.`denied`"), server.commands.toList())
                val actual = StarRocksDatabaseDialect().tryToLoadSearchPath(core)!!
                assertEquals(ObjectKind.DATABASE, actual.current.kind)
                assertEquals("default_catalog", actual.current.name)
            } finally { remote.close() }
            server.assertHealthy()
        }
    }

    private inline fun <reified T> proxy(crossinline call: (String) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ -> call(method.name) } as T
}

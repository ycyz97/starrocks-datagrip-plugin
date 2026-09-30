package com.github.ycyz.starrocks.datagrip.database

import com.intellij.database.Dbms
import com.intellij.database.dataSource.DatabaseConnectionInterceptor
import com.intellij.database.dataSource.DatabaseConnectionPoint
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.remote.jdbc.helpers.GenericJdbcHelper
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.lang.reflect.Proxy

class StarRocksConnectionInterceptorTest : BasePlatformTestCase() {
    fun testActualConnectorJDefaultCollapsesNamespacesButSchemaModeDoesNot() {
        val default = connectorJConnection("jdbc:mysql://localhost:9030", emptyMap())
        assertEquals("database", default.metaData.catalogTerm)
        assertEquals("", default.metaData.schemaTerm)
        assertTrue(GenericJdbcHelper("MySQL", "4.1", default, javaClass.classLoader).jdbcCatalogIsSchema)

        val fixture = intercept("jdbc:mysql://localhost:9030/sales", mutableMapOf())
        val configured = connectorJConnection(fixture.first, fixture.second)
        assertEquals("CATALOG", configured.metaData.catalogTerm)
        assertEquals("SCHEMA", configured.metaData.schemaTerm)
        assertFalse(GenericJdbcHelper("MySQL", "4.1", configured, javaClass.classLoader).jdbcCatalogIsSchema)
    }

    fun testExistingUrlAndAdvancedPropertiesCannotKeepCatalogMode() {
        val url = "jdbc:mysql://localhost:9030/sales?useSSL=false&databaseTerm=CATALOG&characterEncoding=utf8"
        val result = intercept(url, mutableMapOf("databaseTerm" to "CATALOG", "tinyInt1isBit" to "false"))
        assertEquals(url.replace("databaseTerm=CATALOG", "databaseTerm=SCHEMA"), result.first)
        assertEquals(mapOf("databaseTerm" to "SCHEMA", "tinyInt1isBit" to "false"), result.second)
        val connection = connectorJConnection(result.first, result.second)
        assertFalse(GenericJdbcHelper("MySQL", "4.1", connection, javaClass.classLoader).jdbcCatalogIsSchema)
        assertEquals(result, intercept(result.first, result.second.toMutableMap()))
    }

    fun testOrdinaryMysqlConnectionsAreUnchanged() {
        val url = "jdbc:mysql://localhost:3306/app?databaseTerm=CATALOG"
        val properties = mutableMapOf("databaseTerm" to "CATALOG")
        assertEquals(url to properties, intercept(url, properties.toMutableMap(), Dbms.MYSQL))
    }

    fun testDriverDefaultsDeclareSchemaMode() {
        val config = javaClass.getResourceAsStream("/databaseDrivers/starrocks-drivers.xml")!!.bufferedReader().use { it.readText() }
        assertTrue(config.contains("<property name=\"databaseTerm\" value=\"SCHEMA\"/>"))
    }

    private fun intercept(initialUrl: String, properties: MutableMap<String, String>, dbms: Dbms = StarRocksDbms.INSTANCE): Pair<String, Map<String, String>> {
        var url = initialUrl
        val source = LocalDataSource.temporary().apply { info.setDbms(dbms) }
        val point = proxy<DatabaseConnectionPoint> { name, _ ->
            if (name == "getDataSource") source else error(name)
        }
        val proto = proxy<DatabaseConnectionInterceptor.ProtoConnection> { name, args ->
            when (name) {
                "getConnectionPoint" -> point
                "getConnectionProperties" -> properties
                "getUrl" -> url
                "setUrl" -> { url = args[0] as String; null }
                else -> error(name)
            }
        }
        val interceptor = DatabaseConnectionInterceptor.EP_NAME.extensionList.filterIsInstance<StarRocksConnectionInterceptor>().single()
        assertTrue(interceptor.interceptConnectionFast(proto, true))
        return url to properties
    }

    private inline fun <reified T> proxy(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}

package com.github.ycyz.starrocks.datagrip.database

import com.intellij.database.dataSource.DatabaseConnectionCore
import com.intellij.database.dataSource.DataSourceBriefConfig
import com.intellij.database.dataSource.DataSourceInfo
import com.intellij.database.dataSource.DatabaseConnectionInterceptor
import com.intellij.database.dataSource.DatabaseConnectionPoint
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.connection.ConnectionRequestor
import com.intellij.database.dataSource.connection.audit.AdvancedAuditService
import com.intellij.database.dataSource.connection.audit.AuditService
import com.intellij.database.dialects.base.introspector.jdbc.wrappers.DatabaseMetaDataWrapper
import com.intellij.database.dialects.base.introspector.jdbc.wrappers.DatabaseMetaDataWrapper.Schema
import com.intellij.database.dialects.base.introspector.jdbc.wrappers.DatabaseMetaDataWrapper.Table
import com.intellij.database.dialects.base.introspector.jdbc.JdbcIntrospector
import com.intellij.database.introspection.DBIntrospectionContext
import com.intellij.database.introspection.DBIntrospectionOptions
import com.intellij.database.introspection.DBIntrospector
import com.intellij.database.dialects.generic.model.GenericModel
import com.intellij.database.dialects.generic.model.GenericDatabase
import com.intellij.database.dialects.generic.model.GenericSchema
import com.intellij.database.model.ModelFactory
import com.intellij.database.model.ObjectKind
import com.intellij.database.model.ObjectName
import com.intellij.database.DbmsExtension
import com.intellij.database.remote.jdba.RemoteFacade
import com.intellij.database.remote.jdba.core.ConnectionInfo
import com.intellij.database.remote.jdbc.RemoteConnection
import com.intellij.database.remote.jdbc.RemoteDatabaseMetaData
import com.intellij.database.remote.jdbc.RemoteResultSet
import com.intellij.database.remote.jdbc.RemoteStatement
import com.intellij.database.remote.jdbc.helpers.GenericJdbcHelper
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.database.util.TreePattern
import com.intellij.database.util.TreePatternUtils
import com.intellij.database.util.Version
import com.thoughtworks.xstream.io.HierarchicalStreamReader
import com.intellij.util.containers.JBIterable
import java.lang.reflect.Proxy
import java.sql.SQLException
import java.sql.Types

class StarRocksMetadataWrapperTest : BasePlatformTestCase() {
    private val queries = mutableListOf<String>()
    private var statementCloses = 0
    private var resultCloses = 0
    private var effectiveJdbcConnection: java.sql.Connection? = null

    fun testGenericIntrospectorReproducesCachedMysqlNamespaceCollapse() {
        val wrapper = wrapper(catalogIsSchema = true) { sql ->
            assertEquals("SHOW CATALOGS", sql)
            listOf(listOf("default_catalog"), listOf("paimon_catalog_fs"))
        }
        val introspector = introspector(wrapper.connection, JdbcIntrospector.Factory(StarRocksDbms.INSTANCE))
        try {
            introspector.introspectNamespaces()
            val model = introspector.model as GenericModel
            assertEquals(listOf(""), model.root.databases.map { it.name })
            val catalog = model.root.databases.single() as GenericDatabase
            assertEquals(setOf("default_catalog", "paimon_catalog_fs"), catalog.schemas.map { it.name }.toSet())
            assertTrue(queries.isNotEmpty())
            assertTrue(queries.all { it == "SHOW CATALOGS" })
        } finally {
            introspector.detachFromDB()
        }
    }

    fun testPlatformIntrospectionKeepsCatalogDatabaseTablesAndViewsWithCachedMysqlFlag() {
        assertPlatformIntrospection(catalogIsSchema = true, reuseOldModel = false)
    }

    fun testFullRefreshReplacesOldCollapsedModelAndKeepsNamespacesAfterReconnect() {
        assertPlatformIntrospection(catalogIsSchema = true, reuseOldModel = true)
    }

    fun testFreshDataSourceKeepsCatalogDatabaseTablesAndViews() {
        assertPlatformIntrospection(catalogIsSchema = false, reuseOldModel = false)
    }

    private fun assertPlatformIntrospection(catalogIsSchema: Boolean, reuseOldModel: Boolean) {
        val wrapper = wrapper(catalogIsSchema = catalogIsSchema) { sql ->
            when {
                sql == "SHOW CATALOGS" -> listOf(listOf("default_catalog"), listOf("paimon_catalog_fs"))
                sql == "SHOW DATABASES FROM `default_catalog`" || sql == "SHOW DATABASES FROM `paimon_catalog_fs`" ->
                    listOf(listOf("sales"))
                sql.matches(Regex("SHOW FULL TABLES FROM `(default_catalog|paimon_catalog_fs)`\\.`sales`")) ->
                    listOf(listOf("orders", "BASE TABLE"), listOf("v_orders", "VIEW"), listOf("mv_orders", "MATERIALIZED VIEW"))
                sql.matches(Regex("SHOW FULL COLUMNS FROM `(default_catalog|paimon_catalog_fs)`\\.`sales`\\.`(orders|v_orders|mv_orders)`")) ->
                    listOf(listOf("id", "bigint", "", "NO", "", null, "", "", "identifier"))
                else -> error("Unexpected SQL (catalog may have been treated as database): $sql")
            }
        }
        val factory = DbmsExtension<DBIntrospector.Factory>("com.intellij.database.introspector").forDbms(StarRocksDbms.INSTANCE)
        assertTrue(factory is JdbcIntrospector.Factory)
        configureJdbcConnection(wrapper.connection)
        val oldModel = if (reuseOldModel) {
            ModelFactory.BLACK_HOLE.createModel(StarRocksDbms.INSTANCE, GenericModel::class.java).also { model ->
                val emptyCatalog = model.root.databases.createOrGet("") as GenericDatabase
                emptyCatalog.schemas.createOrGet("default_catalog")
                emptyCatalog.schemas.createOrGet("paimon_catalog_fs")
            }
        } else null
        val introspector = introspector(wrapper.connection, factory, oldModel)
        try {
            // Reattach to exercise the cached flag on every connection, not just initialization.
            repeat(2) {
                introspector.introspectNamespaces()
                val model = introspector.model as GenericModel
                assertEquals(setOf("default_catalog", "paimon_catalog_fs"), model.root.databases.map { it.name }.toSet())
                for (catalog in model.root.databases) {
                    val schemas = (catalog as GenericDatabase).schemas
                    assertEquals(listOf("sales"), schemas.map { it.name })
                    val schema = schemas.single() as GenericSchema
                    introspector.introspectAuto(arrayOf(schema))
                    assertEquals(listOf("orders"), schema.tables.map { it.name })
                    assertEquals(setOf("v_orders", "mv_orders"), schema.views.map { it.name }.toSet())
                    for (table in schema.tables.toList() + schema.views.toList()) {
                        assertEquals(listOf("id"), table.columns.map { it.name })
                        assertEquals("identifier", table.columns.single().comment)
                    }
                }
                assertFalse(wrapper.connection.connectionPoint.dataSource.info.isJdbcCatalogIsSchema)
                introspector.detachFromDB()
                configureJdbcConnection(wrapper.connection)
                introspector.attachToDB(wrapper.connection)
            }
        } finally {
            introspector.detachFromDB()
        }
        assertTrue(queries.contains("SHOW DATABASES FROM `paimon_catalog_fs`"))
        assertEquals(queries.size, statementCloses)
        assertEquals(queries.size, resultCloses)
    }

    private fun configureJdbcConnection(connection: DatabaseConnectionCore) {
        var url = "jdbc:mysql://localhost:9030/sales?databaseTerm=CATALOG"
        val properties = mutableMapOf("databaseTerm" to "CATALOG")
        val proto = proxy<DatabaseConnectionInterceptor.ProtoConnection> { name, args ->
            when (name) {
                "getConnectionPoint" -> connection.connectionPoint
                "getConnectionProperties" -> properties
                "getUrl" -> url
                "setUrl" -> { url = args[0] as String; null }
                else -> error(name)
            }
        }
        val interceptor = DatabaseConnectionInterceptor.EP_NAME.extensionList.filterIsInstance<StarRocksConnectionInterceptor>().single()
        assertTrue(interceptor.interceptConnectionFast(proto, true))
        effectiveJdbcConnection = connectorJConnection(url, properties)
        DataSourceInfo.refreshDatabaseInfo(connection.connectionPoint.dataSource, connection)
    }

    private fun introspector(connection: DatabaseConnectionCore, factory: DBIntrospector.Factory, oldModel: GenericModel? = null): JdbcIntrospector {
        val context = proxy<DBIntrospectionContext> { name, _ ->
            when (name) {
                "getProject" -> project
                "getUserNotifier" -> proxy<DBIntrospectionContext.UserNotifier> { _, _ -> null }
                else -> error(name)
            }
        }
        val scope = TreePattern(TreePatternUtils.create(null as Array<ObjectName>?, ObjectKind.DATABASE,
            TreePatternUtils.create(null as Array<ObjectName>?, ObjectKind.SCHEMA)))
        val source = connection.connectionPoint.dataSource
        val config = proxy<DataSourceBriefConfig> { name, _ ->
            when (name) {
                "getUniqueId" -> source.uniqueId
                "getName" -> source.name
                "getDbms" -> StarRocksDbms.INSTANCE
                "getIntrospectionScope" -> scope
                "isRewriteBounds" -> false
                else -> null
            }
        }
        return (factory.createIntrospector(context, StarRocksDbms.INSTANCE, ModelFactory.BLACK_HOLE) as JdbcIntrospector).also {
            it.progressIndicator = EmptyProgressIndicator()
            it.errorSink = java.util.function.BiConsumer { message, cause -> throw AssertionError(message, cause) }
            it.options = DBIntrospectionOptions(DBIntrospectionOptions.SourceLoading.NO_SOURCES, scope)
            it.init(oldModel, config, scope)
            it.attachToDB(connection)
        }
    }

    fun testCatalogsAndDatabasesKeepTheirParentsWithoutChangingSession() {
        val wrapper = wrapper { sql ->
            when (sql) {
                "SHOW CATALOGS" -> listOf(listOf("default_catalog"), listOf("hive"))
                "SHOW DATABASES FROM `default_catalog`", "SHOW DATABASES FROM `hive`" -> listOf(listOf("sales"))
                else -> error(sql)
            }
        }
        val schemas = wrapper.schemas(JBIterable.empty()).use { it.asSequence().toList() }
        assertEquals(listOf("default_catalog", "hive"), schemas.map { it.database })
        assertEquals(listOf("sales", "sales"), schemas.map { it.schema })
        assertEquals(3, queries.size)
        assertEquals(3, statementCloses)
        assertEquals(3, resultCloses)
    }

    fun testTableAndBothViewTypesAndColumns() {
        val schema = Schema("sales", "paimon_catalog_fs")
        val wrapper = wrapper { sql ->
            when (sql) {
                "SHOW FULL TABLES FROM `paimon_catalog_fs`.`sales`" -> listOf(
                    listOf("orders", "BASE TABLE"), listOf("v_orders", "VIEW"), listOf("mv_orders", "MATERIALIZED VIEW")
                )
                "SHOW FULL COLUMNS FROM `paimon_catalog_fs`.`sales`.`orders`" -> listOf(
                    listOf("amount", "decimal(18,2)", "", "NO", "", "0", "", "", "amount comment"),
                    listOf("tags", "array<varchar(20)>", "", "YES", "", null, "", "", "tags")
                )
                else -> throw SQLException("SELECT on information_schema denied: $sql", "42000", 5203)
            }
        }
        val tables = wrapper.tables(schema, null, wrapper.allTableTypes).use { it.asSequence().toList() }
        assertEquals(listOf("TABLE", "VIEW", "VIEW"), tables.map { it.type })
        assertTrue(tables.all { it.schema === schema })
        wrapper.tableColumns(schema, null, listOf("orders")).use { groups ->
            assertTrue(groups.hasNext())
            assertEquals("orders", groups.next().name)
            val columns = groups.groupIt().asSequence().map {
                listOf(it.name, it.typeName, it.type, it.position, it.nullable, it.def, it.comment, it.length)
            }.toList()
            assertEquals(listOf("amount", "decimal(18,2)", Types.DECIMAL, 1, false, "0", "amount comment", -1), columns[0])
            assertEquals(listOf("tags", "array<varchar(20)>", Types.ARRAY, 2, true, null, "tags", -1), columns[1])
        }
        assertTrue(queries.all { it.startsWith("SHOW ") && it.contains("`paimon_catalog_fs`.`sales`") })
        assertEquals(2, statementCloses)
        assertEquals(2, resultCloses)
    }

    fun testUnsupportedObjectsNeverCallJdbcMetadataOrExecuteSql() {
        val wrapper = wrapper { error("Unexpected SQL: $it") }
        val schema = Schema("sales", "default_catalog")
        val table = Table(schema, "orders")
        listOf(wrapper.tableKeys(table), wrapper.tableFKeyColumns(table), wrapper.tableIndexColumns(table),
            wrapper.tableVersionColumns(table), wrapper.routines(schema, null, null),
            wrapper.userDefinedTypes(schema, null), wrapper.userDefinedTypeAttributes(schema, null, emptySet()),
            wrapper.routineArguments(schema, null) { error("Unexpected routine") }
        ).forEach { it.use { assertFalse(it.hasNext()) } }
        assertTrue(queries.isEmpty())
    }

    fun testQueryFailureClosesStatementAndDoesNotPretendNamespaceIsEmpty() {
        val wrapper = wrapper { throw SQLException("Permission denied") }
        try {
            wrapper.databases()
            fail("Query failure must reach the introspector")
        } catch (expected: SQLException) {
            assertEquals("Permission denied", expected.message)
        }
        assertEquals(1, statementCloses)
    }

    fun testGroupedViewsStillLoadTheirNativeDdl() {
        val model = ModelFactory.BLACK_HOLE.createModel(StarRocksDbms.INSTANCE, GenericModel::class.java)
        val catalog = model.root.databases.createOrGet("default_catalog") as GenericDatabase
        val schema = catalog.schemas.createOrGet("sales") as GenericSchema
        val view = schema.views.createOrGet("v_orders")
        val materialized = schema.views.createOrGet("mv_orders")
        val wrapper = wrapper { sql ->
            when (sql) {
                "SHOW CREATE TABLE `default_catalog`.`sales`.`v_orders`" -> listOf(listOf("v_orders", "CREATE VIEW v_orders AS SELECT 1"))
                "SHOW CREATE TABLE `default_catalog`.`sales`.`mv_orders`" -> listOf(listOf("mv_orders", "CREATE MATERIALIZED VIEW mv_orders AS SELECT 1"))
                else -> error(sql)
            }
        }
        val definitions = StringBuilder()
        StarRocksDefinitionProvider().load(listOf(view, materialized), wrapper.connection, definitions)
        assertTrue(definitions.contains("CREATE VIEW v_orders AS SELECT 1"))
        assertTrue(definitions.contains("CREATE MATERIALIZED VIEW mv_orders AS SELECT 1"))
        assertEquals(2, resultCloses)
        assertEquals(2, statementCloses)
    }

    fun testQuotedNamesAreCatalogSpecific() {
        val schema = Schema("sales'2026", "hi`ve")
        assertEquals("SHOW FULL TABLES FROM `hi``ve`.`sales'2026` LIKE 'order\\\\_%'",
            StarRocksMetadataWrapper.tablesQuery(schema, "order\\_%"))
        assertEquals("SHOW FULL COLUMNS FROM `hi``ve`.`sales'2026`.`order_%``x`",
            StarRocksMetadataWrapper.columnsQuery(schema, "order_%`x"))
    }

    fun testColumnRefreshWithoutExpectedTablesUsesVisibleTablesAndResetsPositions() {
        val schema = Schema("sales", "paimon_catalog_fs")
        val wrapper = wrapper { sql ->
            when (sql) {
                "SHOW FULL TABLES FROM `paimon_catalog_fs`.`sales` LIKE 'order%'" ->
                    listOf(listOf("orders_b", "BASE TABLE"), listOf("orders_a", "BASE TABLE"))
                "SHOW FULL COLUMNS FROM `paimon_catalog_fs`.`sales`.`orders_a`",
                "SHOW FULL COLUMNS FROM `paimon_catalog_fs`.`sales`.`orders_b`" ->
                    listOf(listOf("id", "bigint", "", "NO", "YES", null, "", "", ""))
                else -> error(sql)
            }
        }
        val tables = mutableListOf<String>()
        wrapper.tableColumns(schema, "order%", null).use { groups ->
            while (groups.hasNext()) {
                tables += groups.next().name
                val columns = groups.groupIt()
                assertTrue(columns.hasNext())
                assertEquals(1, columns.next().position)
                assertFalse(columns.hasNext())
            }
        }
        assertEquals(listOf("orders_a", "orders_b"), tables)
        assertEquals(3, queries.size)
    }

    fun testEmptyExpectedTablesDoNotQueryTheDatabase() {
        val wrapper = wrapper { error("Unexpected SQL: $it") }
        wrapper.tableColumns(Schema("sales", "paimon_catalog_fs"), null, emptyList()).use {
            assertFalse(it.hasNext())
        }
        assertTrue(queries.isEmpty())
    }

    private fun wrapper(catalogIsSchema: Boolean? = null, rows: (String) -> List<List<Any?>>): DatabaseMetaDataWrapper {
        val metadata = proxy<RemoteDatabaseMetaData> { name, _ ->
            if (catalogIsSchema == null) error("Unexpected JDBC metadata call: $name")
            when (name) {
                "storesLowerCaseIdentifiers", "storesUpperCaseIdentifiers", "storesMixedCaseIdentifiers",
                "supportsMixedCaseIdentifiers", "storesLowerCaseQuotedIdentifiers", "storesUpperCaseQuotedIdentifiers",
                "storesMixedCaseQuotedIdentifiers", "supportsMixedCaseQuotedIdentifiers" -> false
                "getJdbcCatalogIsSchema" -> GenericJdbcHelper("MySQL", "4.1", effectiveJdbcConnection!!, javaClass.classLoader).jdbcCatalogIsSchema
                "getDatabaseProductName" -> "StarRocks"
                "getDatabaseProductVersion" -> "4.1"
                "getDriverName" -> "MySQL Connector/J"
                "getDriverVersion" -> "9.4.0"
                "getJDBCMajorVersion" -> 4
                "getJDBCMinorVersion" -> 2
                "getIdentifierQuoteString" -> "`"
                "getExtraNameCharacters" -> ""
                else -> error("Unexpected JDBC metadata call: $name")
            }
        }
        val facade = proxy<RemoteFacade> { name, _ ->
            when (name) {
                "getConnectionInfo" -> ConnectionInfo("StarRocks", "sales", null, "test",
                    com.intellij.database.remote.jdba.util.Version.of(4, 1),
                    com.intellij.database.remote.jdba.util.Version.of(8, 0))
                "isConnected" -> true
                "connect", "detachFromDB", "disconnect" -> null
                else -> error("Unexpected facade call: $name")
            }
        }
        val remote = proxy<RemoteConnection> { name, _ ->
            if (name == "createStatement") proxy<RemoteStatement> { method, args ->
                when (method) {
                    "executeQuery" -> {
                        val sql = args[0] as String
                        queries += sql
                        val values = rows(sql)
                        var row = -1
                        proxy<RemoteResultSet> { operation, parameters ->
                            when (operation) {
                                "next" -> ++row < values.size
                                "getString" -> values[row][(parameters[0] as Int) - 1]?.toString()
                                "getInt" -> (values[row][(parameters[0] as Int) - 1] as? Number)?.toInt() ?: 0
                                "close" -> { resultCloses++; null }
                                else -> error(operation)
                            }
                        }
                    }
                    "close" -> { statementCloses++; null }
                    else -> error(method)
                }
            } else when (name) {
                "jpdaFacade" -> facade
                "getMetaData" -> metadata
                "getCatalog" -> "sales"
                "setCatalog" -> throw SQLException("Unknown database (JDBC catalog must not be used for StarRocks catalogs)")
                else -> error("Unexpected remote connection call: $name")
            }
        }
        val source = LocalDataSource.create("StarRocks test", "", "", "")
        source.info.setDbms(StarRocksDbms.INSTANCE)
        if (catalogIsSchema != null) {
            assertTrue(source.info.deserializeDatabaseInfo(proxy<HierarchicalStreamReader> { name, _ ->
                when (name) {
                    "getNodeName" -> "jdbc-catalog-is-schema"
                    "getValue" -> catalogIsSchema.toString()
                    else -> error(name)
                }
            }))
            assertEquals(catalogIsSchema, source.info.isJdbcCatalogIsSchema)
        }
        val point = proxy<DatabaseConnectionPoint> { name, _ -> if (name == "getDataSource") source else null }
        lateinit var audit: AdvancedAuditService
        val connection = proxy<DatabaseConnectionCore> { name, _ ->
            when (name) {
                "getRemoteConnection" -> remote
                "getRemoteMetaData" -> metadata
                "getAuditService" -> audit
                "getConnectionPoint" -> point
                "getDbms" -> StarRocksDbms.INSTANCE
                "getVersion" -> Version.of(4, 1)
                "getDriverVersion" -> Version.of(9, 4, 0)
                else -> null
            }
        }
        audit = AdvancedAuditService(connection, proxy<ConnectionRequestor> { _, _ -> proxy<AuditService> { _, _ -> null } }, project)
        return DatabaseMetaDataWrapper.create(connection, metadata).also { assertTrue(it is StarRocksMetadataWrapper) }
    }

    private inline fun <reified T> proxy(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { instance, method, args ->
            when (method.name) {
                "hashCode" -> System.identityHashCode(instance)
                "equals" -> instance === args?.firstOrNull()
                "toString" -> "Test ${T::class.java.simpleName}"
                else -> call(method.name, args ?: emptyArray())
            }
        } as T
}

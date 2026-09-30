package com.github.ycyz.starrocks.datagrip.database

import com.intellij.database.model.ObjectKind
import com.intellij.database.model.ModelFactory
import com.intellij.database.dialects.base.BaseDmlHelper
import com.intellij.database.script.generator.dml.DmlHelper
import com.intellij.database.dialects.generic.model.GenericModel
import com.intellij.database.dialects.generic.model.GenericDatabase
import com.intellij.database.dialects.generic.model.GenericSchema
import com.intellij.database.util.DdlBuilder
import com.intellij.database.util.ObjectPath
import com.intellij.database.util.SearchPath
import com.intellij.database.util.DbImplUtilCore
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class StarRocksDatabaseDialectTest : BasePlatformTestCase() {
    fun testStarRocksDatabaseDialectIsRegistered() {
        assertTrue(
            DbImplUtilCore.getDatabaseDialect(StarRocksDbms.INSTANCE) is StarRocksDatabaseDialect
        )
    }

    fun testConnectionNamespaceLoadsWithoutSelectedEditorSchema() {
        val queries = mutableListOf<String>()
        val searchPath = StarRocksDatabaseDialect.loadSearchPath { sql ->
            queries += sql
            when (sql) {
                StarRocksDatabaseDialect.CURRENT_CATALOG_QUERY -> "default_catalog"
                StarRocksDatabaseDialect.CURRENT_DATABASE_QUERY -> "dwd"
                else -> error("Unexpected query: $sql")
            }
        }

        assertEquals(
            listOf(
                StarRocksDatabaseDialect.CURRENT_CATALOG_QUERY,
                StarRocksDatabaseDialect.CURRENT_DATABASE_QUERY,
            ),
            queries,
        )
        assertNotNull(searchPath)
        assertEquals(ObjectKind.SCHEMA, searchPath!!.current.kind)
        assertEquals("dwd", searchPath.current.name)
        assertEquals(ObjectKind.DATABASE, searchPath.current.parent.kind)
        assertEquals("default_catalog", searchPath.current.parent.name)
    }

    fun testConnectionNamespaceKeepsCatalogWhenDatabaseIsUnset() {
        val searchPath = StarRocksDatabaseDialect.loadSearchPath { sql ->
            when (sql) {
                StarRocksDatabaseDialect.CURRENT_CATALOG_QUERY -> "default_catalog"
                StarRocksDatabaseDialect.CURRENT_DATABASE_QUERY -> null
                else -> error("Unexpected query: $sql")
            }
        }

        assertNotNull(searchPath)
        assertEquals(ObjectKind.DATABASE, searchPath!!.current.kind)
        assertEquals("default_catalog", searchPath.current.name)
    }

    fun testUnrelatedGenericDialectBehaviorIsPreserved() {
        val dialect = StarRocksDatabaseDialect()

        assertEquals("Generic SQL", dialect.displayName)
        assertFalse(dialect.supportsCommonTableExpression())
    }

    fun testTableReferencesKeepCatalogAcrossRepeatedQueries() {
        val model = ModelFactory.BLACK_HOLE.createModel(StarRocksDbms.INSTANCE, GenericModel::class.java)
        for (catalogName in listOf("external_one", "external_two", "default_catalog", "external_one")) {
            val catalog = model.root.databases.createOrGet(catalogName) as GenericDatabase
            val schema = catalog.schemas.createOrGet("work_temp") as GenericSchema
            val table = schema.tables.createOrGet("issue103_cdc_guard_20260908")
            val builder = DdlBuilder().withDialect(StarRocksDatabaseDialect()).qualifyReferences(true)
            assertEquals(
                "`$catalogName`.`work_temp`.`issue103_cdc_guard_20260908`",
                builder.qualifiedRef(table).statement,
            )
        }
    }

    fun testQualifiedReferencesEscapeNamesAndKeepEqualCatalogAndDatabaseNames() {
        val model = ModelFactory.BLACK_HOLE.createModel(StarRocksDbms.INSTANCE, GenericModel::class.java)
        val catalog = model.root.databases.createOrGet("same`name") as GenericDatabase
        val schema = catalog.schemas.createOrGet("same`name") as GenericSchema
        val table = schema.tables.createOrGet("table.name")
        val builder = DdlBuilder().withDialect(StarRocksDatabaseDialect()).qualifyReferences(true)
        assertEquals("`same``name`.`same``name`.`table.name`", builder.qualifiedRef(table).statement)
    }

    fun testPlatformFromAndPaginationKeepExternalCatalogWithDifferentCurrentCatalog() {
        val model = ModelFactory.BLACK_HOLE.createModel(StarRocksDbms.INSTANCE, GenericModel::class.java)
        val catalog = model.root.databases.createOrGet("external_catalog") as GenericDatabase
        val schema = catalog.schemas.createOrGet("work_temp") as GenericSchema
        val table = schema.tables.createOrGet("issue103_cdc_guard_20260908")
        val dialect = DbImplUtilCore.getDatabaseDialect(StarRocksDbms.INSTANCE) as StarRocksDatabaseDialect
        val helper = DmlHelper.EP.forDbms(StarRocksDbms.INSTANCE) as BaseDmlHelper
        for (offset in listOf(0, 100, 0)) {
            val builder = DdlBuilder().withDialect(dialect).qualifyReferences(true)
                .pushDefaultSchema(ObjectPath.create("default_catalog", ObjectKind.DATABASE).append("work_temp", ObjectKind.SCHEMA))
                .plain("SELECT * ")
            helper.appendFromTable(builder, table, "t", null)
            helper.appendLimit(table, builder, dialect, 100, offset, false, false)
            val sql = builder.statement
            assertTrue(sql, sql.contains("FROM `external_catalog`.`work_temp`.`issue103_cdc_guard_20260908`"))
            // GenericDmlHelper may apply paging through JDBC rather than SQL.
            // The table target must stay qualified in either case.
            assertFalse(sql, sql.contains("`default_catalog`"))
        }
    }

    fun testNamespaceSwitchUsesSingleNativeStatement() {
        val dialect = StarRocksDatabaseDialect()
        assertTrue(dialect.supportsSearchPath())
        assertTrue(dialect.supportsSchemaChanging())
        assertEquals(ObjectKind.SCHEMA, dialect.searchPathObjectKind)
        assertTrue(dialect.shouldSwitchThroughJdbc(ObjectKind.SCHEMA))
        assertTrue(dialect.shouldSwitchThroughJdbc(ObjectKind.DATABASE))
        val catalog = ObjectPath.create("external`catalog", ObjectKind.DATABASE)
        assertEquals("SET CATALOG `external``catalog`", dialect.sqlSetSearchPath(SearchPath.of(catalog)))
        assertNull(dialect.sqlSetSearchPath(SearchPath.of(catalog.append("work.temp", ObjectKind.SCHEMA))))
        assertNull(dialect.sqlSetSearchPath(SearchPath.of(ObjectPath.create("work_temp", ObjectKind.SCHEMA))))
        assertNull(dialect.sqlSetSearchPath(SearchPath.of(ObjectPath.create("", ObjectKind.SCHEMA))))
        assertEquals("SET CATALOG `default_catalog`", dialect.sqlResetSearchPath())
    }

    fun testSwitchBackToInternalDatabaseDoesNotGenerateMultipleStatements() {
        val path = ObjectPath.create("default_catalog", ObjectKind.DATABASE).append("ads", ObjectKind.SCHEMA)
        assertNull(StarRocksDatabaseDialect().sqlSetSearchPath(SearchPath.of(path)))
    }

    fun testSearchPathReadsCatalogSessionVariableInsteadOfCatalogFunction() {
        val path = StarRocksDatabaseDialect.loadSearchPath { sql ->
            when (sql) {
                "SELECT @@catalog" -> "paimon_catalog_fs"
                "SELECT DATABASE()", "SELECT CATALOG()" -> "work_temp"
                else -> error(sql)
            }
        }!!
        assertEquals("paimon_catalog_fs", path.current.parent.name)
        assertEquals("work_temp", path.current.name)
    }

    fun testMissingCatalogDoesNotInventAPath() {
        val queries = mutableListOf<String>()
        val searchPath = StarRocksDatabaseDialect.loadSearchPath { sql ->
            queries += sql
            null
        }

        assertNull(searchPath)
        assertEquals(listOf(StarRocksDatabaseDialect.CURRENT_CATALOG_QUERY), queries)
    }
}

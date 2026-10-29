package com.github.ycyz.starrocks.datagrip.database

import com.intellij.database.model.ObjectKind
import com.intellij.database.model.ModelFactory
import com.intellij.database.DbmsExtension
import com.intellij.database.dialects.SqlAffectAnalyzer
import com.intellij.database.dialects.base.BaseDmlHelper
import com.intellij.database.script.generator.dml.DmlHelper
import com.intellij.database.dialects.generic.model.GenericModel
import com.intellij.database.dialects.generic.model.GenericDatabase
import com.intellij.database.dialects.generic.model.GenericSchema
import com.intellij.database.util.DdlBuilder
import com.intellij.database.util.ObjectPath
import com.intellij.database.util.SearchPath
import com.intellij.database.util.Casing
import com.intellij.database.util.DbImplUtilCore
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.sql.intentions.SqlExpandColumnListIntention
import com.intellij.sql.psi.SqlCreateTableStatement
import com.intellij.sql.psi.SqlReferenceExpression
import com.intellij.sql.psi.SqlTableType

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

    fun testTemporaryTableCreationDoesNotTriggerMetadataIntrospection() {
        val dialect = StarRocksDatabaseDialect()
        val temporaryFile = PsiFileFactory.getInstance(project).createFileFromText(
            "temporary-table.sql",
            StarRocksDialect.INSTANCE,
            "CREATE TEMPORARY TABLE scratch (id BIGINT);",
        )
        val temporaryTable = PsiTreeUtil.findChildOfType(temporaryFile, SqlCreateTableStatement::class.java)
        assertNotNull(temporaryTable)
        assertTrue(temporaryTable!!.isTemporary)
        assertFalse(dialect.triggersIntrospection(temporaryTable))

        val regularFile = PsiFileFactory.getInstance(project).createFileFromText(
            "regular-table.sql",
            StarRocksDialect.INSTANCE,
            "CREATE TABLE persistent (id BIGINT);",
        )
        val regularTable = PsiTreeUtil.findChildOfType(regularFile, SqlCreateTableStatement::class.java)
        assertNotNull(regularTable)
        assertFalse(regularTable!!.isTemporary)
        assertTrue(dialect.triggersIntrospection(regularTable))
    }

    fun testAffectAnalyzerCreatesTableLevelRefreshMention() {
        val registered = DbmsExtension<SqlAffectAnalyzer>("com.intellij.database.sqlEffectAnalyzer")
            .forDbms(StarRocksDbms.INSTANCE)
        assertTrue(registered is StarRocksAffectAnalyzer)

        val source = LocalDataSource.temporary().also {
            it.info.setDbms(StarRocksDbms.INSTANCE)
        }
        val file = PsiFileFactory.getInstance(project).createFileFromText(
            "affect-table.sql",
            StarRocksDialect.INSTANCE,
            "CREATE TABLE dwd.orders (id BIGINT);",
        )
        val statement = PsiTreeUtil.findChildOfType(file, SqlCreateTableStatement::class.java)
        assertNotNull(statement)
        val searchPath = SearchPath.of(
            ObjectPath.create("default_catalog", ObjectKind.DATABASE)
                .append("dwd", ObjectKind.SCHEMA),
        )

        val impactees = StarRocksAffectAnalyzer().determineAffectedObjects(source, listOf(statement!!), searchPath)
        assertEquals(1, impactees.mentions.size)
        val mention = impactees.mentions.single()
        assertEquals("default_catalog", mention.databaseName.name)
        assertEquals("dwd", mention.schemaName.name)
        assertEquals("orders", mention.objectNewName.name)
        assertEquals(ObjectKind.TABLE, mention.objectKind)
    }

    fun testAffectAnalyzerIgnoresTemporaryTableCreation() {
        val source = LocalDataSource.temporary().also {
            it.info.setDbms(StarRocksDbms.INSTANCE)
        }
        val file = PsiFileFactory.getInstance(project).createFileFromText(
            "affect-temporary-table.sql",
            StarRocksDialect.INSTANCE,
            "CREATE TEMPORARY TABLE scratch (id BIGINT);",
        )
        val statement = PsiTreeUtil.findChildOfType(file, SqlCreateTableStatement::class.java)
        assertNotNull(statement)

        val impactees = StarRocksAffectAnalyzer().determineAffectedObjects(
            source,
            listOf(statement!!),
            SearchPath.of(ObjectPath.create("default_catalog", ObjectKind.DATABASE)
                .append("dwd", ObjectKind.SCHEMA)),
        )
        assertFalse(impactees.hasObjects)
    }

    fun testAffectAnalyzerKeepsExplicitStarRocksCatalog() {
        val source = LocalDataSource.temporary().also {
            it.info.setDbms(StarRocksDbms.INSTANCE)
        }
        val file = PsiFileFactory.getInstance(project).createFileFromText(
            "affect-qualified-table.sql",
            StarRocksDialect.INSTANCE,
            "CREATE TABLE external_catalog.dwd.orders (id BIGINT);",
        )
        val statement = PsiTreeUtil.findChildOfType(file, SqlCreateTableStatement::class.java)
        assertNotNull(statement)

        val impactees = StarRocksAffectAnalyzer().determineAffectedObjects(
            source,
            listOf(statement!!),
            SearchPath.of(ObjectPath.create("default_catalog", ObjectKind.DATABASE)
                .append("dwd", ObjectKind.SCHEMA)),
        )
        val mention = impactees.mentions.single()
        assertEquals("external_catalog", mention.databaseName.name)
        assertEquals("dwd", mention.schemaName.name)
        assertEquals("orders", mention.objectNewName.name)
    }

    fun testColumnScriptRespectsQuoteSuppression() {
        val dialect = StarRocksDatabaseDialect()

        assertEquals(
            "item_code",
            dialect.catToScript("item_code", ObjectKind.COLUMN, false, false, Casing.EXACT),
        )
        assertEquals(
            "`item_code`",
            dialect.catToScript("item_code", ObjectKind.COLUMN, true, false, Casing.EXACT),
        )
    }

    fun testExpandSubqueryColumnsOnlyQuotesNamesThatNeedIt() {
        val dialect = StarRocksDialect.INSTANCE
        val file = PsiFileFactory.getInstance(project).createFileFromText(
            "expand-columns.sql", dialect,
            "SELECT * FROM (SELECT 1 AS item_code, 2 AS `order`, 3 AS `item.name`, 4 AS `a``b`) s;",
        )
        val asterisk = PsiTreeUtil.findChildrenOfType(file, SqlReferenceExpression::class.java)
            .single { it.name == "*" }
        val type = asterisk.dasType as SqlTableType
        val expanded = SqlExpandColumnListIntention.getAllColumnsList(project, asterisk, type, dialect)

        assertEquals("item_code, `order`, `item.name`, `a``b`", expanded)
    }

    fun testTableReferencesKeepCatalogAcrossRepeatedQueries() {
        val model = ModelFactory.BLACK_HOLE.createModel(StarRocksDbms.INSTANCE, GenericModel::class.java)
        for (catalogName in listOf("external_one", "external_two", "default_catalog", "external_one")) {
            val catalog = model.root.databases.createOrGet(catalogName) as GenericDatabase
            val schema = catalog.schemas.createOrGet("work_temp") as GenericSchema
            val table = schema.tables.createOrGet("issue103_cdc_guard_20260908")
            val builder = DdlBuilder().withDialect(StarRocksDatabaseDialect()).qualifyReferences(true)
            assertEquals(
                "$catalogName.work_temp.issue103_cdc_guard_20260908",
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
            assertTrue(sql, sql.contains("FROM external_catalog.work_temp.issue103_cdc_guard_20260908"))
            // GenericDmlHelper may apply paging through JDBC rather than SQL.
            // The table target must stay qualified in either case.
            assertFalse(sql, sql.contains("default_catalog"))
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

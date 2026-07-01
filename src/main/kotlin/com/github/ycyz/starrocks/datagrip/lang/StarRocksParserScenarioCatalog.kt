package com.github.ycyz.starrocks.datagrip.lang

object StarRocksParserScenarioCatalog {
    val SCENARIOS: List<StarRocksParserScenario> = listOf(
        StarRocksParserScenario(
            name = "Core query syntax",
            fileName = "query/core-query.sql",
            milestone = StarRocksGrammarMilestone.QUERY,
            requiredFeatures = setOf(
                StarRocksFeature.CAST_TYPE_SYNTAX,
                StarRocksFeature.QUALIFY_CLAUSE,
                StarRocksFeature.WINDOW_FUNCTION,
                StarRocksFeature.GROUPING_SETS
            )
        ),
        StarRocksParserScenario(
            name = "Set operations and named windows",
            fileName = "query/set-window-query.sql",
            milestone = StarRocksGrammarMilestone.QUERY,
            requiredFeatures = setOf(
                StarRocksFeature.SET_OPERATION,
                StarRocksFeature.NAMED_WINDOW,
                StarRocksFeature.WINDOW_FUNCTION
            )
        ),
        StarRocksParserScenario(
            name = "Nested query expressions",
            fileName = "query/nested-query.sql",
            milestone = StarRocksGrammarMilestone.EXPRESSIONS,
            requiredFeatures = setOf(
                StarRocksFeature.NESTED_QUERY
            )
        ),
        StarRocksParserScenario(
            name = "Create table DDL",
            fileName = "ddl/create-table.sql",
            milestone = StarRocksGrammarMilestone.TABLE_DDL,
            requiredFeatures = setOf(
                StarRocksFeature.STARROCKS_CREATE_TABLE,
                StarRocksFeature.CREATE_TABLE_AS_SELECT,
                StarRocksFeature.STARROCKS_PROPERTIES
            )
        ),
        StarRocksParserScenario(
            name = "Insert with local DDL reference",
            fileName = "dml/insert-local-ddl.sql",
            milestone = StarRocksGrammarMilestone.DML,
            requiredFeatures = setOf(
                StarRocksFeature.STARROCKS_CREATE_TABLE,
                StarRocksFeature.LOCAL_DDL_REFERENCE
            )
        ),
        StarRocksParserScenario(
            name = "Materialized view with AS SELECT",
            fileName = "ddl/materialized-view.sql",
            milestone = StarRocksGrammarMilestone.MATERIALIZED_VIEW,
            requiredFeatures = setOf(
                StarRocksFeature.MATERIALIZED_VIEW,
                StarRocksFeature.STARROCKS_PROPERTIES,
                StarRocksFeature.AS_SELECT
            )
        ),
        StarRocksParserScenario(
            name = "View with AS SELECT",
            fileName = "ddl/view.sql",
            milestone = StarRocksGrammarMilestone.TABLE_DDL,
            requiredFeatures = setOf(
                StarRocksFeature.VIEW_STATEMENT,
                StarRocksFeature.AS_SELECT,
                StarRocksFeature.LOCAL_DDL_REFERENCE
            )
        ),
        StarRocksParserScenario(
            name = "Type syntax",
            fileName = "types/complex-types.sql",
            milestone = StarRocksGrammarMilestone.TYPES,
            requiredFeatures = setOf(
                StarRocksFeature.CAST_TYPE_SYNTAX,
                StarRocksFeature.COMPLEX_TYPES
            )
        ),
        StarRocksParserScenario(
            name = "Official function families",
            fileName = "functions/function-families.sql",
            milestone = StarRocksGrammarMilestone.FUNCTIONS,
            requiredFeatures = setOf(
                StarRocksFeature.OFFICIAL_FUNCTIONS
            )
        ),
        StarRocksParserScenario(
            name = "Catalog and resource statements",
            fileName = "admin/catalog-resource.sql",
            milestone = StarRocksGrammarMilestone.CATALOG_RESOURCE,
            requiredFeatures = setOf(
                StarRocksFeature.CATALOG_STATEMENT,
                StarRocksFeature.RESOURCE_STATEMENT,
                StarRocksFeature.STARROCKS_PROPERTIES,
                StarRocksFeature.STARROCKS_STATEMENT_CLASSIFIER
            )
        ),
        StarRocksParserScenario(
            name = "Load and task statements",
            fileName = "admin/load-task.sql",
            milestone = StarRocksGrammarMilestone.LOAD_EXPORT,
            requiredFeatures = setOf(
                StarRocksFeature.LOAD_STATEMENT,
                StarRocksFeature.TASK_STATEMENT,
                StarRocksFeature.STARROCKS_STATEMENT_CLASSIFIER
            )
        ),
        StarRocksParserScenario(
            name = "Administration statements",
            fileName = "admin/show-admin.sql",
            milestone = StarRocksGrammarMilestone.ADMINISTRATION,
            requiredFeatures = setOf(
                StarRocksFeature.ADMIN_STATEMENT,
                StarRocksFeature.STARROCKS_STATEMENT_CLASSIFIER
            )
        ),
        StarRocksParserScenario(
            name = "DML statements",
            fileName = "dml/mutations.sql",
            milestone = StarRocksGrammarMilestone.DML,
            requiredFeatures = setOf(
                StarRocksFeature.INSERT_OVERWRITE,
                StarRocksFeature.UPDATE_STATEMENT,
                StarRocksFeature.DELETE_STATEMENT,
                StarRocksFeature.MERGE_STATEMENT
            )
        ),
        StarRocksParserScenario(
            name = "Export and analyze statements",
            fileName = "admin/export-analyze.sql",
            milestone = StarRocksGrammarMilestone.TASK_ANALYZE,
            requiredFeatures = setOf(
                StarRocksFeature.EXPORT_STATEMENT,
                StarRocksFeature.ANALYZE_STATEMENT
            )
        ),
        StarRocksParserScenario(
            name = "Backup and restore statements",
            fileName = "admin/backup-restore.sql",
            milestone = StarRocksGrammarMilestone.BACKUP_RESTORE,
            requiredFeatures = setOf(
                StarRocksFeature.BACKUP_RESTORE_STATEMENT,
                StarRocksFeature.STARROCKS_STATEMENT_CLASSIFIER
            )
        )
    )
}

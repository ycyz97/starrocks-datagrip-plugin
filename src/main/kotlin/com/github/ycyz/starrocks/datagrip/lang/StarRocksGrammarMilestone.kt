package com.github.ycyz.starrocks.datagrip.lang

/**
 * Rewrite milestones for the native StarRocks parser.
 *
 * These entries are intentionally not wired into parsing yet. They define the
 * order in which the replacement grammar should become executable.
 */
enum class StarRocksGrammarMilestone {
    QUERY,
    EXPRESSIONS,
    TYPES,
    DML,
    TABLE_DDL,
    MATERIALIZED_VIEW,
    CATALOG_RESOURCE,
    LOAD_EXPORT,
    TASK_ANALYZE,
    BACKUP_RESTORE,
    ADMINISTRATION,
    FUNCTIONS,
    LOCAL_RESOLUTION,
    FORMATTING,
    COMPLETION,

    @Deprecated("Use QUERY.")
    CORE_QUERY,
    @Deprecated("Use QUERY.")
    QUALIFY_AND_WINDOWS,
    @Deprecated("Use QUERY.")
    TABLE_FUNCTIONS,
    @Deprecated("Use TABLE_DDL.")
    CREATE_TABLE_DDL,
    @Deprecated("Use MATERIALIZED_VIEW.")
    MATERIALIZED_VIEW_DDL,
    @Deprecated("Use LOCAL_RESOLUTION.")
    LOCAL_DDL_RESOLUTION
}

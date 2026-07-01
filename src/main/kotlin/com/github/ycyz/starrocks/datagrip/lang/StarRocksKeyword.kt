package com.github.ycyz.starrocks.datagrip.lang

/**
 * StarRocks-specific keywords that must be represented by the native grammar.
 */
enum class StarRocksKeyword(val text: String, val milestone: StarRocksGrammarMilestone) {
    QUALIFY("QUALIFY", StarRocksGrammarMilestone.QUERY),
    UNNEST("UNNEST", StarRocksGrammarMilestone.QUERY),
    LATERAL("LATERAL", StarRocksGrammarMilestone.QUERY),
    MATERIALIZED("MATERIALIZED", StarRocksGrammarMilestone.MATERIALIZED_VIEW),
    DISTRIBUTED("DISTRIBUTED", StarRocksGrammarMilestone.TABLE_DDL),
    PROPERTIES("PROPERTIES", StarRocksGrammarMilestone.TABLE_DDL),
    DUPLICATE("DUPLICATE", StarRocksGrammarMilestone.TABLE_DDL),
    AGGREGATE("AGGREGATE", StarRocksGrammarMilestone.TABLE_DDL),
    LARGEINT("LARGEINT", StarRocksGrammarMilestone.TYPES),
    BITMAP("BITMAP", StarRocksGrammarMilestone.TYPES),
    HLL("HLL", StarRocksGrammarMilestone.TYPES),
    JSON("JSON", StarRocksGrammarMilestone.TYPES),
    STRUCT("STRUCT", StarRocksGrammarMilestone.TYPES),
    MAP("MAP", StarRocksGrammarMilestone.TYPES),
    ARRAY("ARRAY", StarRocksGrammarMilestone.TYPES);
}

package com.github.ycyz.starrocks.datagrip.lang

data class StarRocksParserScenario(
    val name: String,
    val fileName: String,
    val milestone: StarRocksGrammarMilestone,
    val requiredFeatures: Set<StarRocksFeature>
)

package com.github.ycyz.starrocks.datagrip.dialect

interface StarRocksDialectRules {
    fun isAdditionalKeyword(tokenText: String): Boolean
    fun isStarRocksSyntaxKeyword(tokenText: String): Boolean
    fun isFunctionName(tokenText: String): Boolean
    fun isFunctionLikeButNotCall(tokenText: String): Boolean
}

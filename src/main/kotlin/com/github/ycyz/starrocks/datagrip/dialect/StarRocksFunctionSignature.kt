package com.github.ycyz.starrocks.datagrip.dialect

data class StarRocksFunctionSignature(
    val name: String,
    val category: StarRocksFunctionCategory,
    val signature: String,
    val description: String
) {
    val lookupName: String = name.uppercase()
}

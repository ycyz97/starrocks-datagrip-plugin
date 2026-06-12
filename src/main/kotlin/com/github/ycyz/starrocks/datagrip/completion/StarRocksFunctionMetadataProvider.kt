package com.github.ycyz.starrocks.datagrip.completion

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksFunctionRegistry

object StarRocksFunctionMetadataProvider {
    fun functions(): List<FunctionMetadata> = StarRocksFunctionRegistry.functions()

    fun functionNames(): Set<String> = StarRocksFunctionRegistry.names()
}

data class FunctionMetadata(
    val name: String,
    val category: String,
    val signature: String? = null,
    val description: String? = null,
)

package com.github.ycyz.starrocks.datagrip.database

import com.intellij.database.dataSource.DatabaseConnectionCore
import com.intellij.database.dialects.AbstractDefinitionProvider
import com.intellij.database.model.DasObject
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.DasUtil
import com.intellij.util.PairConsumer

class StarRocksDefinitionProvider : AbstractDefinitionProvider() {
    override fun isSupported(obj: DasObject): Boolean {
        return obj.kind in SUPPORTED_KINDS
    }

    override fun fetchSources(
        objects: Iterable<DasObject>,
        connection: DatabaseConnectionCore,
        consumer: PairConsumer<DasObject, Any>
    ) {
        val remoteConnection = connection.remoteConnection
        objects.filter(::isSupported).forEach { obj ->
            val statementText = StarRocksDdlStatements.showCreateStatement(obj.kind, qualifiedName(obj))
            val statement = remoteConnection.createStatement()
            try {
                val resultSet = statement.executeQuery(statementText)
                try {
                    if (resultSet.next()) {
                        consumer.consume(obj, resultSet.getString(2) ?: resultSet.getString(1))
                    }
                } finally {
                    resultSet.close()
                }
            } finally {
                statement.close()
            }
        }
    }

    private fun qualifiedName(obj: DasObject): String {
        val schema = DasUtil.getSchema(obj).takeIf { it.isNotBlank() }
        val catalog = DasUtil.getCatalog(obj).takeIf { it.isNotBlank() && it != schema }
        return listOfNotNull(catalog, schema, obj.name)
            .joinToString(".") { quoteIdentifier(it) }
    }

    companion object {
        private val SUPPORTED_KINDS: Set<ObjectKind> = setOf(
            ObjectKind.TABLE,
            ObjectKind.VIEW,
            ObjectKind.MAT_VIEW
        )

        fun quoteIdentifier(identifier: String): String {
            if (identifier.startsWith("`") && identifier.endsWith("`")) {
                return identifier
            }
            return "`" + identifier.replace("`", "``") + "`"
        }
    }
}

object StarRocksDdlStatements {
    fun showCreateStatement(kind: ObjectKind, qualifiedName: String): String {
        return when (kind) {
            ObjectKind.MAT_VIEW -> "SHOW CREATE MATERIALIZED VIEW $qualifiedName"
            ObjectKind.VIEW -> "SHOW CREATE VIEW $qualifiedName"
            else -> "SHOW CREATE TABLE $qualifiedName"
        }
    }
}

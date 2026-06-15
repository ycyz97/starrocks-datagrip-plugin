package com.github.ycyz.starrocks.datagrip.ddl

import com.intellij.database.dataSource.DatabaseConnectionCore
import com.intellij.database.dialects.AbstractDefinitionProvider
import com.intellij.database.model.DasObject
import com.intellij.database.model.ObjectKind
import com.intellij.util.PairConsumer

class StarRocksDefinitionProvider : AbstractDefinitionProvider() {
    override fun isSupported(obj: DasObject): Boolean = statementKind(obj) != null

    override fun fetchSources(
        objects: Iterable<DasObject>,
        connection: DatabaseConnectionCore,
        sink: PairConsumer<DasObject, Any>
    ) {
        for (obj in objects) {
            val kind = statementKind(obj) ?: continue
            try {
                sink.consume(obj, loadCreateStatement(connection, kind, obj))
            } catch (e: Exception) {
                sink.consume(obj, e)
            }
        }
    }

    private fun loadCreateStatement(
        connection: DatabaseConnectionCore,
        kind: ShowCreateKind,
        obj: DasObject
    ): String {
        val sql = "${kind.sqlPrefix} ${qualifiedName(obj)}"
        val statement = connection.remoteConnection.createStatement()
        try {
            val resultSet = statement.executeQuery(sql)
            try {
                if (!resultSet.next()) return ""
                val columnCount = resultSet.metaData.columnCount
                return resultSet.getString(columnCount) ?: ""
            } finally {
                resultSet.close()
            }
        } finally {
            statement.close()
        }
    }

    private fun qualifiedName(obj: DasObject): String {
        val schema = findNamespaceName(obj)
        val objectName = quoteIdentifier(obj.name)
        return if (schema == null) objectName else "${quoteIdentifier(schema)}.$objectName"
    }

    private fun findNamespaceName(obj: DasObject): String? {
        var parent = obj.dasParent
        while (parent != null) {
            if (parent.kind == ObjectKind.SCHEMA || parent.kind == ObjectKind.DATABASE) {
                return parent.name
            }
            parent = parent.dasParent
        }
        return null
    }

    private fun quoteIdentifier(identifier: String): String =
        "`" + identifier.replace("`", "``") + "`"

    private fun statementKind(obj: DasObject): ShowCreateKind? =
        when (obj.kind) {
            ObjectKind.TABLE -> ShowCreateKind.TABLE
            ObjectKind.VIEW -> ShowCreateKind.VIEW
            ObjectKind.MAT_VIEW -> ShowCreateKind.MATERIALIZED_VIEW
            else -> null
        }

    private enum class ShowCreateKind(val sqlPrefix: String) {
        TABLE("SHOW CREATE TABLE"),
        VIEW("SHOW CREATE VIEW"),
        MATERIALIZED_VIEW("SHOW CREATE MATERIALIZED VIEW")
    }
}

package com.github.ycyz.starrocks.datagrip.database

import com.intellij.database.Dbms
import com.intellij.database.dialects.generic.naming.GenericNamingService
import com.intellij.database.model.DasObject
import com.intellij.database.model.ObjectKind
import com.intellij.database.model.basic.BasicDatabase
import com.intellij.database.model.basic.BasicModel
import com.intellij.database.script.generator.NamingService
import com.intellij.database.util.Case
import com.intellij.database.util.Casing

/** Column matching is independent of the JDBC model's default identifier casing. */
class StarRocksNamingService(private val delegate: NamingService) : NamingService by delegate {
    override fun getCasing(kind: ObjectKind, context: DasObject?): Casing {
        return if (kind == ObjectKind.COLUMN || context is com.intellij.sql.psi.SqlAsExpression ||
            context is com.intellij.sql.psi.SqlColumnAliasDefinition) COLUMN_CASING
        else delegate.getCasing(kind, context)
    }

    class Provider(dbms: Dbms) : NamingService.Provider {
        private val delegate = GenericNamingService.Provider(dbms)

        override fun getNamingService(model: BasicModel?, database: BasicDatabase?): NamingService =
            StarRocksNamingService(delegate.getNamingService(model, database))
    }

    companion object {
        private val COLUMN_CASING = Casing.create(Case.MIXED, Case.MIXED)
    }
}

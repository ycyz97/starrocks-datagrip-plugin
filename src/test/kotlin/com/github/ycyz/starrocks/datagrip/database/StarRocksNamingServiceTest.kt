package com.github.ycyz.starrocks.datagrip.database

import com.intellij.database.dialects.generic.model.GenericModel
import com.intellij.database.dialects.generic.naming.GenericNamingService
import com.intellij.database.model.ModelFactory
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Casing
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect

class StarRocksNamingServiceTest : BasePlatformTestCase() {
    fun testColumnPolicyIsIndependentOfModelCasingAndPreservesOtherKinds() {
        for (casing in listOf(Casing.EXACT, Casing.create(com.intellij.database.util.Case.LOWER, com.intellij.database.util.Case.EXACT))) {
            val model = ModelFactory.BLACK_HOLE.createModel(StarRocksDbms.INSTANCE, GenericModel::class.java)
            model.root.defaultCasing = casing
            val service = StarRocksNamingService.Provider(StarRocksDbms.INSTANCE).getNamingService(model, null)
            val generic = GenericNamingService.Provider(StarRocksDbms.INSTANCE).getNamingService(model, null)
            val columns = service.getCasing(ObjectKind.COLUMN, null)
            assertFalse(columns.plain.sensitive)
            assertFalse(columns.quoted.sensitive)
            assertEquals("OrderID", columns.plain.apply("OrderID"))
            assertEquals("OrderID", columns.quoted.apply("OrderID"))
            for (kind in listOf(ObjectKind.DATABASE, ObjectKind.SCHEMA, ObjectKind.TABLE, ObjectKind.VIEW)) {
                assertEquals(generic.getCasing(kind, null), service.getCasing(kind, null))
            }
        }
    }

    fun testRegisteredDialectUsesColumnPolicy() {
        val casing = StarRocksDialect.INSTANCE.getCasing(ObjectKind.COLUMN, null)
        assertFalse(casing.plain.sensitive)
        assertFalse(casing.quoted.sensitive)
    }
}

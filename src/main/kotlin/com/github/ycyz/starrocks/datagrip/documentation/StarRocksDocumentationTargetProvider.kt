package com.github.ycyz.starrocks.datagrip.documentation

import com.github.ycyz.starrocks.datagrip.database.StarRocksDbms
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.database.access.ConnectionProvider
import com.intellij.database.dataSource.connection.DGDepartment
import com.intellij.database.model.ObjectKind
import com.intellij.database.psi.DbTableImpl
import com.intellij.database.psi.documentation.DbTableDocumentationTarget
import com.intellij.database.util.DatabaseDefinitionHelper
import com.intellij.database.util.DasUtil
import com.intellij.database.util.DbSqlUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import java.util.concurrent.CancellationException
import java.util.function.Function
import java.util.function.Supplier

/** Native DDL for hover/quick documentation without changing the data source model. */
class StarRocksDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val table = element as? DbTableImpl ?: return null
        if (table.dataSource.dbms != StarRocksDbms.INSTANCE ||
            table.kind !in setOf(ObjectKind.TABLE, ObjectKind.VIEW, ObjectKind.MAT_VIEW)) return null
        return StarRocksTableDocumentationTarget(table)
    }
}

internal class StarRocksTableDocumentationTarget(
    table: DbTableImpl,
    private val loadDefinition: (DbTableImpl) -> String = ::loadNativeDefinition
) : DbTableDocumentationTarget(table) {
    override fun getFactory(): Function<DbTableImpl, DocumentationTarget> =
        Function { StarRocksTableDocumentationTarget(it, loadDefinition) }

    override fun computeDocumentation(): DocumentationResult {
        // The platform invokes this under a read action. Capture the fallback and
        // a pointer now, but never acquire a connection or run JDBC under that lock.
        val fallback = documentation
        val pointer = SmartPointerManager.createPointer(dbElement)
        return DocumentationResult.asyncDocumentation(Supplier {
            ProgressManager.checkCanceled()
            val table = ReadAction.computeBlocking<DbTableImpl?, RuntimeException> {
                pointer.element?.takeIf { it.isValid && !it.project.isDisposed }
            } ?: return@Supplier null
            val definition = try {
                loadDefinition(table)
            } catch (cancelled: ProcessCanceledException) {
                throw cancelled
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                LOG.debug("Unable to load native StarRocks DDL for quick documentation", error)
                null
            }
            ProgressManager.checkCanceled()
            ReadAction.computeBlocking<DocumentationResult.Documentation?, RuntimeException> {
                if (!table.isValid || table.project.isDisposed) null
                else DocumentationResult.documentation(
                    if (definition.isNullOrBlank()) fallback else renderDefinition(table, definition)
                )
            }
        })
    }

    private fun renderDefinition(table: DbTableImpl, definition: String): String = buildString {
        fun field(label: String, value: String) {
            append("<b>").append(label).append(":</b> ")
                .append(StringUtil.escapeXmlEntities(value)).append("<br>")
        }
        field("Data Source", table.dataSource.name)
        DasUtil.getCatalog(table).takeIf { it.isNotBlank() }?.let { field("Catalog", it) }
        field("Schema", DasUtil.getSchema(table))
        field("Table", table.name)
        append("<br><code><pre>")
        append(DbSqlUtil.sql2Html(table.project, StarRocksDialect.INSTANCE, definition))
        append("</pre></code>")
    }

    private companion object {
        val LOG = Logger.getInstance(StarRocksTableDocumentationTarget::class.java)

        fun loadNativeDefinition(table: DbTableImpl): String = DatabaseDefinitionHelper.loadDefinition(
            ConnectionProvider.forElement(table, DGDepartment.QUICK_DOCUMENTATION),
            table,
            StringBuilder()
        ).toString()
    }
}

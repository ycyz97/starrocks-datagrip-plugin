package com.github.ycyz.starrocks.datagrip.dialect

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.sql.dialects.SqlDialectMappings

class ContextAwareDialectResolver {
    fun shouldEnableStarRocksEnhancement(project: Project?, virtualFile: VirtualFile?): Boolean {
        if (project == null || virtualFile == null) return false
        val mappedDialect = SqlDialectMappings.getMapping(project, virtualFile) ?: return false
        val id = mappedDialect.id.lowercase()
        return id.contains("starrocks")
    }
}

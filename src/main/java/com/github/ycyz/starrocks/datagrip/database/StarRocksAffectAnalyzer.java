package com.github.ycyz.starrocks.datagrip.database;

import com.intellij.database.dialects.base.BaseAffectAnalyzer;
import com.intellij.database.introspection.Impactees;
import com.intellij.database.model.DasDataSource;
import com.intellij.database.util.SearchPath;
import com.intellij.psi.PsiElement;
import com.intellij.sql.psi.SqlCreateTableStatement;

import java.util.ArrayList;
import java.util.List;

/** Produces table-level refresh tasks for StarRocks DDL executed in a SQL console. */
public final class StarRocksAffectAnalyzer extends BaseAffectAnalyzer {
    @Override
    public Impactees determineAffectedObjects(
        DasDataSource dataSource,
        List<? extends PsiElement> script,
        SearchPath searchPath
    ) {
        List<PsiElement> persistentStatements = new ArrayList<>(script.size());
        for (PsiElement statement : script) {
            if (statement instanceof SqlCreateTableStatement table && table.isTemporary()) {
                continue;
            }
            persistentStatements.add(statement);
        }
        if (persistentStatements.isEmpty()) {
            return Impactees.EMPTY;
        }
        return super.determineAffectedObjects(dataSource, persistentStatements, searchPath);
    }
}

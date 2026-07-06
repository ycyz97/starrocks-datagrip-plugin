// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksAlterStatement extends PsiElement {

  @Nullable
  StarRocksAlterCatalogStatement getAlterCatalogStatement();

  @Nullable
  StarRocksAlterMaterializedViewStatement getAlterMaterializedViewStatement();

  @Nullable
  StarRocksAlterPrincipalStatement getAlterPrincipalStatement();

  @Nullable
  StarRocksAlterResourceStatement getAlterResourceStatement();

  @Nullable
  StarRocksAlterRoutineLoadStatement getAlterRoutineLoadStatement();

  @Nullable
  StarRocksAlterSchemaStatement getAlterSchemaStatement();

  @Nullable
  StarRocksAlterTableStatement getAlterTableStatement();

  @Nullable
  StarRocksAlterViewStatement getAlterViewStatement();

}

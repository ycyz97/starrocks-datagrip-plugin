// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksDropStatement extends PsiElement {

  @Nullable
  StarRocksDropCatalogStatement getDropCatalogStatement();

  @Nullable
  StarRocksDropIndexStatement getDropIndexStatement();

  @Nullable
  StarRocksDropMaterializedViewStatement getDropMaterializedViewStatement();

  @Nullable
  StarRocksDropPrincipalStatement getDropPrincipalStatement();

  @Nullable
  StarRocksDropRepositoryStatement getDropRepositoryStatement();

  @Nullable
  StarRocksDropResourceStatement getDropResourceStatement();

  @Nullable
  StarRocksDropSchemaStatement getDropSchemaStatement();

  @Nullable
  StarRocksDropTableStatement getDropTableStatement();

  @Nullable
  StarRocksDropViewStatement getDropViewStatement();

}

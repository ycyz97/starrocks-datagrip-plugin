// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksCreateStatement extends PsiElement {

  @Nullable
  StarRocksCreateCatalogStatement getCreateCatalogStatement();

  @Nullable
  StarRocksCreateIndexStatement getCreateIndexStatement();

  @Nullable
  StarRocksCreateMaterializedViewStatement getCreateMaterializedViewStatement();

  @Nullable
  StarRocksCreatePrincipalStatement getCreatePrincipalStatement();

  @Nullable
  StarRocksCreateRepositoryStatement getCreateRepositoryStatement();

  @Nullable
  StarRocksCreateResourceStatement getCreateResourceStatement();

  @Nullable
  StarRocksCreateRoutineLoadStatement getCreateRoutineLoadStatement();

  @Nullable
  StarRocksCreateSchemaStatement getCreateSchemaStatement();

  @Nullable
  StarRocksCreateTableStatement getCreateTableStatement();

  @Nullable
  StarRocksCreateViewStatement getCreateViewStatement();

}

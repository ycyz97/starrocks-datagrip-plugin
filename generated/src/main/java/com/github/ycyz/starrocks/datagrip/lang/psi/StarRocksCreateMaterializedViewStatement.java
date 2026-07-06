// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksCreateMaterializedViewStatement extends PsiElement {

  @Nullable
  StarRocksAsSelectClause getAsSelectClause();

  @NotNull
  List<StarRocksCreateMaterializedViewClause> getCreateMaterializedViewClauseList();

  @Nullable
  StarRocksMaterializedViewReference getMaterializedViewReference();

  @Nullable
  StarRocksTableColumnList getTableColumnList();

}

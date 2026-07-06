// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksCreateViewStatement extends PsiElement {

  @Nullable
  StarRocksAsSelectClause getAsSelectClause();

  @Nullable
  StarRocksCommentClause getCommentClause();

  @Nullable
  StarRocksTableColumnList getTableColumnList();

  @Nullable
  StarRocksViewReference getViewReference();

}

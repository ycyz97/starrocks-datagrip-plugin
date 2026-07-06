// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksDmlStatement extends PsiElement {

  @Nullable
  StarRocksDeleteStatement getDeleteStatement();

  @Nullable
  StarRocksInsertStatement getInsertStatement();

  @Nullable
  StarRocksMergeStatement getMergeStatement();

  @Nullable
  StarRocksSelectStatement getSelectStatement();

  @Nullable
  StarRocksUpdateStatement getUpdateStatement();

}

// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksMergeStatement extends PsiElement {

  @Nullable
  StarRocksDmlTargetTable getDmlTargetTable();

  @Nullable
  StarRocksMergeOnClause getMergeOnClause();

  @Nullable
  StarRocksMergeUsingClause getMergeUsingClause();

  @NotNull
  List<StarRocksMergeWhenClause> getMergeWhenClauseList();

}

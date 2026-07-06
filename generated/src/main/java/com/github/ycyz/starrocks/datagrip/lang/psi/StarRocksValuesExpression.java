// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksValuesExpression extends PsiElement {

  @Nullable
  StarRocksLimitClause getLimitClause();

  @Nullable
  StarRocksOrderByClause getOrderByClause();

  @NotNull
  StarRocksValuesClause getValuesClause();

}

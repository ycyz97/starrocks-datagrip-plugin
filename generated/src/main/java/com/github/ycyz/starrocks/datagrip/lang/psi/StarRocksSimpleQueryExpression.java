// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksSimpleQueryExpression extends PsiElement {

  @Nullable
  StarRocksFromClause getFromClause();

  @Nullable
  StarRocksGroupByClause getGroupByClause();

  @Nullable
  StarRocksHavingClause getHavingClause();

  @Nullable
  StarRocksLimitClause getLimitClause();

  @Nullable
  StarRocksOrderByClause getOrderByClause();

  @Nullable
  StarRocksQualifyClause getQualifyClause();

  @NotNull
  StarRocksSelectClause getSelectClause();

  @Nullable
  StarRocksWhereClause getWhereClause();

  @Nullable
  StarRocksWindowClause getWindowClause();

}

// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksTableExpression extends PsiElement {

  @NotNull
  List<StarRocksJoinExpression> getJoinExpressionList();

  @Nullable
  StarRocksParenthesizedJoinExpression getParenthesizedJoinExpression();

  @Nullable
  StarRocksParenthesizedQueryExpression getParenthesizedQueryExpression();

  @Nullable
  StarRocksTableAlias getTableAlias();

  @Nullable
  StarRocksTableAliasColumnList getTableAliasColumnList();

  @Nullable
  StarRocksTableFunctionCall getTableFunctionCall();

  @Nullable
  StarRocksTableReference getTableReference();

}

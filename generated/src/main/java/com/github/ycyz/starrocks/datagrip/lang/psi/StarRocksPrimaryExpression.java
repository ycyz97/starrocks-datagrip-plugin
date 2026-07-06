// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksPrimaryExpression extends PsiElement {

  @Nullable
  StarRocksCaseExpression getCaseExpression();

  @Nullable
  StarRocksCastExpression getCastExpression();

  @Nullable
  StarRocksColumnReference getColumnReference();

  @Nullable
  StarRocksExistsExpression getExistsExpression();

  @Nullable
  StarRocksFunctionCall getFunctionCall();

  @Nullable
  StarRocksIntervalExpression getIntervalExpression();

  @Nullable
  StarRocksLiteral getLiteral();

  @Nullable
  StarRocksParenthesizedValueExpression getParenthesizedValueExpression();

  @Nullable
  StarRocksTypedLiteralExpression getTypedLiteralExpression();

}

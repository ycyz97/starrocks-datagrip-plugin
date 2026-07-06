// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksPostfixExpression extends PsiElement {

  @Nullable
  StarRocksAnalyticClause getAnalyticClause();

  @NotNull
  List<StarRocksArrayAccessTail> getArrayAccessTailList();

  @NotNull
  List<StarRocksFieldAccessTail> getFieldAccessTailList();

  @NotNull
  StarRocksPrimaryExpression getPrimaryExpression();

}

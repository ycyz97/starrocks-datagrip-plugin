// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksSetOperationClause extends PsiElement {

  @NotNull
  StarRocksSetOperator getSetOperator();

  @Nullable
  StarRocksSimpleQueryExpression getSimpleQueryExpression();

}

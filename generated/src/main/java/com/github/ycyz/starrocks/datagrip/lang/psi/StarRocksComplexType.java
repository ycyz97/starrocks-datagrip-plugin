// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksComplexType extends PsiElement {

  @Nullable
  StarRocksArrayType getArrayType();

  @Nullable
  StarRocksMapType getMapType();

  @Nullable
  StarRocksStructType getStructType();

}

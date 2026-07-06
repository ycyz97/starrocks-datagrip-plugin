// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksColumnDefinition extends PsiElement {

  @NotNull
  List<StarRocksColumnAttribute> getColumnAttributeList();

  @NotNull
  StarRocksColumnName getColumnName();

  @NotNull
  StarRocksTypeElement getTypeElement();

}

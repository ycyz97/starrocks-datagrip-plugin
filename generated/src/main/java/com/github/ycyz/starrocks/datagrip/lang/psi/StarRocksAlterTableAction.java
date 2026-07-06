// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksAlterTableAction extends PsiElement {

  @Nullable
  StarRocksAddColumnAction getAddColumnAction();

  @Nullable
  StarRocksDropColumnAction getDropColumnAction();

  @Nullable
  StarRocksModifyColumnAction getModifyColumnAction();

  @Nullable
  StarRocksRenameAction getRenameAction();

  @Nullable
  StarRocksSetPropertiesAction getSetPropertiesAction();

}

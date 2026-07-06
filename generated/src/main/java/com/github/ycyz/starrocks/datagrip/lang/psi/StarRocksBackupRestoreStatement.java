// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksBackupRestoreStatement extends PsiElement {

  @NotNull
  List<StarRocksIdentifierReference> getIdentifierReferenceList();

  @Nullable
  StarRocksPropertiesClause getPropertiesClause();

  @NotNull
  List<StarRocksTableReference> getTableReferenceList();

}

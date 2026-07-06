// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksExportStatement extends PsiElement {

  @Nullable
  StarRocksPropertiesClause getPropertiesClause();

  @Nullable
  StarRocksSchemaReference getSchemaReference();

  @Nullable
  StarRocksStringLiteral getStringLiteral();

  @Nullable
  StarRocksTableReference getTableReference();

  @Nullable
  StarRocksWhereClause getWhereClause();

}

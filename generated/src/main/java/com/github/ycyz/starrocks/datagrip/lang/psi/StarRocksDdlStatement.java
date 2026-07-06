// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksDdlStatement extends PsiElement {

  @Nullable
  StarRocksAlterStatement getAlterStatement();

  @Nullable
  StarRocksCreateStatement getCreateStatement();

  @Nullable
  StarRocksDropStatement getDropStatement();

  @Nullable
  StarRocksGrantStatement getGrantStatement();

  @Nullable
  StarRocksRefreshMaterializedViewStatement getRefreshMaterializedViewStatement();

  @Nullable
  StarRocksRevokeStatement getRevokeStatement();

  @Nullable
  StarRocksTruncateTableStatement getTruncateTableStatement();

}

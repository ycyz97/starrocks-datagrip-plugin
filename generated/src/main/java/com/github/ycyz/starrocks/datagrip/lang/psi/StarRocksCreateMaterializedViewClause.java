// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksCreateMaterializedViewClause extends PsiElement {

  @Nullable
  StarRocksBucketsClause getBucketsClause();

  @Nullable
  StarRocksCommentClause getCommentClause();

  @Nullable
  StarRocksDistributionClause getDistributionClause();

  @Nullable
  StarRocksPartitionClause getPartitionClause();

  @Nullable
  StarRocksPropertiesClause getPropertiesClause();

  @Nullable
  StarRocksRefreshClause getRefreshClause();

}

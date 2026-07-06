// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksRoutineLoadClause extends PsiElement {

  @Nullable
  StarRocksColumnsClause getColumnsClause();

  @Nullable
  StarRocksFromKafkaClause getFromKafkaClause();

  @Nullable
  StarRocksPropertiesClause getPropertiesClause();

}

// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.github.ycyz.starrocks.datagrip.lang.psi.*;

public class StarRocksCreateTableClauseImpl extends ASTWrapperPsiElement implements StarRocksCreateTableClause {

  public StarRocksCreateTableClauseImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitCreateTableClause(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksBucketsClause getBucketsClause() {
    return findChildByClass(StarRocksBucketsClause.class);
  }

  @Override
  @Nullable
  public StarRocksCommentClause getCommentClause() {
    return findChildByClass(StarRocksCommentClause.class);
  }

  @Override
  @Nullable
  public StarRocksDistributionClause getDistributionClause() {
    return findChildByClass(StarRocksDistributionClause.class);
  }

  @Override
  @Nullable
  public StarRocksKeyModelClause getKeyModelClause() {
    return findChildByClass(StarRocksKeyModelClause.class);
  }

  @Override
  @Nullable
  public StarRocksOrderByClause getOrderByClause() {
    return findChildByClass(StarRocksOrderByClause.class);
  }

  @Override
  @Nullable
  public StarRocksPartitionClause getPartitionClause() {
    return findChildByClass(StarRocksPartitionClause.class);
  }

  @Override
  @Nullable
  public StarRocksPropertiesClause getPropertiesClause() {
    return findChildByClass(StarRocksPropertiesClause.class);
  }

}

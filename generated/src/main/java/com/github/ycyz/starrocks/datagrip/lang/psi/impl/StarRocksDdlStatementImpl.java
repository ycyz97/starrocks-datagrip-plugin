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

public class StarRocksDdlStatementImpl extends ASTWrapperPsiElement implements StarRocksDdlStatement {

  public StarRocksDdlStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitDdlStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksAlterStatement getAlterStatement() {
    return findChildByClass(StarRocksAlterStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCreateStatement getCreateStatement() {
    return findChildByClass(StarRocksCreateStatement.class);
  }

  @Override
  @Nullable
  public StarRocksDropStatement getDropStatement() {
    return findChildByClass(StarRocksDropStatement.class);
  }

  @Override
  @Nullable
  public StarRocksGrantStatement getGrantStatement() {
    return findChildByClass(StarRocksGrantStatement.class);
  }

  @Override
  @Nullable
  public StarRocksRefreshMaterializedViewStatement getRefreshMaterializedViewStatement() {
    return findChildByClass(StarRocksRefreshMaterializedViewStatement.class);
  }

  @Override
  @Nullable
  public StarRocksRevokeStatement getRevokeStatement() {
    return findChildByClass(StarRocksRevokeStatement.class);
  }

  @Override
  @Nullable
  public StarRocksTruncateTableStatement getTruncateTableStatement() {
    return findChildByClass(StarRocksTruncateTableStatement.class);
  }

}

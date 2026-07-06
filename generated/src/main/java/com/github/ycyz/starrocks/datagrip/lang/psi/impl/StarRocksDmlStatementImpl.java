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

public class StarRocksDmlStatementImpl extends ASTWrapperPsiElement implements StarRocksDmlStatement {

  public StarRocksDmlStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitDmlStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksDeleteStatement getDeleteStatement() {
    return findChildByClass(StarRocksDeleteStatement.class);
  }

  @Override
  @Nullable
  public StarRocksInsertStatement getInsertStatement() {
    return findChildByClass(StarRocksInsertStatement.class);
  }

  @Override
  @Nullable
  public StarRocksMergeStatement getMergeStatement() {
    return findChildByClass(StarRocksMergeStatement.class);
  }

  @Override
  @Nullable
  public StarRocksSelectStatement getSelectStatement() {
    return findChildByClass(StarRocksSelectStatement.class);
  }

  @Override
  @Nullable
  public StarRocksUpdateStatement getUpdateStatement() {
    return findChildByClass(StarRocksUpdateStatement.class);
  }

}

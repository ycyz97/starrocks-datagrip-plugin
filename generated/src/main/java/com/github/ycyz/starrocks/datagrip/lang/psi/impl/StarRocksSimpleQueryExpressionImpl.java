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

public class StarRocksSimpleQueryExpressionImpl extends ASTWrapperPsiElement implements StarRocksSimpleQueryExpression {

  public StarRocksSimpleQueryExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitSimpleQueryExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksFromClause getFromClause() {
    return findChildByClass(StarRocksFromClause.class);
  }

  @Override
  @Nullable
  public StarRocksGroupByClause getGroupByClause() {
    return findChildByClass(StarRocksGroupByClause.class);
  }

  @Override
  @Nullable
  public StarRocksHavingClause getHavingClause() {
    return findChildByClass(StarRocksHavingClause.class);
  }

  @Override
  @Nullable
  public StarRocksLimitClause getLimitClause() {
    return findChildByClass(StarRocksLimitClause.class);
  }

  @Override
  @Nullable
  public StarRocksOrderByClause getOrderByClause() {
    return findChildByClass(StarRocksOrderByClause.class);
  }

  @Override
  @Nullable
  public StarRocksQualifyClause getQualifyClause() {
    return findChildByClass(StarRocksQualifyClause.class);
  }

  @Override
  @NotNull
  public StarRocksSelectClause getSelectClause() {
    return findNotNullChildByClass(StarRocksSelectClause.class);
  }

  @Override
  @Nullable
  public StarRocksWhereClause getWhereClause() {
    return findChildByClass(StarRocksWhereClause.class);
  }

  @Override
  @Nullable
  public StarRocksWindowClause getWindowClause() {
    return findChildByClass(StarRocksWindowClause.class);
  }

}

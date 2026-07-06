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

public class StarRocksPrimaryExpressionImpl extends ASTWrapperPsiElement implements StarRocksPrimaryExpression {

  public StarRocksPrimaryExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitPrimaryExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksCaseExpression getCaseExpression() {
    return findChildByClass(StarRocksCaseExpression.class);
  }

  @Override
  @Nullable
  public StarRocksCastExpression getCastExpression() {
    return findChildByClass(StarRocksCastExpression.class);
  }

  @Override
  @Nullable
  public StarRocksColumnReference getColumnReference() {
    return findChildByClass(StarRocksColumnReference.class);
  }

  @Override
  @Nullable
  public StarRocksExistsExpression getExistsExpression() {
    return findChildByClass(StarRocksExistsExpression.class);
  }

  @Override
  @Nullable
  public StarRocksFunctionCall getFunctionCall() {
    return findChildByClass(StarRocksFunctionCall.class);
  }

  @Override
  @Nullable
  public StarRocksIntervalExpression getIntervalExpression() {
    return findChildByClass(StarRocksIntervalExpression.class);
  }

  @Override
  @Nullable
  public StarRocksLiteral getLiteral() {
    return findChildByClass(StarRocksLiteral.class);
  }

  @Override
  @Nullable
  public StarRocksParenthesizedValueExpression getParenthesizedValueExpression() {
    return findChildByClass(StarRocksParenthesizedValueExpression.class);
  }

  @Override
  @Nullable
  public StarRocksTypedLiteralExpression getTypedLiteralExpression() {
    return findChildByClass(StarRocksTypedLiteralExpression.class);
  }

}

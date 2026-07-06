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

public class StarRocksPostfixExpressionImpl extends ASTWrapperPsiElement implements StarRocksPostfixExpression {

  public StarRocksPostfixExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitPostfixExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksAnalyticClause getAnalyticClause() {
    return findChildByClass(StarRocksAnalyticClause.class);
  }

  @Override
  @NotNull
  public List<StarRocksArrayAccessTail> getArrayAccessTailList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, StarRocksArrayAccessTail.class);
  }

  @Override
  @NotNull
  public List<StarRocksFieldAccessTail> getFieldAccessTailList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, StarRocksFieldAccessTail.class);
  }

  @Override
  @NotNull
  public StarRocksPrimaryExpression getPrimaryExpression() {
    return findNotNullChildByClass(StarRocksPrimaryExpression.class);
  }

}

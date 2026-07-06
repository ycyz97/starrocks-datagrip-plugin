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

public class StarRocksCaseExpressionImpl extends ASTWrapperPsiElement implements StarRocksCaseExpression {

  public StarRocksCaseExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitCaseExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksCaseElseClause getCaseElseClause() {
    return findChildByClass(StarRocksCaseElseClause.class);
  }

  @Override
  @NotNull
  public List<StarRocksCaseWhenClause> getCaseWhenClauseList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, StarRocksCaseWhenClause.class);
  }

  @Override
  @Nullable
  public StarRocksValueExpression getValueExpression() {
    return findChildByClass(StarRocksValueExpression.class);
  }

}

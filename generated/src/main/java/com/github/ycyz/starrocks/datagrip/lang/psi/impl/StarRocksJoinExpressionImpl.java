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

public class StarRocksJoinExpressionImpl extends ASTWrapperPsiElement implements StarRocksJoinExpression {

  public StarRocksJoinExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitJoinExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksJoinConditionClause getJoinConditionClause() {
    return findChildByClass(StarRocksJoinConditionClause.class);
  }

  @Override
  @NotNull
  public StarRocksJoinOperator getJoinOperator() {
    return findNotNullChildByClass(StarRocksJoinOperator.class);
  }

  @Override
  @Nullable
  public StarRocksTableExpression getTableExpression() {
    return findChildByClass(StarRocksTableExpression.class);
  }

}

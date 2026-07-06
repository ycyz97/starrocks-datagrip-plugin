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

public class StarRocksTypedLiteralExpressionImpl extends ASTWrapperPsiElement implements StarRocksTypedLiteralExpression {

  public StarRocksTypedLiteralExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitTypedLiteralExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public StarRocksLiteral getLiteral() {
    return findNotNullChildByClass(StarRocksLiteral.class);
  }

  @Override
  @NotNull
  public StarRocksTypedLiteralPrefix getTypedLiteralPrefix() {
    return findNotNullChildByClass(StarRocksTypedLiteralPrefix.class);
  }

}

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

public class StarRocksLiteralImpl extends ASTWrapperPsiElement implements StarRocksLiteral {

  public StarRocksLiteralImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitLiteral(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksKeywordLiteral getKeywordLiteral() {
    return findChildByClass(StarRocksKeywordLiteral.class);
  }

  @Override
  @Nullable
  public StarRocksNumericLiteral getNumericLiteral() {
    return findChildByClass(StarRocksNumericLiteral.class);
  }

  @Override
  @Nullable
  public StarRocksParameterLiteral getParameterLiteral() {
    return findChildByClass(StarRocksParameterLiteral.class);
  }

  @Override
  @Nullable
  public StarRocksStringLiteral getStringLiteral() {
    return findChildByClass(StarRocksStringLiteral.class);
  }

}

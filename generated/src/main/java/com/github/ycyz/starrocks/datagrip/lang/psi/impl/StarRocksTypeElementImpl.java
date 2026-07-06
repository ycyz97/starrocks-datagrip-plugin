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

public class StarRocksTypeElementImpl extends ASTWrapperPsiElement implements StarRocksTypeElement {

  public StarRocksTypeElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitTypeElement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksComplexType getComplexType() {
    return findChildByClass(StarRocksComplexType.class);
  }

  @Override
  @Nullable
  public StarRocksScalarType getScalarType() {
    return findChildByClass(StarRocksScalarType.class);
  }

  @Override
  @Nullable
  public StarRocksTypeParameters getTypeParameters() {
    return findChildByClass(StarRocksTypeParameters.class);
  }

}

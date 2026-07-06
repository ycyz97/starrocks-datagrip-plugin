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

public class StarRocksDmlTargetTableImpl extends ASTWrapperPsiElement implements StarRocksDmlTargetTable {

  public StarRocksDmlTargetTableImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitDmlTargetTable(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksTableAlias getTableAlias() {
    return findChildByClass(StarRocksTableAlias.class);
  }

  @Override
  @Nullable
  public StarRocksTableAliasColumnList getTableAliasColumnList() {
    return findChildByClass(StarRocksTableAliasColumnList.class);
  }

  @Override
  @NotNull
  public StarRocksTableReference getTableReference() {
    return findNotNullChildByClass(StarRocksTableReference.class);
  }

}

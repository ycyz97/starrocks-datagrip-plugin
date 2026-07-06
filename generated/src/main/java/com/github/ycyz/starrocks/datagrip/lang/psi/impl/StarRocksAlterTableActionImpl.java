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

public class StarRocksAlterTableActionImpl extends ASTWrapperPsiElement implements StarRocksAlterTableAction {

  public StarRocksAlterTableActionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitAlterTableAction(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksAddColumnAction getAddColumnAction() {
    return findChildByClass(StarRocksAddColumnAction.class);
  }

  @Override
  @Nullable
  public StarRocksDropColumnAction getDropColumnAction() {
    return findChildByClass(StarRocksDropColumnAction.class);
  }

  @Override
  @Nullable
  public StarRocksModifyColumnAction getModifyColumnAction() {
    return findChildByClass(StarRocksModifyColumnAction.class);
  }

  @Override
  @Nullable
  public StarRocksRenameAction getRenameAction() {
    return findChildByClass(StarRocksRenameAction.class);
  }

  @Override
  @Nullable
  public StarRocksSetPropertiesAction getSetPropertiesAction() {
    return findChildByClass(StarRocksSetPropertiesAction.class);
  }

}

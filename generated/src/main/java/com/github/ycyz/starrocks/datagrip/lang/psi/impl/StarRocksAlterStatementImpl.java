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

public class StarRocksAlterStatementImpl extends ASTWrapperPsiElement implements StarRocksAlterStatement {

  public StarRocksAlterStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitAlterStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksAlterCatalogStatement getAlterCatalogStatement() {
    return findChildByClass(StarRocksAlterCatalogStatement.class);
  }

  @Override
  @Nullable
  public StarRocksAlterMaterializedViewStatement getAlterMaterializedViewStatement() {
    return findChildByClass(StarRocksAlterMaterializedViewStatement.class);
  }

  @Override
  @Nullable
  public StarRocksAlterPrincipalStatement getAlterPrincipalStatement() {
    return findChildByClass(StarRocksAlterPrincipalStatement.class);
  }

  @Override
  @Nullable
  public StarRocksAlterResourceStatement getAlterResourceStatement() {
    return findChildByClass(StarRocksAlterResourceStatement.class);
  }

  @Override
  @Nullable
  public StarRocksAlterRoutineLoadStatement getAlterRoutineLoadStatement() {
    return findChildByClass(StarRocksAlterRoutineLoadStatement.class);
  }

  @Override
  @Nullable
  public StarRocksAlterSchemaStatement getAlterSchemaStatement() {
    return findChildByClass(StarRocksAlterSchemaStatement.class);
  }

  @Override
  @Nullable
  public StarRocksAlterTableStatement getAlterTableStatement() {
    return findChildByClass(StarRocksAlterTableStatement.class);
  }

  @Override
  @Nullable
  public StarRocksAlterViewStatement getAlterViewStatement() {
    return findChildByClass(StarRocksAlterViewStatement.class);
  }

}

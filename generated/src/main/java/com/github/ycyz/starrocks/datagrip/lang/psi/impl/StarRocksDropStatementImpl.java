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

public class StarRocksDropStatementImpl extends ASTWrapperPsiElement implements StarRocksDropStatement {

  public StarRocksDropStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitDropStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksDropCatalogStatement getDropCatalogStatement() {
    return findChildByClass(StarRocksDropCatalogStatement.class);
  }

  @Override
  @Nullable
  public StarRocksDropIndexStatement getDropIndexStatement() {
    return findChildByClass(StarRocksDropIndexStatement.class);
  }

  @Override
  @Nullable
  public StarRocksDropMaterializedViewStatement getDropMaterializedViewStatement() {
    return findChildByClass(StarRocksDropMaterializedViewStatement.class);
  }

  @Override
  @Nullable
  public StarRocksDropPrincipalStatement getDropPrincipalStatement() {
    return findChildByClass(StarRocksDropPrincipalStatement.class);
  }

  @Override
  @Nullable
  public StarRocksDropRepositoryStatement getDropRepositoryStatement() {
    return findChildByClass(StarRocksDropRepositoryStatement.class);
  }

  @Override
  @Nullable
  public StarRocksDropResourceStatement getDropResourceStatement() {
    return findChildByClass(StarRocksDropResourceStatement.class);
  }

  @Override
  @Nullable
  public StarRocksDropSchemaStatement getDropSchemaStatement() {
    return findChildByClass(StarRocksDropSchemaStatement.class);
  }

  @Override
  @Nullable
  public StarRocksDropTableStatement getDropTableStatement() {
    return findChildByClass(StarRocksDropTableStatement.class);
  }

  @Override
  @Nullable
  public StarRocksDropViewStatement getDropViewStatement() {
    return findChildByClass(StarRocksDropViewStatement.class);
  }

}

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

public class StarRocksCreateStatementImpl extends ASTWrapperPsiElement implements StarRocksCreateStatement {

  public StarRocksCreateStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitCreateStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksCreateCatalogStatement getCreateCatalogStatement() {
    return findChildByClass(StarRocksCreateCatalogStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCreateIndexStatement getCreateIndexStatement() {
    return findChildByClass(StarRocksCreateIndexStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCreateMaterializedViewStatement getCreateMaterializedViewStatement() {
    return findChildByClass(StarRocksCreateMaterializedViewStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCreatePrincipalStatement getCreatePrincipalStatement() {
    return findChildByClass(StarRocksCreatePrincipalStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCreateRepositoryStatement getCreateRepositoryStatement() {
    return findChildByClass(StarRocksCreateRepositoryStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCreateResourceStatement getCreateResourceStatement() {
    return findChildByClass(StarRocksCreateResourceStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCreateRoutineLoadStatement getCreateRoutineLoadStatement() {
    return findChildByClass(StarRocksCreateRoutineLoadStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCreateSchemaStatement getCreateSchemaStatement() {
    return findChildByClass(StarRocksCreateSchemaStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCreateTableStatement getCreateTableStatement() {
    return findChildByClass(StarRocksCreateTableStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCreateViewStatement getCreateViewStatement() {
    return findChildByClass(StarRocksCreateViewStatement.class);
  }

}

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

public class StarRocksCreateMaterializedViewStatementImpl extends ASTWrapperPsiElement implements StarRocksCreateMaterializedViewStatement {

  public StarRocksCreateMaterializedViewStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitCreateMaterializedViewStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksAsSelectClause getAsSelectClause() {
    return findChildByClass(StarRocksAsSelectClause.class);
  }

  @Override
  @NotNull
  public List<StarRocksCreateMaterializedViewClause> getCreateMaterializedViewClauseList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, StarRocksCreateMaterializedViewClause.class);
  }

  @Override
  @Nullable
  public StarRocksMaterializedViewReference getMaterializedViewReference() {
    return findChildByClass(StarRocksMaterializedViewReference.class);
  }

  @Override
  @Nullable
  public StarRocksTableColumnList getTableColumnList() {
    return findChildByClass(StarRocksTableColumnList.class);
  }

}

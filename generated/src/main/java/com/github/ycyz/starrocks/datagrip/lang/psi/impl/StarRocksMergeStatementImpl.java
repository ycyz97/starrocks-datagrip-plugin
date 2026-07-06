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

public class StarRocksMergeStatementImpl extends ASTWrapperPsiElement implements StarRocksMergeStatement {

  public StarRocksMergeStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitMergeStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksDmlTargetTable getDmlTargetTable() {
    return findChildByClass(StarRocksDmlTargetTable.class);
  }

  @Override
  @Nullable
  public StarRocksMergeOnClause getMergeOnClause() {
    return findChildByClass(StarRocksMergeOnClause.class);
  }

  @Override
  @Nullable
  public StarRocksMergeUsingClause getMergeUsingClause() {
    return findChildByClass(StarRocksMergeUsingClause.class);
  }

  @Override
  @NotNull
  public List<StarRocksMergeWhenClause> getMergeWhenClauseList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, StarRocksMergeWhenClause.class);
  }

}

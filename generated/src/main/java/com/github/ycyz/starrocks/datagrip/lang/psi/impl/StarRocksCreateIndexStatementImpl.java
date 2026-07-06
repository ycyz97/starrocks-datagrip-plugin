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

public class StarRocksCreateIndexStatementImpl extends ASTWrapperPsiElement implements StarRocksCreateIndexStatement {

  public StarRocksCreateIndexStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitCreateIndexStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksCommentClause getCommentClause() {
    return findChildByClass(StarRocksCommentClause.class);
  }

  @Override
  @Nullable
  public StarRocksIndexReference getIndexReference() {
    return findChildByClass(StarRocksIndexReference.class);
  }

  @Override
  @Nullable
  public StarRocksParenthesizedIdentifierList getParenthesizedIdentifierList() {
    return findChildByClass(StarRocksParenthesizedIdentifierList.class);
  }

  @Override
  @Nullable
  public StarRocksPropertiesClause getPropertiesClause() {
    return findChildByClass(StarRocksPropertiesClause.class);
  }

  @Override
  @Nullable
  public StarRocksTableReference getTableReference() {
    return findChildByClass(StarRocksTableReference.class);
  }

}

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

public class StarRocksGrantStatementImpl extends ASTWrapperPsiElement implements StarRocksGrantStatement {

  public StarRocksGrantStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitGrantStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksPrivilegeList getPrivilegeList() {
    return findChildByClass(StarRocksPrivilegeList.class);
  }

  @Override
  @Nullable
  public StarRocksPrivilegeTarget getPrivilegeTarget() {
    return findChildByClass(StarRocksPrivilegeTarget.class);
  }

  @Override
  @Nullable
  public StarRocksSecurityPrincipalList getSecurityPrincipalList() {
    return findChildByClass(StarRocksSecurityPrincipalList.class);
  }

  @Override
  @Nullable
  public StarRocksWithGrantOptionClause getWithGrantOptionClause() {
    return findChildByClass(StarRocksWithGrantOptionClause.class);
  }

}

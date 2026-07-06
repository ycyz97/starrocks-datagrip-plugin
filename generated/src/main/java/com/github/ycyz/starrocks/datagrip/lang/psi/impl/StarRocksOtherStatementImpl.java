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

public class StarRocksOtherStatementImpl extends ASTWrapperPsiElement implements StarRocksOtherStatement {

  public StarRocksOtherStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StarRocksVisitor visitor) {
    visitor.visitOtherStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StarRocksVisitor) accept((StarRocksVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StarRocksAdminStatement getAdminStatement() {
    return findChildByClass(StarRocksAdminStatement.class);
  }

  @Override
  @Nullable
  public StarRocksAnalyzeStatement getAnalyzeStatement() {
    return findChildByClass(StarRocksAnalyzeStatement.class);
  }

  @Override
  @Nullable
  public StarRocksBackupRestoreStatement getBackupRestoreStatement() {
    return findChildByClass(StarRocksBackupRestoreStatement.class);
  }

  @Override
  @Nullable
  public StarRocksBeginStatement getBeginStatement() {
    return findChildByClass(StarRocksBeginStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCallStatement getCallStatement() {
    return findChildByClass(StarRocksCallStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCancelLoadStatement getCancelLoadStatement() {
    return findChildByClass(StarRocksCancelLoadStatement.class);
  }

  @Override
  @Nullable
  public StarRocksCommitStatement getCommitStatement() {
    return findChildByClass(StarRocksCommitStatement.class);
  }

  @Override
  @Nullable
  public StarRocksDescribeStatement getDescribeStatement() {
    return findChildByClass(StarRocksDescribeStatement.class);
  }

  @Override
  @Nullable
  public StarRocksExplainStatement getExplainStatement() {
    return findChildByClass(StarRocksExplainStatement.class);
  }

  @Override
  @Nullable
  public StarRocksExportStatement getExportStatement() {
    return findChildByClass(StarRocksExportStatement.class);
  }

  @Override
  @Nullable
  public StarRocksKillStatement getKillStatement() {
    return findChildByClass(StarRocksKillStatement.class);
  }

  @Override
  @Nullable
  public StarRocksLoadStatement getLoadStatement() {
    return findChildByClass(StarRocksLoadStatement.class);
  }

  @Override
  @Nullable
  public StarRocksRollbackStatement getRollbackStatement() {
    return findChildByClass(StarRocksRollbackStatement.class);
  }

  @Override
  @Nullable
  public StarRocksSetPasswordStatement getSetPasswordStatement() {
    return findChildByClass(StarRocksSetPasswordStatement.class);
  }

  @Override
  @Nullable
  public StarRocksSetStatement getSetStatement() {
    return findChildByClass(StarRocksSetStatement.class);
  }

  @Override
  @Nullable
  public StarRocksShowStatement getShowStatement() {
    return findChildByClass(StarRocksShowStatement.class);
  }

  @Override
  @Nullable
  public StarRocksSyncStatement getSyncStatement() {
    return findChildByClass(StarRocksSyncStatement.class);
  }

  @Override
  @Nullable
  public StarRocksTaskStatement getTaskStatement() {
    return findChildByClass(StarRocksTaskStatement.class);
  }

  @Override
  @Nullable
  public StarRocksUnsetStatement getUnsetStatement() {
    return findChildByClass(StarRocksUnsetStatement.class);
  }

  @Override
  @Nullable
  public StarRocksUseStatement getUseStatement() {
    return findChildByClass(StarRocksUseStatement.class);
  }

}

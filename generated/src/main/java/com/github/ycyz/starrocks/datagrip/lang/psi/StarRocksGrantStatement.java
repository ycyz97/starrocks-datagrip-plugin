// This is a generated file. Not intended for manual editing.
package com.github.ycyz.starrocks.datagrip.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StarRocksGrantStatement extends PsiElement {

  @Nullable
  StarRocksPrivilegeList getPrivilegeList();

  @Nullable
  StarRocksPrivilegeTarget getPrivilegeTarget();

  @Nullable
  StarRocksSecurityPrincipalList getSecurityPrincipalList();

  @Nullable
  StarRocksWithGrantOptionClause getWithGrantOptionClause();

}

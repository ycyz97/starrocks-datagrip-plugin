package com.github.ycyz.starrocks.datagrip.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.sql.dialects.base.SqlGeneratedParserUtil;
import com.intellij.sql.psi.SqlCompositeElementTypes;

public class StarRocksParserUtil extends SqlGeneratedParserUtil {
    public static boolean parseTableReference(PsiBuilder builder, int level) {
        return parseReference(builder, level, SqlCompositeElementTypes.SQL_TABLE_REFERENCE);
    }
}

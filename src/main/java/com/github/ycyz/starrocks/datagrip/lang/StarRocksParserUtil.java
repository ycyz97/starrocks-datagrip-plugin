package com.github.ycyz.starrocks.datagrip.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.sql.dialects.base.SqlGeneratedParserUtil;
import com.intellij.sql.psi.SqlCompositeElementTypes;
import com.intellij.sql.psi.SqlTokens;

import java.util.regex.Pattern;

public class StarRocksParserUtil extends SqlGeneratedParserUtil {
    private static final Pattern FUNCTION_WORD = Pattern.compile("[A-Za-z_][A-Za-z0-9_$]*");

    public static boolean parseTableReference(PsiBuilder builder, int level) {
        return parseReference(builder, level, SqlCompositeElementTypes.SQL_TABLE_REFERENCE);
    }

    public static boolean parseFunctionKeyword(PsiBuilder builder, int level) {
        if (builder.getTokenType() == null || builder.getTokenType() == SqlTokens.SQL_IDENT ||
            builder.getTokenType() == SqlTokens.SQL_IDENT_DELIMITED) {
            return false;
        }
        if (builder.rawLookup(1) != StarRocksElementTypes.SQL_LEFT_PAREN &&
            builder.rawLookup(1) != StarRocksElementTypes.SQL_PERIOD) {
            return false;
        }
        String text = builder.getTokenText();
        if (text == null || !FUNCTION_WORD.matcher(text).matches()) {
            return false;
        }
        builder.advanceLexer();
        return true;
    }

    public static boolean parseOptionalKeyword(PsiBuilder builder, int level) {
        String text = builder.getTokenText();
        if (text == null || !StarRocksKeywordCatalog.INSTANCE.isOptionalKeyword(text)) {
            return false;
        }
        builder.advanceLexer();
        return true;
    }
}

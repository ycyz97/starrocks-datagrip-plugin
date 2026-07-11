package com.github.ycyz.starrocks.datagrip.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.sql.dialects.base.SqlGeneratedParserUtil;
import com.intellij.sql.psi.SqlCompositeElementTypes;
import com.intellij.sql.psi.SqlTokens;

import java.util.regex.Pattern;
import java.util.Set;

public class StarRocksParserUtil extends SqlGeneratedParserUtil {
    private static final Pattern FUNCTION_WORD = Pattern.compile("[A-Za-z_][A-Za-z0-9_$]*");
    private static final Set<String> NON_FUNCTION_KEYWORDS = Set.of(
        "AS", "CASE", "ELSE", "END", "EXCEPT", "FROM", "GROUP", "HAVING", "IN",
        "INTERSECT", "JOIN", "LIMIT", "ON", "ORDER", "OVER", "QUALIFY", "SELECT",
        "THEN", "UNION", "WHEN", "WHERE", "WINDOW", "WITH"
    );

    public static boolean parseTableReference(PsiBuilder builder, int level) {
        return parseReference(builder, level, SqlCompositeElementTypes.SQL_TABLE_REFERENCE);
    }

    public static boolean parseFunctionKeyword(PsiBuilder builder, int level) {
        if (builder.getTokenType() == null || builder.getTokenType() == SqlTokens.SQL_IDENT ||
            builder.getTokenType() == SqlTokens.SQL_IDENT_DELIMITED) {
            return false;
        }
        if (builder.lookAhead(1) != StarRocksElementTypes.SQL_LEFT_PAREN &&
            builder.lookAhead(1) != StarRocksElementTypes.SQL_PERIOD) {
            return false;
        }
        String text = builder.getTokenText();
        if (text == null || NON_FUNCTION_KEYWORDS.contains(text.toUpperCase()) ||
            !FUNCTION_WORD.matcher(text).matches()) {
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

    public static boolean parseParameter(PsiBuilder builder, int level) {
        String text = builder.getTokenText();
        if (text == null) {
            return false;
        }
        if (text.equals("${") || text.equals("$[")) {
            String closing = text.equals("${") ? "}" : "]";
            builder.advanceLexer();
            while (builder.getTokenType() != null && !closing.equals(builder.getTokenText())) {
                builder.advanceLexer();
            }
            if (builder.getTokenType() != null) {
                builder.advanceLexer();
            }
            return true;
        }
        if (!(text.equals("?") || text.startsWith(":") ||
            (text.startsWith("${") && text.endsWith("}")) ||
            (text.startsWith("$[") && text.endsWith("]")))) {
            return false;
        }
        builder.advanceLexer();
        return true;
    }

    public static boolean isWithUpdate(PsiBuilder builder, int level) {
        if (!"WITH".equalsIgnoreCase(builder.getTokenText())) {
            return false;
        }
        int depth = 0;
        for (int index = 1; ; index++) {
            Object tokenType = builder.lookAhead(index);
            if (tokenType == null) {
                return false;
            }
            if (tokenType == StarRocksElementTypes.SQL_LEFT_PAREN) {
                depth++;
                continue;
            }
            if (tokenType == StarRocksElementTypes.SQL_RIGHT_PAREN) {
                depth--;
                continue;
            }
            if (depth == 0) {
                if (tokenType == StarRocksElementTypes.UPDATE) {
                    return true;
                }
                if (tokenType == StarRocksElementTypes.SELECT || tokenType == StarRocksElementTypes.VALUES ||
                    tokenType == StarRocksElementTypes.SQL_SEMICOLON) {
                    return false;
                }
            }
        }
    }

    public static boolean parsePrincipalComment(PsiBuilder builder, int level) {
        if (!"COMMENT".equalsIgnoreCase(builder.getTokenText())) {
            return false;
        }
        builder.advanceLexer();
        if (builder.getTokenType() != SqlTokens.SQL_STRING_TOKEN) {
            return false;
        }
        builder.advanceLexer();
        return true;
    }
}

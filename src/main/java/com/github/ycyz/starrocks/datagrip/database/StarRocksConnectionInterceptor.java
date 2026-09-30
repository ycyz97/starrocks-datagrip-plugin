package com.github.ycyz.starrocks.datagrip.database;

import com.intellij.database.dataSource.DatabaseFastConnectionInterceptor;

import java.util.regex.Pattern;

/** Configures Connector/J before the platform detects JDBC namespace capabilities. */
public final class StarRocksConnectionInterceptor extends DatabaseFastConnectionInterceptor {
    private static final Pattern DATABASE_TERM = Pattern.compile("([?&])databaseTerm=[^&#]*", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean interceptConnectionFast(ProtoConnection connection, boolean silent) {
        if (connection.getConnectionPoint().getDataSource().getDbms() != StarRocksDbms.INSTANCE) {
            return true;
        }
        // Connector/J's default CATALOG mode makes DataGrip flatten catalogs into
        // databases. SCHEMA mode preserves both levels in the generic introspector.
        connection.getConnectionProperties().put("databaseTerm", "SCHEMA");
        // An explicit URL setting must not override the required connection property.
        String url = connection.getUrl();
        String normalized = DATABASE_TERM.matcher(url).replaceAll("$1databaseTerm=SCHEMA");
        if (!normalized.equals(url)) {
            connection.setUrl(normalized);
        }
        return true;
    }
}

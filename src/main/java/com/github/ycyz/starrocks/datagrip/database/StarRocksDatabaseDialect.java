package com.github.ycyz.starrocks.datagrip.database;

import com.intellij.database.Dbms;
import com.intellij.database.dataSource.DatabaseConnectionCore;
import com.intellij.database.dialects.DatabaseDialect;
import com.intellij.database.dialects.base.AbstractDatabaseDialect;
import com.intellij.database.dialects.base.TypeHelper;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.ObjectKind;
import com.intellij.database.remote.jdbc.RemoteResultSet;
import com.intellij.database.remote.jdbc.RemoteStatement;
import com.intellij.database.util.DasUtil;
import com.intellij.database.util.Casing;
import com.intellij.database.util.DdlBuilder;
import com.intellij.database.util.ObjectPath;
import com.intellij.database.util.SearchPath;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.sql.psi.SqlCreateTableStatement;

import java.sql.SQLException;

/** Database Tools behavior needed to preserve the StarRocks connection namespace. */
public final class StarRocksDatabaseDialect extends AbstractDatabaseDialect {
    static final String CURRENT_CATALOG_QUERY = "SELECT @@catalog";
    static final String CURRENT_DATABASE_QUERY = "SELECT DATABASE()";

    public StarRocksDatabaseDialect() {
        super(new TypeHelper() {
            @Override
            public String getTypeName(int jdbcType, int length, int precision, int scale) {
                return null;
            }
        });
    }

    @Override
    public Dbms getDbms() {
        return StarRocksDbms.INSTANCE;
    }

    @Override
    public String getDisplayName() {
        return "Generic SQL";
    }

    @Override
    public ObjectKind getSearchPathObjectKind() {
        return ObjectKind.SCHEMA;
    }

    @Override
    public boolean supportsSearchPath() {
        return true;
    }

    @Override
    public boolean supportsSchemaChanging() {
        return true;
    }

    @Override
    public boolean triggersIntrospection(PsiElement element) {
        // StarRocks temporary tables are session-scoped and are not visible to
        // the separate metadata connection used by automatic introspection.
        if (element instanceof SqlCreateTableStatement table && table.isTemporary()) {
            return false;
        }
        return super.triggersIntrospection(element);
    }

    @Override
    public String sqlSetSearchPath(SearchPath searchPath) {
        ObjectPath current = searchPath.getCurrent();
        if (current == null || current.getName().isEmpty()) return null;
        if (current.kind == ObjectKind.DATABASE) {
            return "SET CATALOG " + quoteNamespace(current.getName());
        }
        // Database Tools calls setCatalog then setSchema on the same connection.
        // StarRocks Connector/J implements both with separate native statements.
        return null;
    }

    @Override
    public String sqlResetSearchPath() {
        return "SET CATALOG `default_catalog`";
    }

    @Override
    public String catToScript(String name, ObjectKind kind, boolean quote, boolean mixed, Casing casing) {
        // Keep the platform's quote/casing/keyword decision, using StarRocks
        // backticks instead of the generic naming service's double quotes.
        String scripted = super.catToScript(name, kind, quote, mixed, casing);
        return scripted.equals(name) ? name : quoteNamespace(name);
    }

    private static String quoteNamespace(String name) {
        return "`" + name.replace("`", "``") + "`";
    }

    @Override
    public boolean similarTo(DatabaseDialect dialect) {
        return dialect instanceof StarRocksDatabaseDialect;
    }

    @Override
    public DdlBuilder qualifiedIdentifier(
        DdlBuilder builder,
        String name,
        DasObject parent,
        DasObject object
    ) {
        DasObject schema = DasUtil.getSchemaObject(object);
        DasObject catalog = DasUtil.getCatalogObject(object);
        return builder.qualifiedRef(
            parent,
            name,
            schema,
            DasUtil.getName(schema),
            catalog,
            DasUtil.getName(catalog),
            null,
            null
        );
    }

    @Override
    public boolean supportsCommonTableExpression() {
        return false;
    }

    @Override
    public SearchPath tryToLoadSearchPath(DatabaseConnectionCore connection) throws SQLException {
        try {
            return loadSearchPath(sql -> queryScalar(connection, sql));
        }
        catch (ProcessCanceledException error) {
            throw error;
        }
        catch (Exception error) {
            if (error instanceof SQLException sqlError) throw sqlError;
            throw new SQLException("Unable to read the current StarRocks catalog and database", error);
        }
    }

    @Override
    public boolean shouldSwitchThroughJdbc(ObjectKind kind) {
        return kind == ObjectKind.DATABASE || kind == ObjectKind.SCHEMA;
    }

    static SearchPath loadSearchPath(ScalarQuery query) throws Exception {
        String catalog = query.execute(CURRENT_CATALOG_QUERY);
        if (catalog == null || catalog.isBlank()) return null;

        ObjectPath catalogPath = ObjectPath.create(catalog, ObjectKind.DATABASE);
        String database = query.execute(CURRENT_DATABASE_QUERY);
        if (database == null || database.isBlank()) {
            return SearchPath.of(catalogPath);
        }
        return SearchPath.of(catalogPath.append(database, ObjectKind.SCHEMA));
    }

    private static String queryScalar(DatabaseConnectionCore connection, String sql) throws Exception {
        RemoteStatement statement = null;
        RemoteResultSet resultSet = null;
        try {
            statement = connection.getRemoteConnection().createStatement();
            resultSet = statement.executeQuery(sql);
            return resultSet.next() ? resultSet.getString(1) : null;
        }
        finally {
            close(resultSet);
            close(statement);
        }
    }

    private static void close(RemoteResultSet resultSet) {
        if (resultSet == null) return;
        try {
            resultSet.close();
        }
        catch (Exception ignored) {
        }
    }

    private static void close(RemoteStatement statement) {
        if (statement == null) return;
        try {
            statement.close();
        }
        catch (Exception ignored) {
        }
    }

    @FunctionalInterface
    interface ScalarQuery {
        String execute(String sql) throws Exception;
    }
}

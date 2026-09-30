package com.github.ycyz.starrocks.datagrip.database;

import com.intellij.database.dataSource.DatabaseConnectionCore;
import com.intellij.database.dialects.base.introspector.jdbc.wrappers.ClosableIt;
import com.intellij.database.dialects.base.introspector.jdbc.wrappers.DatabaseMetaDataWrapper;
import com.intellij.database.model.DasRoutine;
import com.intellij.database.remote.jdbc.RemoteDatabaseMetaData;
import com.intellij.database.remote.jdbc.RemoteResultSet;
import com.intellij.util.Function;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.containers.JBIterator;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/** Supplies the public JDBC introspector with StarRocks namespaces, not MySQL namespaces. */
public final class StarRocksMetadataWrapper extends DatabaseMetaDataWrapper {
    public StarRocksMetadataWrapper(DatabaseConnectionCore connection, RemoteDatabaseMetaData metadata) {
        super(connection, metadata);
    }

    // Database Tools represents a SQL catalog as DATABASE and a SQL database as SCHEMA.
    @Override
    public ClosableIt<String> databases() throws SQLException {
        return query("SHOW CATALOGS", rs -> rs.getString(1));
    }

    @Override
    public ClosableIt<Schema> schemas(JBIterable<String> catalogs) throws SQLException {
        List<Schema> schemas = new ArrayList<>();
        try (ClosableIt<String> names = catalogs.isEmpty() ? databases() : ClosableIt.from(catalogs.iterator())) {
            while (names.hasNext()) {
                String catalog = names.next();
                try (ClosableIt<Schema> databases = query("SHOW DATABASES FROM " + identifier(catalog),
                    rs -> new Schema(rs.getString(1), catalog))) {
                    databases.forEachRemaining(schemas::add);
                }
            }
        }
        return ClosableIt.from(schemas.iterator());
    }

    @Override
    public String[] getAllTableTypes() {
        return new String[]{"TABLE", "VIEW"};
    }

    @Override
    public ClosableIt<? extends Table> tables(Schema schema, String pattern, String[] types) throws SQLException {
        return query(tablesQuery(schema, pattern), rs -> {
            Table table = new Table(schema, rs.getString(1));
            table.type = tableType(rs.getString(2));
            return table;
        });
    }

    @Override
    protected ClosableIt<TableColumn> getTableColumnsInner(Schema schema, String pattern,
                                                           Iterable<String> expectedTables) throws SQLException {
        // SHOW uses object visibility privileges. SELECT on an external catalog's
        // information_schema would require additional SELECT grants on its tables.
        Set<String> names = new TreeSet<>();
        if (expectedTables != null && pattern == null) {
            expectedTables.forEach(names::add);
        } else {
            try (var tables = tables(schema, pattern, getAllTableTypes())) {
                tables.forEachRemaining(table -> names.add(table.name));
            }
            if (expectedTables != null) {
                Set<String> expected = new TreeSet<>();
                expectedTables.forEach(expected::add);
                names.retainAll(expected);
            }
        }
        List<TableColumn> columns = new ArrayList<>();
        for (String name : names) {
            Table table = new Table(schema, name);
            int[] position = {0};
            try (var tableColumns = query(columnsQuery(schema, name), rs -> {
                TableColumn column = new TableColumn(table);
                // SHOW FULL COLUMNS: Field, Type, Collation, Null, Key, Default,
                // Extra, Privileges, Comment. Key is deliberately not introspected.
                column.name = rs.getString(1);
                column.typeName = rs.getString(2);
                column.type = jdbcType(column.typeName.split("[<(\\s]", 2)[0]);
                column.position = ++position[0];
                column.nullable = "YES".equalsIgnoreCase(rs.getString(4));
                column.def = rs.getString(6);
                column.comment = rs.getString(9);
                // The platform parses length/scale from the complete native type.
                column.length = -1;
                String extra = rs.getString(7);
                column.autoIncrement = extra != null && extra.toLowerCase(Locale.ROOT).contains("auto_increment");
                return column;
            })) {
                tableColumns.forEachRemaining(columns::add);
            }
        }
        return ClosableIt.from(columns.iterator());
    }

    static String tablesQuery(Schema schema, String pattern) {
        return "SHOW FULL TABLES FROM " + namespace(schema) +
            (pattern == null ? "" : " LIKE " + literal(pattern));
    }

    static String columnsQuery(Schema schema, String table) {
        return "SHOW FULL COLUMNS FROM " + namespace(schema) + "." + identifier(table);
    }

    private static String namespace(Schema schema) {
        return identifier(schema.database) + "." + identifier(schema.schema);
    }

    public static String identifier(String name) {
        return "`" + name.replace("`", "``") + "`";
    }

    public static String literal(String value) {
        return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'";
    }

    static String tableType(String type) {
        return type != null && type.toUpperCase(Locale.ROOT).contains("VIEW") ? "VIEW" : "TABLE";
    }

    static int jdbcType(String type) {
        return switch (type.toUpperCase(Locale.ROOT)) {
            case "BOOLEAN" -> Types.BOOLEAN;
            case "TINYINT" -> Types.TINYINT;
            case "SMALLINT" -> Types.SMALLINT;
            case "INT", "INTEGER" -> Types.INTEGER;
            case "BIGINT" -> Types.BIGINT;
            case "LARGEINT", "DECIMAL", "DECIMAL32", "DECIMAL64", "DECIMAL128", "DECIMAL256" -> Types.DECIMAL;
            case "FLOAT" -> Types.FLOAT;
            case "DOUBLE" -> Types.DOUBLE;
            case "DATE" -> Types.DATE;
            case "DATETIME", "TIMESTAMP" -> Types.TIMESTAMP;
            case "CHAR" -> Types.CHAR;
            case "VARCHAR", "STRING" -> Types.VARCHAR;
            case "BINARY", "VARBINARY" -> Types.VARBINARY;
            case "ARRAY" -> Types.ARRAY;
            default -> Types.OTHER;
        };
    }

    private <T> ClosableIt<T> query(String sql, RowReader<T> reader) throws SQLException {
        List<T> rows = new ArrayList<>();
        try {
            var statement = getConnection().getRemoteConnection().createStatement();
            try {
                var rs = statement.executeQuery(sql);
                try {
                    while (rs.next()) {
                        com.intellij.openapi.progress.ProgressManager.checkCanceled();
                        rows.add(reader.read(rs));
                    }
                } finally {
                    rs.close();
                }
            } finally {
                statement.close();
            }
        } catch (com.intellij.openapi.progress.ProcessCanceledException cancelled) {
            throw cancelled;
        } catch (Exception error) {
            if (error instanceof SQLException sqlError) throw sqlError;
            throw new SQLException("Unable to introspect StarRocks metadata: " + sql, error);
        }
        return ClosableIt.from(rows.iterator());
    }

    @FunctionalInterface
    private interface RowReader<T> {
        T read(RemoteResultSet rs) throws Exception;
    }

    // Do not ask the MySQL driver for objects that are outside this metadata model.
    @Override public ClosableIt.GroupingIt<TableKey, TableKeyColumn> tableKeys(Table table) { return emptyGroups(); }
    @Override public ClosableIt.GroupingIt<TableFKey, TableFKeyColumn> tableFKeyColumns(Table table) { return emptyGroups(); }
    @Override public ClosableIt.GroupingIt<TableIndex, TableIndexColumn> tableIndexColumns(Table table) { return emptyGroups(); }
    @Override public ClosableIt<TableColumn> tableVersionColumns(Table table) { return ClosableIt.empty(); }
    @Override public ClosableIt<Routine> routines(Schema schema, String name, String specificName) { return ClosableIt.empty(); }
    @Override public ClosableIt.GroupingIt<Routine, RoutineArgument> routineArguments(
        Schema schema, String name, Function<Routine, DasRoutine.Kind> kind) { return emptyGroups(); }
    @Override public ClosableIt<? extends UserDefinedType> userDefinedTypes(Schema schema, String name) { return ClosableIt.empty(); }
    @Override public ClosableIt.GroupingIt<UserDefinedType, UserDefinedTypeAttribute> userDefinedTypeAttributes(
        Schema schema, String name, Set<String> names) { return emptyGroups(); }

    private static <T, C> ClosableIt.GroupingIt<T, C> emptyGroups() {
        return new ClosableIt.GroupingIt<>() {
            @Override protected T nextImpl() { return stop(); }
            @Override public JBIterator<C> groupIt() { return ClosableIt.empty(); }
            @Override public void close() { }
        };
    }

    public static final class Factory extends MDFactory {
        @Override
        public DatabaseMetaDataWrapper create(DatabaseConnectionCore connection, RemoteDatabaseMetaData metadata) {
            return new StarRocksMetadataWrapper(connection, metadata);
        }
    }
}

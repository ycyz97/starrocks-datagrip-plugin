# StarRocks Support

[简体中文](README.zh-CN.md)

StarRocks Support is a JetBrains DataGrip/IntelliJ plugin that provides an
independent StarRocks SQL dialect and StarRocks database integration.

The plugin is built on JetBrains' SQL language platform. Its parser grammar,
parser lexer, dialect catalogs, formatting extensions, and supplementary
completion are scoped to the StarRocks language.

- Marketplace: [StarRocks Support][marketplace]
- Syntax coverage: [`SYNTAX_COVERAGE.md`](SYNTAX_COVERAGE.md)

## Capabilities

- Registers a dedicated StarRocks SQL dialect, DBMS, and data source type.
- Parses StarRocks SQL with a JFlex-generated parser lexer and a
  Grammar-Kit-generated parser.
- Reuses JetBrains SQL PSI element types for standard SQL structures so that
  platform formatting, references, resolution, and database-aware editing keep
  working.
- Highlights StarRocks keywords, identifiers, literals, comments, parameters,
  and operators.
- Publishes StarRocks keywords, scalar and complex types, and a broad built-in
  function catalog to the SQL platform.
- Provides supplementary completion for StarRocks-specific snippets and
  properties while leaving ordinary table, column, type, and function
  completion to the platform SQL completion system.
- Extends the platform SQL formatter with StarRocks SQL support.
- Integrates JDBC metadata, the StarRocks type system, and native `SHOW CREATE`
  statements with the Database Tools platform.
- Synchronizes `catalog → database → table/view`, grouping ordinary and
  materialized views under Views. Column metadata is retained; JDBC foreign
  keys, indexes, key constraints, routines, and user-defined types are not queried.

The predefined driver selects **Latest stable** for StarRocks Connector/J, recommended
by the [StarRocks JDBC documentation](https://docs.starrocks.io/docs/integrations/JDBC_driver/).
This selects the latest stable entry in the available artifact list; the plugin
currently includes `com.starrocks:starrocks-connector-j:1.1.1`. New releases must
be added to that list; this setting does not query Maven Central for new versions.
The native selector uses JDBC `setCatalog` followed by `setSchema` on the same
connection. This driver sends separate `SET CATALOG` and `USE` commands; no
multi-query setting or custom remote JDBC helper is needed.

For an existing data source, restart the IDE and update its StarRocks driver:
download the StarRocks Connector/J artifact, remove the MySQL Connector/J driver
file, set the class to `com.starrocks.cj.jdbc.Driver`, and change the URL prefix
from `jdbc:mysql:` to `jdbc:starrocks:` (for example,
`jdbc:starrocks://host:9030/default_catalog.ads`). Saved custom driver definitions
may need to be reset to the predefined defaults or updated manually. Remove the
old `databaseTerm` and plugin-added `allowMultiQueries` settings; keep
`useLocalSessionState=false` so selecting the same database name in another
catalog still executes `USE`. Then disconnect, reconnect and fully refresh the
data source. Existing connection URLs and driver files are not silently migrated.
On DataGrip 2026.1, the standard remote `setSchema` path can suppress a database
selection error. If the toolbar and query behavior disagree, check
`SELECT @@catalog, DATABASE()` in that same session; the plugin reads server
state when loading the search path, but does not override the platform's error UI.
Synchronization requires access to `SHOW CATALOGS`,
`SHOW DATABASES FROM catalog`, `SHOW FULL TABLES`, and `SHOW FULL COLUMNS`.
It does not require SELECT grants on an external catalog's `information_schema.tables/columns`.

Syntax support is intentionally tracked as structured coverage rather than a
claim of full StarRocks compatibility. The current target baseline is the
StarRocks 4.1 documentation. See the [coverage checklist](SYNTAX_COVERAGE.md)
for implemented, partial, missing, and pending areas.

## Architecture

The parser follows a generated-grammar architecture:

```text
grammar/starrocks-keywords.txt
        -> generated keyword/token registries

grammar/starrocks.flex
        -> JFlex parser lexer

grammar/starrocks.bnf
        -> Grammar-Kit parser

generated lexer + generated parser
        -> DataGrip SQL parser adapter
        -> JetBrains SQL PSI / formatter / resolve / completion
        -> StarRocks database services
```

The main design rules are:

- `grammar/starrocks.flex` is the source of truth for parser lexing.
- `grammar/starrocks.bnf` is the source of truth for parser grammar.
- `StarRocksParser` is an adapter for JetBrains SQL parser hooks; grammar logic
  belongs in the generated parser.
- Standard SQL nodes map to platform SQL PSI wherever possible.
- StarRocks-only behavior is implemented through explicit PSI mappings,
  formatter helpers, supplementary completion, and database services.
- There is no handwritten whole-statement parser fallback.

Generated source code under `build/generated` must not be edited manually.
Change the grammar or keyword catalog and run the generation tasks instead.

## Project layout

```text
grammar/
  starrocks.flex              Parser lexer grammar
  starrocks.bnf               Parser grammar and PSI element mappings
  starrocks-keywords.txt      Canonical keyword catalog

src/main/kotlin/.../
  lang/                       Parser adapter, lexer facade, element mappings
  dialect/                    Dialect identity and function catalogs
  highlight/                  Syntax highlighting
  format/                     SQL platform formatting extensions
  completion/                 StarRocks-specific supplementary completion
  database/                   DBMS, types, metadata, and DDL integration

src/test/kotlin/              Parser, PSI, resolve, formatter, and integration tests
src/testData/sql/             SQL acceptance and regression scenarios
```

## Requirements

- JDK 17
- The bundled Gradle Wrapper, currently Gradle 9.6.1
- DataGrip 2026.1 or later at runtime (`sinceBuild = 261`)
- DataGrip 2026.1.4 (`261.26222.86`) is the default development, test, and
  plugin-verification platform; it can be
  overridden with `intellijPlatformVersion` or `intellijPlatformLocalPath`

## Build and verification

Compile the plugin:

```powershell
.\gradlew.bat compileKotlin --no-daemon
```

Validate the lexer and grammar source assets:

```powershell
.\gradlew.bat validateGrammarSources --no-daemon
```

Generate the parser assets explicitly when working on grammar changes:

```powershell
.\gradlew.bat generateLexer generateParser --no-daemon
```

Run the StarRocks SQL scenario validator:

```powershell
.\gradlew.bat validateStarRocksScenarios --no-daemon
```

Run the complete verification suite:

```powershell
.\gradlew.bat check --no-daemon
```

Build the distributable plugin ZIP:

```powershell
.\gradlew.bat buildPlugin --no-daemon
```

The ZIP is written to `build/distributions/`.

## Development

Use the checked-in run configuration to start a sandbox IDE from IntelliJ IDEA
or DataGrip:

```text
.run/Run IDE with Plugin.run.xml
```

It can also be started from the command line:

```powershell
.\gradlew.bat runIde --no-daemon
```

When adding syntax support:

1. Verify the syntax against the targeted StarRocks documentation.
2. Update `starrocks.flex`, `starrocks.bnf`, or the canonical keyword catalog.
3. Add focused parser tests and a scenario fixture when introducing a new
   statement family.
4. Verify PSI mappings, formatting, and references for object-bearing syntax.
5. Update the syntax coverage checklist.
6. Run the grammar validation, scenarios, and complete test suite.

## License

Licensed under the Apache License 2.0. See [`LICENSE`](LICENSE).

[marketplace]: https://plugins.jetbrains.com/plugin/32243-starrocks-support

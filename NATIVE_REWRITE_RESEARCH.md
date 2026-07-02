# Native StarRocks Rewrite Research

## 2026-06-30 Platform Architecture Update

Inspection of the bundled DataGrip 2025.1.4.1 (`DB-251.28774.27`)
`database-plugin.jar` confirms that bundled SQL dialects share the JetBrains SQL
parser framework instead of maintaining unrelated hand-written parser stacks.

Observed signatures and call targets:

- `GenericParser extends Sql92Parser`.
- `Sql92Parser extends SqlParser` and delegates to `Sql92GeneratedParser`,
  `Sql92DmlParsing`, `Sql92DdlParsing`, and `Sql92ExpressionParsing`.
- `CHouseParser extends SqlParser` and delegates to `CHouseGeneratedParser`,
  `CHouseDmlParsing`, `CHouseDdlParsing`, and `CHouseExpressionParsing`.
- `MysqlParser extends SqlParser` and delegates to `MysqlGeneratedParser`,
  `MysqlDmlParsing`, `MysqlDdlParsing`, and `MysqlExpressionParsing`.
- BigQuery, Snowflake, Hive/HiveBase, and PostgreSQL also extend `SqlParser`
  or a dialect-specific `SqlParser` base and delegate their entry points to
  generated dialect grammar classes.
- Parser definitions extend `SqlParserDefinitionBase` and provide dialect
  lexers, parsers, file element types, and element factories.

Updated direction:

- The final StarRocks language layer should be a JetBrains SQL-framework
  dialect, not a standalone lightweight parser.
- `StarRocksParser extends SqlParser` and mirrors full bundled dialects by
  delegating to generated
  `StarRocksGeneratedParser`, `StarRocksDmlParsing`,
  `StarRocksDdlParsing`, and `StarRocksExpressionParsing`.
- StarRocks-specific syntax remains possible in this model. It should be added
  through generated StarRocks grammar and custom StarRocks PSI nodes, not through
  broad formatter hacks or a permanently separate lightweight parser.

## Legacy Baseline and Current Integration Map

The rewrite branch started from a MySQL-compatible JetBrains SQL integration,
but the current branch no longer uses the MySQL lexer/parser/dialect as the
main language path.

Main extension points still relevant to the final plugin:

- `com.intellij.sql.dialect` registers `StarRocksDialect`.
- `lang.parserDefinition` registers `StarRocksParserDefinition`.
- `lang.syntaxHighlighterFactory` registers StarRocks syntax highlighting.
- `completion.contributor` registers StarRocks-specific completion for both SQL
  and StarRocks language contexts.
- `com.intellij.database.dbms`, `dialect`, `typeSystem`, `definitionProvider`,
  and `driversConfig` register StarRocks database integration.

Legacy implementation constraints that the rewrite is replacing:

- `StarRocksDialect` extends `MysqlDialectBase`.
- `StarRocksParser` extends `MysqlParser`.
- `StarRocksLexer` wraps `MysqlLexer` and remaps selected tokens.
- StarRocks DDL is often handled by lenient statement parsing.
- Formatter behavior depends on whole-document string rewrites.
- Data source behavior still depends partly on MySQL dialect/type system.

Current rewrite status before the 2026-06-30 architecture update:

- `StarRocksDialect` now extends `SqlLanguageDialectBase`.
- `StarRocksParser` was an independent statement-level parser skeleton.
- `StarRocksLexer` is an independent lexer skeleton.
- `StarRocksParserDefinition` uses the StarRocks element factory.
- That coarse parser direction has been superseded by the platform-framework
  decision above.
- The JDBC driver configuration may still use MySQL wire-protocol classes and
  URLs where StarRocks compatibility requires it; that is separate from the SQL
  language/parser architecture.

## Rewrite Direction

The rewrite should introduce a native StarRocks track instead of continuing to
patch MySQL behavior. The target is not just fewer red underlines; the target is
structured StarRocks PSI and first-class database integration.

The native implementation should separate these concerns:

- Language layer: lexer, parser, PSI, syntax highlighting.
- Dialect layer: StarRocks SQL dialect rules and function metadata.
- Database layer: DBMS, data source, driver config, type system, DDL provider.
- Editor layer: completion, formatting, inspections, local SQL resolution.

## Reference Sources

Primary StarRocks references:

- StarRocks official `fe-grammar`.
- StarRocks official `fe-parser`.
- StarRocks documentation for SQL syntax and functions.
- StarRocks source/tests for syntax cases not fully documented.

JetBrains implementation references:

- Hive and HiveBase for complex SQL types and non-MySQL DDL.
- ClickHouse for analytical SQL and database-tool integration patterns.
- BigQuery and Snowflake for `QUALIFY`-style query clauses.
- Existing MySQL implementation only as a compatibility reference, not as the
  main parser strategy.

## Proposed Package Layout

The rewrite branch should delete the legacy implementation and rebuild under the
normal plugin package names.

Target package layout:

- `com.github.ycyz.starrocks.datagrip.lang`
- `com.github.ycyz.starrocks.datagrip.psi`
- `com.github.ycyz.starrocks.datagrip.dialect`
- `com.github.ycyz.starrocks.datagrip.database`
- `com.github.ycyz.starrocks.datagrip.completion`
- `com.github.ycyz.starrocks.datagrip.format`
- `com.github.ycyz.starrocks.datagrip.resolve`

## Phase 1 Skeleton Goals

The first implementation milestone should not attempt full SQL coverage. It
should only prove that the native track can load safely.

Minimum skeleton after legacy removal:

- StarRocks language object and parser definition.
- Lexer/parser placeholder that can parse a file without crashing.
- Syntax highlighter that does not depend on MySQL token remapping.
- Database DBMS and driver registration retained or wrapped behind native
  classes.
- Build remains green.

Temporary compatibility should be minimized. If a compatibility shim is required
to keep the plugin loadable, it must live behind the final package names and be
marked for removal.

## Phase 1 Skeleton Status

Legacy production registrations for completion, formatter post-processing,
inspection suppression, lenient parsing support, and DDL definition rewriting
have been removed from the rewrite branch. The remaining implementation is a
minimal StarRocks shell under the final package names.

The language layer is now detached from the MySQL lexer/parser path. StarRocks
has its own lexer skeleton, parser skeleton, parser definition, and element
factory under the final package names. The current parser is still a coarse
statement-level skeleton, not a complete grammar implementation.

The DBMS singleton has been moved into the `database` package so later data
source work can evolve independently from dialect code. The dialect package keeps
only a deprecated forwarding holder for temporary source compatibility.

The first parser milestone metadata is now represented as source code:
`StarRocksGrammarMilestone` defines the rewrite order and `StarRocksKeyword`
captures the StarRocks-specific keywords that must move into the native grammar.
These classes are not wired into editor behavior yet.

Function and type metadata now exist as isolated catalog classes. They are not
registered as completion or highlighting providers yet, but they establish the
data source for future native completion, function resolution, and type-system
work.

Regression SQL samples are stored in `src/testData/sql`. They define the first
syntax scenarios the native parser must support before editor features are
rewired.

The parser scenario catalog now mirrors those SQL fixtures in Kotlin. This keeps
the acceptance matrix close to source code and gives future automated parser
tests a stable list of files, milestones, and required features.

The first generated-grammar-shaped StarRocks skeleton now exists under the
final language package:

- `StarRocksGeneratedParser`
- `StarRocksDmlParsing`
- `StarRocksDdlParsing`
- `StarRocksExpressionParsing`
- `StarRocksParsingUtil`

This skeleton mirrors the public entry point shape used by bundled dialects
such as ClickHouse, MySQL, BigQuery, Snowflake, Hive, and PostgreSQL
(`statement`, DML query parsing, DDL type parsing, value-expression parsing,
and an `EXTENDS_SETS_` hook). `StarRocksParser` now extends `SqlParser`
directly and delegates those entry points to the StarRocks grammar helpers.
The current StarRocks grammar covers ordinary SELECT/INSERT/WITH entrypoints,
StarRocks `QUALIFY` and `UNNEST`, `CREATE TABLE`, `CREATE MATERIALIZED VIEW`,
`PROPERTIES`, and complex types such as `ARRAY`, `MAP`, `STRUCT`, and `JSON`.

`validateRewriteScenarios` checks that the SQL fixture files exist and are
documented. It is intentionally lightweight and does not start the IDE.

`src/testData/sql/scenarios.properties` is the lightweight fixture manifest. It
keeps SQL files, milestones, and feature names aligned with documentation.

`STARROCKS_SYNTAX_COVERAGE.md` is the broad syntax coverage matrix. The rewrite
should expand parser fixtures and implementation by syntax family instead of
only addressing individual bug reports.

## Risks

- JetBrains SQL PSI and database resolution APIs are not designed around simple
  external grammar drop-in replacement.
- A generated StarRocks parser may not directly produce PSI nodes expected by
  DataGrip SQL features.
- Replacing the data source layer may affect Marketplace compatibility and
  supported product detection.
- Formatting should be delayed until PSI shape is stable; early formatter work is
  likely to create regressions.

## Immediate Next Step

Move from the loadable language skeleton to structured parser work:

- Define native StarRocks element types for statement families, query clauses,
  DDL nodes, expressions, and types.
- Replace statement-level parsing with incremental grammar-backed parsing for
  the fixture families in `src/testData/sql`.
- Keep using JetBrains Hive, ClickHouse, BigQuery, and Snowflake as integration
  references before introducing new parser, PSI, formatter, or resolve code.
- Keep formatter and completion deferred until the relevant PSI structures are
  stable.

## Chinese Status Summary

当前重构分支的开发路线仍在计划内。最新状态已经不是继续继承 MySQL
parser/dialect，而是已经拆出 StarRocks 自己的 dialect、lexer、parser、
parser definition 和 element factory。现在的 parser 仍然只是粗粒度
statement-level 骨架，下一步应进入结构化语法节点和 PSI 的实现。

后续开发顺序应保持为：先补齐 StarRocks 原生语法结构，再做解析上下文和数据源，
最后再恢复补全和格式化。格式化和补全不要早于 PSI 稳定，否则容易重新引入之前那类
互相影响的回归。

---

# StarRocks 原生重构技术调研

## 当前集成地图

当前插件仍然建立在 JetBrains 的 MySQL 兼容 SQL 基础设施之上。

当前使用的主要扩展点：

- `com.intellij.sql.dialect` 注册 `StarRocksDialect`。
- `lang.parserDefinition` 注册 `StarRocksParserDefinition`。
- `lang.syntaxHighlighterFactory` 注册 StarRocks 语法高亮。
- `lang.formatter` 委托 JetBrains SQL formatter。
- `completion.contributor` 在 SQL 和 StarRocks language 上下文中注册补全。
- `preFormatProcessor` 和 `postFormatProcessor` 做字符串级格式化修正。
- `sql.inspectionSuppressorDelegate` 抑制部分 SQL resolve 警告。
- `com.intellij.database.dbms`、`dialect`、`typeSystem`、`definitionProvider`
  和 `driversConfig` 注册 StarRocks 数据库集成。

当前实现约束：

- `StarRocksDialect` 继承 `MysqlDialectBase`。
- `StarRocksParser` 继承 `MysqlParser`。
- `StarRocksLexer` 包装 `MysqlLexer` 并重映射部分 token。
- StarRocks DDL 经常通过宽松语句解析处理。
- Formatter 行为依赖整文档字符串重写。
- 数据源行为仍部分依赖 MySQL dialect/type system。

## 重构方向

重构应引入原生 StarRocks 路线，而不是继续修补 MySQL 行为。目标不是单纯减少
红线，而是建立结构化 StarRocks PSI 和一等数据库集成。

原生实现应拆分这些职责：

- 语言层：lexer、parser、PSI、语法高亮。
- 方言层：StarRocks SQL 方言规则和函数元数据。
- 数据库层：DBMS、数据源、driver 配置、type system、DDL provider。
- 编辑器层：补全、格式化、inspection、本地 SQL 解析上下文。

## 参考来源

StarRocks 主要参考：

- StarRocks 官方 `fe-grammar`。
- StarRocks 官方 `fe-parser`。
- StarRocks SQL 语法和函数文档。
- StarRocks 源码和测试，用于补充文档未完整覆盖的语法场景。

JetBrains 实现参考：

- Hive 和 HiveBase：复杂类型和非 MySQL DDL。
- ClickHouse：分析型 SQL 和 Database Tools 集成模式。
- BigQuery 和 Snowflake：`QUALIFY` 类查询子句。
- 现有 MySQL 实现只作为兼容参考，不作为主 parser 策略。

## 建议包结构

重构分支应删除旧实现，并在正式插件包名下重新构建。

目标包结构：

- `com.github.ycyz.starrocks.datagrip.lang`
- `com.github.ycyz.starrocks.datagrip.psi`
- `com.github.ycyz.starrocks.datagrip.dialect`
- `com.github.ycyz.starrocks.datagrip.database`
- `com.github.ycyz.starrocks.datagrip.completion`
- `com.github.ycyz.starrocks.datagrip.format`
- `com.github.ycyz.starrocks.datagrip.resolve`

## 第一阶段骨架目标

第一个实现里程碑不应追求完整 SQL 覆盖，只需要证明原生路线可以安全加载。

删除旧实现后的最小骨架：

- StarRocks language object 和 parser definition。
- 不会导致 SQL 文件崩溃的 lexer/parser 占位实现。
- 不依赖 MySQL token 重映射的 syntax highlighter。
- 保留或通过原生类包装数据库 DBMS 和 driver 注册。
- 构建保持通过。

骨架阶段应尽量减少临时兼容代码。如果为了保持插件可加载必须保留兼容层，
也应放在最终包名下，并明确标注后续删除。

## 风险

- JetBrains SQL PSI 和数据库解析 API 并不是简单外部 grammar 替换即可接入。
- 生成的 StarRocks parser 未必能直接产出 DataGrip SQL 功能期望的 PSI 节点。
- 替换数据源层可能影响 Marketplace 兼容性和支持产品识别。
- Formatter 应延后到 PSI 结构稳定后再做；过早做格式化很容易引入回归。

## 第一阶段骨架状态

重构分支已经移除旧的补全、格式化后处理、inspection suppressor、宽松解析辅助和
DDL definition 改写等生产注册。当前剩余实现是放在最终包名下的最小 StarRocks
外壳。

现阶段 lexer/parser 仍然临时委托 JetBrains MySQL 类，只用于保证插件可以加载和继续
迭代。这不是目标架构，后续 parser 里程碑必须替换为原生 StarRocks grammar/parser
路径。

DBMS 单例已经移动到 `database` 包，后续数据源层可以独立于 dialect 代码继续演进。
`dialect` 包中只保留临时兼容转发类。

第一批 parser 里程碑元数据已经进入源码：`StarRocksGrammarMilestone` 定义重写顺序，
`StarRocksKeyword` 记录必须进入原生 grammar 的 StarRocks 专属关键字。这些类目前还
没有接入编辑器行为。

函数和类型元数据已经以独立 catalog 类的形式加入源码。它们目前还没有注册为补全或
高亮 provider，但会作为后续原生补全、函数解析和类型系统工作的数据来源。

回归 SQL 样例放在 `src/testData/sql` 下。它们定义了原生 parser 在重新接入编辑器功能
之前必须先支持的第一批语法场景。

parser 场景 catalog 已经在 Kotlin 源码中对应这些 SQL fixture。这样验收矩阵会贴近
源码，后续自动化 parser 测试也能直接复用稳定的文件、里程碑和必备功能清单。

`validateRewriteScenarios` 会检查 SQL fixture 文件是否存在并已写入文档。该任务刻意保持
轻量，不启动 IDE。

`src/testData/sql/scenarios.properties` 是轻量 fixture manifest，用于让 SQL 文件、里程碑
和功能点名称与文档保持一致。

`STARROCKS_SYNTAX_COVERAGE.md` 是完整语法覆盖矩阵。重构后续应按语法族扩展 parser
fixture 和实现，而不是只围绕单个报错点修补。

## 下一步

删除 legacy 生产注册，并用最终包名下的最小 StarRocks 骨架替换。骨架必须先
保证编译通过并保持插件可加载，然后再进入更深入的 parser 工作。

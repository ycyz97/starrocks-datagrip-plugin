# StarRocks Support Architecture Rewrite Plan

## Summary

This branch is a long-running rewrite track for StarRocks Support. The current
`main` branch remains the stable maintenance line for `1.2.x` releases. This
branch is allowed to replace the current MySQL-parser-based implementation with
a native StarRocks plugin architecture.

The goal is not to keep extending lenient parser fallbacks. The goal is to build
first-class StarRocks support for SQL syntax, data sources, type metadata, DDL
loading, completion, formatting, and local SQL context resolution.

## Branch Strategy

- `main` remains the stable release branch.
- `refactor/rewrite-starrocks-plugin` is the rewrite branch.
- The rewrite branch may be unstable and does not need to stay publishable during
  early phases.
- Backport only clear, low-risk fixes from this branch to `main`.
- Do not add new MySQL parser hacks to this branch unless they are temporary
  scaffolding for migration.

## Reuse Policy

Reusable assets:

- Plugin ID, name, icon assets, license, README positioning, and Marketplace
  metadata.
- Driver configuration as a starting point.
- Existing StarRocks function lists as reference data.
- Existing bug reports and manual SQL samples as regression cases.

Replace or redesign:

- `StarRocksLexer` token remapping hacks.
- `StarRocksParser` lenient statement parsing.
- `QUALIFY` mapped to MySQL `HAVING`.
- String-level DDL/query formatter post-processing as the primary formatter
  strategy.
- Inspection suppressors that only hide unresolved symbols.
- Any data source behavior that depends on pretending StarRocks is only MySQL.

## Architecture Targets

### SQL Language

- Implement a native StarRocks lexer/parser/PSI path.
- Prefer StarRocks official `fe-grammar` and `fe-parser` as syntax references.
- Use JetBrains dialect implementations such as Hive, ClickHouse, BigQuery, and
  Snowflake as platform integration references.
- Preserve structured PSI for tables, columns, CTEs, aliases, functions, and DDL
  objects.

### Data Source and Metadata

- Rebuild StarRocks DBMS, driver, dialect, type system, and DDL definition
  integration as first-class StarRocks components.
- Avoid relying on MySQL behavior where StarRocks semantics diverge.
- Support native `SHOW CREATE` paths for tables, views, and materialized views.

### Editing Experience

- Rebuild keyword, function, snippet, and property completion on top of native
  StarRocks context.
- Support local SQL context resolution, including unexecuted `CREATE TABLE`
  followed by `INSERT`.
- Implement formatter behavior incrementally and keep it idempotent.
- Do not use whole-file string rewrites as the primary formatting mechanism.

## Implementation Phases

### Phase 1: Research and Skeleton

- Document parser and data source integration options.
- Identify the minimum JetBrains extension points required for native language,
  DBMS, type system, DDL provider, completion, and formatter integration.
- Delete legacy parser/formatter/completion/inspection production code before
  rebuilding the new implementation under the final package names.
- Keep the plugin loadable while the native implementation is incomplete.

### Phase 2: Core Query Syntax

- Implement high-frequency query support first:
  - `SELECT`
  - `WITH`
  - joins
  - `QUALIFY`
  - window functions
  - `UNNEST`
- Ensure function highlighting and aliases do not regress.
- Support `HAVING` and `QUALIFY` in the same query without token substitution.

### Phase 3: Core DDL and MV Syntax

- Implement structured StarRocks `CREATE TABLE` support:
  - table name
  - column definitions
  - complex types
  - key models
  - partitioning
  - distribution
  - properties
- Implement materialized view statements:
  - `CREATE MATERIALIZED VIEW`
  - `ALTER MATERIALIZED VIEW`
  - `REFRESH MATERIALIZED VIEW`
  - `CANCEL REFRESH MATERIALIZED VIEW`
- Preserve `AS SELECT` parsing inside DDL.

### Phase 4: Data Source and Resolution

- Rebuild StarRocks data source integration.
- Implement type metadata and native DDL loading.
- Make local, unexecuted DDL available to later statements in the same SQL file.
- Restore table, column, alias, CTE, and function resolution as first-class
  behavior.

### Phase 5: Formatting and Completion

- Rebuild completion on native StarRocks contexts.
- Implement small, idempotent formatter rules only after syntax PSI is stable.
- Validate repeated formatting produces identical text.

## Acceptance Criteria

- The plugin installs and SQL files open without runtime errors.
- `compileKotlin --no-daemon` passes.
- `buildPlugin --no-daemon` produces a package.
- StarRocks syntax works without MySQL token substitution for:
  - `CAST(... AS BIGINT)`
  - `QUALIFY row_number() OVER (...) = 1`
  - `HAVING ... QUALIFY ...`
  - `UNNEST`
- StarRocks DDL works for:
  - `CREATE TABLE ... PRIMARY KEY ... PARTITION BY ... DISTRIBUTED BY ... PROPERTIES ...`
  - `CREATE MATERIALIZED VIEW ... AS SELECT ...`
- A later `INSERT INTO` can resolve a table and columns declared by an earlier
  unexecuted `CREATE TABLE` in the same SQL file.
- Normal MySQL dialect files do not receive StarRocks-only behavior.
- Marketplace verifier does not report binary compatibility errors.

## Non-Goals for Early Phases

- Maintaining feature parity with the current `main` branch at all times.
- Publishing early rewrite builds to Marketplace.
- Supporting every low-frequency StarRocks management statement before core
  query and DDL syntax is stable.
- Continuing broad lenient fallbacks as the main syntax strategy.
- Keeping parallel legacy and rewrite production implementations in the same
  branch.

---

# StarRocks Support 架构重构计划

## 概要

该分支是 StarRocks Support 的长期重构线。当前 `main` 分支继续作为
`1.2.x` 版本的稳定维护线。本分支允许用原生 StarRocks 插件架构替换当前
基于 MySQL parser 的实现。

本次重构目标不是继续扩展宽松 parser fallback，而是为 StarRocks SQL 语法、
数据源、类型元数据、DDL 获取、补全、格式化和本地 SQL 上下文解析建立一等支持。

## 分支策略

- `main` 保持为稳定发布分支。
- `refactor/rewrite-starrocks-plugin` 作为重构分支。
- 重构分支早期可以不稳定，也不要求始终可发布。
- 只把明确、低风险的修复从重构分支回迁到 `main`。
- 除非是迁移脚手架，否则不要在该分支继续增加新的 MySQL parser hack。

## 复用策略

可复用资产：

- 插件 ID、名称、图标资源、License、README 定位和 Marketplace 元数据。
- Driver 配置可作为起点。
- 现有 StarRocks 函数清单可作为参考数据。
- 已发现的问题和手动 SQL 样例可作为回归测试场景。

替换或重新设计：

- `StarRocksLexer` 中的 token 重映射 hack。
- `StarRocksParser` 中的宽松语句解析。
- 将 `QUALIFY` 映射为 MySQL `HAVING` 的做法。
- 以字符串全文处理为主的 DDL/query formatter 后处理。
- 只隐藏未解析符号的 inspection suppressor。
- 任何依赖“把 StarRocks 当成 MySQL”来工作的数据源行为。

## 架构目标

### SQL 语言

- 实现原生 StarRocks lexer/parser/PSI 路径。
- 优先参考 StarRocks 官方 `fe-grammar` 和 `fe-parser`。
- 参考 JetBrains Hive、ClickHouse、BigQuery、Snowflake 等方言的集成方式。
- 保留表、列、CTE、别名、函数和 DDL 对象的结构化 PSI。

### 数据源和元数据

- 重新构建 StarRocks DBMS、driver、dialect、type system 和 DDL definition 集成。
- 避免在 StarRocks 语义与 MySQL 不一致的地方继续依赖 MySQL 行为。
- 支持表、视图和物化视图的原生 `SHOW CREATE` 获取路径。

### 编辑体验

- 基于原生 StarRocks 上下文重建关键字、函数、片段和属性补全。
- 支持本地 SQL 上下文解析，包括未执行 `CREATE TABLE` 后接 `INSERT` 的场景。
- 逐步实现格式化，并保持格式化结果幂等。
- 不再把整文件字符串重写作为主要格式化机制。

## 实施阶段

### 第一阶段：调研和骨架

- 记录 parser 和数据源集成方案。
- 确认原生 language、DBMS、type system、DDL provider、completion 和 formatter
  所需的最小 JetBrains extension point。
- 删除旧 parser/formatter/completion/inspection 生产代码，然后在最终包名下
  重建新实现。
- 在原生实现尚不完整时，仍保持插件可以加载。

### 第二阶段：核心查询语法

- 优先实现高频查询支持：
  - `SELECT`
  - `WITH`
  - join
  - `QUALIFY`
  - 窗口函数
  - `UNNEST`
- 确保函数高亮和别名解析不退化。
- 在不做 token 替换的前提下支持 `HAVING` 和 `QUALIFY` 同时出现。

### 第三阶段：核心 DDL 和物化视图语法

- 实现结构化 StarRocks `CREATE TABLE` 支持：
  - 表名
  - 列定义
  - 复杂类型
  - 表模型
  - 分区
  - 分桶
  - 属性
- 实现物化视图语句：
  - `CREATE MATERIALIZED VIEW`
  - `ALTER MATERIALIZED VIEW`
  - `REFRESH MATERIALIZED VIEW`
  - `CANCEL REFRESH MATERIALIZED VIEW`
- 保留 DDL 内 `AS SELECT` 的解析能力。

### 第四阶段：数据源和解析上下文

- 重建 StarRocks 数据源集成。
- 实现类型元数据和原生 DDL 加载。
- 让同一 SQL 文件中未执行的本地 DDL 可被后续语句使用。
- 把表、列、别名、CTE 和函数解析恢复为一等能力。

### 第五阶段：格式化和补全

- 基于原生 StarRocks 上下文重建补全。
- 只有在语法 PSI 稳定后，才实现小范围、幂等的 formatter 规则。
- 验证重复格式化会产生完全相同的文本。

## 验收标准

- 插件可以安装，SQL 文件可以打开且无运行时错误。
- `compileKotlin --no-daemon` 通过。
- `buildPlugin --no-daemon` 可以产出插件包。
- StarRocks 语法不依赖 MySQL token 替换即可支持：
  - `CAST(... AS BIGINT)`
  - `QUALIFY row_number() OVER (...) = 1`
  - `HAVING ... QUALIFY ...`
  - `UNNEST`
- StarRocks DDL 支持：
  - `CREATE TABLE ... PRIMARY KEY ... PARTITION BY ... DISTRIBUTED BY ... PROPERTIES ...`
  - `CREATE MATERIALIZED VIEW ... AS SELECT ...`
- 后续 `INSERT INTO` 可以解析同一 SQL 文件中前面未执行的 `CREATE TABLE`
  声明的表和字段。
- 普通 MySQL 方言文件不会启用 StarRocks 专属行为。
- Marketplace verifier 不报告二进制兼容性错误。

## 早期非目标

- 不要求始终与当前 `main` 分支保持功能等价。
- 不发布早期重构版本到 Marketplace。
- 在核心查询和 DDL 语法稳定前，不优先覆盖所有低频 StarRocks 管理语句。
- 不再把宽泛的 lenient fallback 作为主要语法策略。
- 不在同一分支内长期保留 legacy 和 rewrite 两套生产实现。

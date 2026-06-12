# StarRocks Support for DataGrip

JetBrains DataGrip/IntelliJ plugin that adds StarRocks SQL dialect support.

The plugin uses JetBrains MySQL SQL support as the baseline and applies StarRocks-specific behavior only when the file or data source is in a StarRocks context.

## Features

- StarRocks dialect and data source registration.
- StarRocks SQL keyword highlighting for common DDL, DML, materialized view, pipe, task, load, and refresh statements.
- StarRocks function completion and highlighting backed by the local function registry.
- Compatibility handling for StarRocks SQL such as:
  - `FULL JOIN` and `FULL OUTER JOIN`
  - `INSERT OVERWRITE`
  - `CREATE MATERIALIZED VIEW`
  - `REFRESH MATERIALIZED VIEW`
  - `UNNEST` and lateral join forms
  - `PROPERTIES ("key" = "value")`
- Formatting adjustments for common StarRocks DDL clauses, `PROPERTIES`, `DISTRIBUTED BY HASH`, `PARTITION BY`, and `UNNEST`.

## Requirements

- JDK 17
- Gradle wrapper or Gradle 8.13
- JetBrains DataGrip 2025.1 or compatible 251-based IDE

## Build

Compile:

```powershell
.\gradlew.bat compileKotlin --no-daemon
```

Build plugin ZIP:

```powershell
.\gradlew.bat buildPlugin --no-daemon
```

The plugin ZIP is generated under:

```text
build/distributions/
```

## Development

Run the IDE sandbox from IntelliJ IDEA/DataGrip using the checked-in run configuration:

```text
.run/Run IDE with Plugin.run.xml
```

Or run from the command line:

```powershell
.\gradlew.bat runIde --no-daemon
```

## Manual Smoke Test

Use the smoke SQL file as the manual regression baseline:

```text
src/main/resources/sql/starrocks/starrocks_editing_smoke.sql
```

Recommended checks:

- StarRocks dialect can be selected.
- `FULL JOIN` and `FULL OUTER JOIN` do not show syntax errors.
- Field, alias, and function highlighting remain usable in joins.
- Official `UNNEST` examples do not show syntax errors.
- `PROPERTIES ("key" = "value")` treats double-quoted values as strings.
- Formatting does not break StarRocks DDL, materialized views, `PROPERTIES`, or `UNNEST`.
- Normal MySQL dialect behavior is unchanged.

## Source Policy

StarRocks-specific syntax, keywords, and functions should only be added when backed by StarRocks official documentation, StarRocks source code, or StarRocks official tests.

Tracked references live in:

```text
src/main/resources/sql/starrocks/starrocks_official_sources.md
```

---

# StarRocks DataGrip 支持插件

为 JetBrains DataGrip/IntelliJ 提供 StarRocks SQL 方言支持的插件。

本插件以 JetBrains 内置 MySQL SQL 能力为基础，只在 StarRocks 方言文件或 StarRocks 数据源上下文中启用 StarRocks 专属增强。

## 功能

- StarRocks 方言和数据源注册。
- 支持常见 DDL、DML、物化视图、Pipe、Task、Load、Refresh 等语句中的 StarRocks 关键字高亮。
- 基于本地函数注册表提供 StarRocks 函数补全和高亮。
- 对以下 StarRocks SQL 语法做兼容处理：
  - `FULL JOIN` 和 `FULL OUTER JOIN`
  - `INSERT OVERWRITE`
  - `CREATE MATERIALIZED VIEW`
  - `REFRESH MATERIALIZED VIEW`
  - `UNNEST` 和 lateral join 形式
  - `PROPERTIES ("key" = "value")`
- 针对常见 StarRocks DDL 子句、`PROPERTIES`、`DISTRIBUTED BY HASH`、`PARTITION BY` 和 `UNNEST` 做格式化调整。

## 环境要求

- JDK 17
- Gradle Wrapper 或 Gradle 8.13
- JetBrains DataGrip 2025.1，或兼容 251 平台版本的 IDE

## 构建

编译：

```powershell
.\gradlew.bat compileKotlin --no-daemon
```

构建插件 ZIP：

```powershell
.\gradlew.bat buildPlugin --no-daemon
```

插件 ZIP 会生成在：

```text
build/distributions/
```

## 开发

可以使用仓库中提交的运行配置，在 IntelliJ IDEA/DataGrip 中启动 IDE 沙箱：

```text
.run/Run IDE with Plugin.run.xml
```

也可以通过命令行运行：

```powershell
.\gradlew.bat runIde --no-daemon
```

## 手动冒烟测试

使用以下 smoke SQL 文件作为手动回归测试基准：

```text
src/main/resources/sql/starrocks/starrocks_editing_smoke.sql
```

建议检查：

- 可以选择 StarRocks 方言。
- `FULL JOIN` 和 `FULL OUTER JOIN` 不出现语法错误。
- Join 场景中的字段、别名和函数高亮保持可用。
- 官方 `UNNEST` 示例不出现语法错误。
- `PROPERTIES ("key" = "value")` 中的双引号值按字符串处理。
- 格式化不会破坏 StarRocks DDL、物化视图、`PROPERTIES` 或 `UNNEST`。
- 普通 MySQL 方言行为不受影响。

## 来源策略

新增 StarRocks 专属语法、关键字和函数时，必须有 StarRocks 官方文档、StarRocks 源码或 StarRocks 官方测试作为依据。

当前记录的官方来源位于：

```text
src/main/resources/sql/starrocks/starrocks_official_sources.md
```

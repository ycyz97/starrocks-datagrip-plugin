# StarRocks Support

JetBrains DataGrip/IntelliJ plugin that adds StarRocks SQL dialect support.

The plugin uses JetBrains MySQL SQL support as the baseline and applies StarRocks-specific behavior only when the file or data source is in a StarRocks context.

## Features

- StarRocks dialect and data source registration.
- Keyword highlighting and lenient parsing for common StarRocks query, DDL, DML, and management statements.
- Function completion and highlighting backed by the local StarRocks function registry.
- Formatting support for common StarRocks-specific statement clauses.

## Requirements

- JDK 17
- Gradle wrapper or Gradle 8.13
- JetBrains DataGrip 2025.1 or compatible 251-based IDE
- StarRocks syntax coverage is based on StarRocks 4.1 official documentation

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

## Version

Current plugin version: `1.2.1`

---

# StarRocks 支持插件

为 JetBrains DataGrip/IntelliJ 提供 StarRocks SQL 方言支持的插件。

本插件以 JetBrains 内置 MySQL SQL 能力为基础，只在 StarRocks 方言文件或 StarRocks 数据源上下文中启用 StarRocks 专属增强。

## 功能

- StarRocks 方言和数据源注册。
- 对常见 StarRocks 查询、DDL、DML 和管理语句提供关键字高亮和宽松解析。
- 基于本地 StarRocks 函数注册表提供函数补全和高亮。
- 对常见 StarRocks 专属语句子句提供格式化支持。

## 环境要求

- JDK 17
- Gradle Wrapper 或 Gradle 8.13
- JetBrains DataGrip 2025.1，或兼容 251 平台版本的 IDE
- StarRocks 语法覆盖基于 StarRocks 4.1 官方文档

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

## 版本

当前插件版本：`1.2.1`

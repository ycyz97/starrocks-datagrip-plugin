# StarRocks Support

JetBrains DataGrip/IntelliJ plugin that adds StarRocks SQL dialect support.

The plugin uses JetBrains MySQL SQL support as the baseline and applies StarRocks-specific behavior only when the file or data source is in a StarRocks context.

JetBrains Marketplace: [StarRocks Support](https://plugins.jetbrains.com/plugin/32243-starrocks-support)

## Features

- StarRocks dialect and data source registration.
- Keyword highlighting and lenient parsing for common StarRocks query, DDL, DML, and management statements.
- Function, keyword, snippet, and property completion for common StarRocks SQL editing workflows.
- Formatting support for common StarRocks-specific statement clauses.
- Native StarRocks `SHOW CREATE` support for viewing object DDL from StarRocks data sources.

## Requirements

- JDK 17
- Gradle wrapper or Gradle 8.13
- JetBrains DataGrip 2025.1 or compatible 251-based IDE
- StarRocks syntax coverage is based on StarRocks 4.1 official documentation.

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

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE).

---

# StarRocks Support 中文说明

这是一个为 JetBrains DataGrip / IntelliJ 系列 IDE 提供 StarRocks SQL 方言支持的插件。

插件以 JetBrains 内置 MySQL SQL 能力为基础，只在 StarRocks 方言文件或 StarRocks 数据源上下文中启用 StarRocks 专属增强。

JetBrains 插件市场：[StarRocks Support](https://plugins.jetbrains.com/plugin/32243-starrocks-support)

## 功能

- StarRocks 方言和数据源注册。
- 为常见 StarRocks 查询、DDL、DML 和管理语句提供关键字高亮和宽松解析。
- 为常见 StarRocks SQL 编辑流程提供函数、关键字、片段和属性补全。
- 为常见 StarRocks 专属语句子句提供格式化支持。
- 在 StarRocks 数据源下使用原生 `SHOW CREATE` 获取对象 DDL。

## 环境要求

- JDK 17
- Gradle Wrapper 或 Gradle 8.13
- JetBrains DataGrip 2025.1，或兼容 251 平台版本的 IDE
- StarRocks 语法覆盖基于 StarRocks 4.1 官方文档。

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

可以使用仓库中提交的运行配置，在 IntelliJ IDEA / DataGrip 中启动 IDE 沙箱：

```text
.run/Run IDE with Plugin.run.xml
```

也可以通过命令行运行：

```powershell
.\gradlew.bat runIde --no-daemon
```

## 许可证

本项目使用 Apache License 2.0。详见 [LICENSE](LICENSE)。

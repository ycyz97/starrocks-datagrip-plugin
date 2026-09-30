# StarRocks Support

[English](README.md)

StarRocks Support 是一款面向 JetBrains DataGrip/IntelliJ 的插件，提供独立的 StarRocks SQL 方言和 StarRocks 数据库集成。

插件构建在 JetBrains SQL 语言平台之上。解析语法、解析词法、方言目录、格式化扩展和补充补全均限定在 StarRocks 语言范围内。

- 插件市场：[StarRocks Support][marketplace]
- 语法覆盖进度：[`SYNTAX_COVERAGE.zh-CN.md`](SYNTAX_COVERAGE.zh-CN.md)

## 功能

- 注册独立的 StarRocks SQL 方言、DBMS 和数据源类型。
- 使用 JFlex 生成的解析 Lexer 和 Grammar-Kit 生成的 Parser 解析 StarRocks SQL。
- 标准 SQL 结构尽量复用 JetBrains SQL PSI 类型，使平台格式化、引用、解析和数据库感知编辑能力可以继续工作。
- 高亮 StarRocks 关键字、标识符、字面量、注释、参数和运算符。
- 向 SQL 平台发布 StarRocks 关键字、标量类型、复杂类型和较完整的内置函数目录。
- 为 StarRocks 专属片段和属性提供补充补全；普通表、列、类型和函数补全仍由平台 SQL 补全系统负责。
- 基于平台 SQL 格式化器提供 StarRocks SQL 格式化支持。
- 将 JDBC 元数据、StarRocks 类型系统和原生 `SHOW CREATE` 语句接入 Database Tools 平台。
- 数据源按 `catalog → database → table/view` 同步，普通视图和物化视图统一放入 View 分组；保留列元数据，不查询 JDBC 外键、索引、键约束、存储过程和用户定义类型。

预定义驱动默认选择 [StarRocks 官方文档推荐](https://docs.starrocks.io/docs/integrations/JDBC_driver/)的 StarRocks Connector/J 的 **Latest stable**。它从可用制品列表中选择最新稳定版本；插件目前收录 `com.starrocks:starrocks-connector-j:1.1.1`，后续版本仍需更新该列表，并非自动查询 Maven Central 的最新发布。原生选择器在同一连接上依次调用 JDBC `setCatalog` 和 `setSchema`，由该驱动独立执行 `SET CATALOG` 和 `USE`，不需要多语句配置或自定义远程 JDBC helper。

已有数据源升级时，请重启 IDE 并更新 StarRocks 驱动：下载 StarRocks Connector/J 制品，移除 MySQL Connector/J 驱动文件，设置驱动类为 `com.starrocks.cj.jdbc.Driver`，将 URL 前缀从 `jdbc:mysql:` 改成 `jdbc:starrocks:`，例如 `jdbc:starrocks://host:9030/default_catalog.ads`。保存过自定义配置的驱动可能需要恢复预定义默认值，或手动更新。删除旧的 `databaseTerm` 和此前插件添加的 `allowMultiQueries` 配置，保持 `useLocalSessionState=false`，避免跨 catalog 选择同名数据库时跳过 `USE`。之后断开、重新连接并完整刷新数据源。插件不会静默迁移已有 URL 或驱动文件。

DataGrip 2026.1 的标准远程 `setSchema` 路径可能吞掉选库错误。若工具栏与查询行为不一致，请在同一会话执行 `SELECT @@catalog, DATABASE()` 核对；插件加载当前命名空间时读取服务端状态，但不接管平台的错误界面。

同步通过 `SHOW CATALOGS`、`SHOW DATABASES FROM catalog`、`SHOW FULL TABLES` 和 `SHOW FULL COLUMNS` 读取可见对象，不依赖外部 catalog 的 `information_schema.tables/columns` SELECT 权限。

项目通过结构化清单记录语法支持，不声明已经完全兼容 StarRocks。当前目标基准为 StarRocks 4.1 文档。已实现、部分实现、缺失和待核对内容见[语法覆盖清单](SYNTAX_COVERAGE.zh-CN.md)。

## 架构

Parser 使用生成式语法架构：

```text
grammar/starrocks-keywords.txt
        -> 生成关键字和 Token 注册表

grammar/starrocks.flex
        -> JFlex Parser Lexer

grammar/starrocks.bnf
        -> Grammar-Kit Parser

生成 Lexer + 生成 Parser
        -> DataGrip SQL Parser Adapter
        -> JetBrains SQL PSI / Formatter / Resolve / Completion
        -> StarRocks Database Services
```

主要设计约束：

- `grammar/starrocks.flex` 是解析词法的事实来源。
- `grammar/starrocks.bnf` 是解析语法的事实来源。
- `StarRocksParser` 只负责适配 JetBrains SQL Parser 钩子，语法逻辑属于生成 Parser。
- 标准 SQL 节点尽可能映射到平台 SQL PSI。
- StarRocks 专属能力通过显式 PSI 映射、Formatter Helper、补充补全和数据库服务实现。
- 不保留手写的整语句 Parser 兜底路径。

不得手动修改 `build/generated` 下的生成代码。应修改语法或关键字目录，然后重新运行生成任务。

## 项目结构

```text
grammar/
  starrocks.flex              Parser Lexer 语法
  starrocks.bnf               Parser 语法与 PSI Element 映射
  starrocks-keywords.txt      标准关键字目录

src/main/kotlin/.../
  lang/                       Parser Adapter、Lexer 门面、Element 映射
  dialect/                    方言标识和函数目录
  highlight/                  语法高亮
  format/                     SQL 平台格式化扩展
  completion/                 StarRocks 专属补充补全
  database/                   DBMS、类型、元数据和 DDL 集成

src/test/kotlin/              Parser、PSI、Resolve、Formatter 和集成测试
src/testData/sql/             SQL 验收与回归场景
```

## 环境要求

- JDK 17
- 使用项目自带的 Gradle Wrapper，当前版本为 Gradle 9.6.1
- 运行环境为 DataGrip 2026.1 或更高版本（`sinceBuild = 261`）
- 默认开发、测试和插件验证平台为 DataGrip 2026.1.4
  （`261.26222.86`）；可通过
  `intellijPlatformVersion` 或 `intellijPlatformLocalPath` 覆盖

## 构建与验证

编译插件：

```powershell
.\gradlew.bat compileKotlin --no-daemon
```

验证 Lexer 和 Grammar 源文件：

```powershell
.\gradlew.bat validateGrammarSources --no-daemon
```

修改语法时，可显式生成 Parser 资产：

```powershell
.\gradlew.bat generateLexer generateParser --no-daemon
```

运行 StarRocks SQL 场景校验：

```powershell
.\gradlew.bat validateStarRocksScenarios --no-daemon
```

运行完整验证：

```powershell
.\gradlew.bat check --no-daemon
```

构建可分发的插件 ZIP：

```powershell
.\gradlew.bat buildPlugin --no-daemon
```

ZIP 输出到 `build/distributions/`。

## 开发

可以使用仓库中的运行配置，在 IntelliJ IDEA 或 DataGrip 中启动沙箱 IDE：

```text
.run/Run IDE with Plugin.run.xml
```

也可以通过命令行启动：

```powershell
.\gradlew.bat runIde --no-daemon
```

新增语法支持时：

1. 对照目标 StarRocks 版本的官方文档确认语法。
2. 修改 `starrocks.flex`、`starrocks.bnf` 或标准关键字目录。
3. 增加针对性 Parser 测试；引入新语句族时增加场景 Fixture。
4. 对包含数据库对象的语法检查 PSI 映射、格式化和引用解析。
5. 更新语法覆盖清单。
6. 运行语法校验、场景校验和完整测试。

## 许可证

本项目使用 Apache License 2.0，详见 [`LICENSE`](LICENSE)。

[marketplace]: https://plugins.jetbrains.com/plugin/32243-starrocks-support

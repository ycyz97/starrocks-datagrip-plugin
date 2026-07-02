目标 1：稳定平台 SQL 基线
StarRocksLexer 输出平台可识别的 SQL token，尤其是 SELECT/FROM/WHERE/LIMIT/INSERT/WITH/SET 等具体 keyword token。
StarRocksParser 基于 SqlParser，并按其他成熟方言方式委托 StarRocksGeneratedParser / StarRocksDmlParsing / StarRocksDdlParsing / StarRocksExpressionParsing，恢复语句识别、运行 SQL 段、基础 PSI、基础 formatter。
验收：打开大 SQL 文件不卡死；IDE 能自动识别可运行 SQL 段；普通 SELECT/INSERT/WITH 能被平台识别为 SQL statement。
目标 2：清理轻量 parser 遗留
停止继续扩展 StarRocksExpressionParser / SegmentParser / StatementClassifier。
将依赖旧自研 PSI 的测试改成平台 SQL PSI 或用户可见行为测试。
暂时保留必要代码，但标记为迁移对象。
验收：核心测试不再依赖旧的 STARROCKS_DML_STATEMENT 等轻量 parser 节点。
目标 3：恢复平台 formatter
普通 SQL 走平台 SQL formatter。
StarRocks 尚未支持的复杂 DDL 先稳定不破坏格式。
删除或收缩用来弥补 grammar 缺失的 formatter hack。
验收：select * from t where a=1 能正常格式化；复杂 StarRocks DDL 不被错误缩进。
目标 4：修正高亮链路
parsing lexer 与 highlighting lexer 分清职责。
keyword、function、datatype 使用正确 token/color category。
不用高亮 token 破坏 parser token。
验收：关键字、函数、数据类型颜色区分正常；括号、运算符、字符串等基础高亮正常。
目标 5：补 StarRocks generated grammar 骨架
按 JetBrains 方言结构准备：StarRocksGeneratedParser
StarRocksDmlParsing
StarRocksDdlParsing
StarRocksExpressionParsing
StarRocksElementTypes
StarRocksElementFactory

先覆盖高频语法：QUALIFY、UNNEST、复杂类型、CREATE TABLE ... PROPERTIES、物化视图。
验收：StarRocks 特有语法进入结构化 PSI，而不是靠字符串/formatter/annotator 补救。
目标 6：重建 resolve / completion
基于平台 SQL PSI + StarRocks grammar 节点重建本地上下文。
支持同文件未执行 CREATE TABLE 被后续 INSERT/SELECT 解析。
completion 从稳定 PSI 上恢复关键字、函数、表名、列名、属性补全。
验收：本地表/列/CTE/alias 能解析；补全不会依赖旧轻量 parser。
目标 7：数据库集成完善
Generic database 组件可临时保留，但 StarRocks 专属行为逐步补齐。
优先完善 type system、definition provider、SHOW CREATE、driver/dbms 元信息。
验收：数据源连接、DDL 查看、类型识别、对象展示符合 StarRocks。

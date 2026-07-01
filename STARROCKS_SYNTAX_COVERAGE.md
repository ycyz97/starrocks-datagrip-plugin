# StarRocks Syntax Coverage Matrix

This matrix is the rewrite branch's parser coverage target. The goal is broad
StarRocks SQL support, not isolated fixes for a few known red-underlined cases.

Primary syntax references:

- StarRocks official FE grammar and parser source.
- StarRocks SQL reference documentation.
- StarRocks source tests for syntax cases that are not fully documented.
- JetBrains dialect implementations only as platform integration references.

## Coverage Families

| Family | Scope | Initial status |
| --- | --- | --- |
| Query | `SELECT`, `WITH`, joins, subqueries, set operations, window functions, `QUALIFY`, grouping extensions, table functions | Fixture started |
| Expressions | literals, operators, predicates, `CAST`, case expressions, parameters, lambda-like forms when supported | Fixture started |
| Types | scalar types, decimal variants, complex types, `JSON`, `BITMAP`, `HLL` | Fixture started |
| DML | `INSERT`, `INSERT OVERWRITE`, `UPDATE`, `DELETE`, `MERGE` | Fixture started |
| Table DDL | database/table/view DDL, key models, partitions, distribution, indexes, properties | Fixture started |
| Materialized Views | create, alter, refresh, cancel refresh, show create | Fixture started |
| Catalog and Resource | external catalog, resource, properties, alter/drop/show statements | Fixture started |
| Load and Export | load, routine load, pipe, export, cancel statements | Fixture started |
| Task and Analyze | task submission, scheduled tasks, analyze, statistics statements | Fixture started |
| Backup and Restore | repository, backup, restore, snapshot, recover statements | Fixture started |
| Administration | admin, show, kill, sync, set/unset and operational statements | Fixture started |
| Functions | official scalar, aggregate, analytic, table, bitmap, array, JSON functions | Fixture started |
| Local Resolution | local unexecuted DDL, CTE, alias, table, column, function resolution | Fixture started |
| Formatting | idempotent formatter rules after PSI is stable | Deferred |
| Completion | keyword, function, snippet, type, property completion after PSI is stable | Deferred |

## Fixture Layout

Fixtures live under `src/testData/sql` and are grouped by syntax family:

- `query/`
- `ddl/`
- `dml/`
- `types/`
- `functions/`
- `admin/`

`src/testData/sql/scenarios.properties` is the manifest that maps fixture files
to milestones and required feature flags.

---

# StarRocks 语法覆盖矩阵

该矩阵是重构分支的 parser 覆盖目标。目标是尽量覆盖 StarRocks SQL，而不是只修几个
已知报红场景。

主要语法依据：

- StarRocks 官方 FE grammar 和 parser 源码。
- StarRocks SQL reference 文档。
- StarRocks 源码测试，用于补充文档未完整覆盖的语法。
- JetBrains 其他方言实现只作为平台集成方式参考。

## 覆盖语法族

| 语法族 | 范围 | 初始状态 |
| --- | --- | --- |
| 查询 | `SELECT`、`WITH`、join、子查询、集合运算、窗口函数、`QUALIFY`、分组扩展、表函数 | 已开始 fixture |
| 表达式 | 字面量、运算符、谓词、`CAST`、case 表达式、参数、官方支持的 lambda 类形式 | 已开始 fixture |
| 类型 | 标量类型、decimal 变体、复杂类型、`JSON`、`BITMAP`、`HLL` | 已开始 fixture |
| DML | `INSERT`、`INSERT OVERWRITE`、`UPDATE`、`DELETE`、`MERGE` | 计划中 |
| 表 DDL | 库/表/视图 DDL、表模型、分区、分桶、索引、属性 | 已开始 fixture |
| 物化视图 | create、alter、refresh、cancel refresh、show create | 已开始 fixture |
| Catalog 和 Resource | external catalog、resource、properties、alter/drop/show 语句 | 已开始 fixture |
| Load 和 Export | load、routine load、pipe、export、cancel 语句 | 已开始 fixture |
| Task 和 Analyze | task submission、定时任务、analyze、统计信息语句 | 已开始 fixture |
| Backup 和 Restore | repository、backup、restore、snapshot、recover 语句 | 计划中 |
| 管理语句 | admin、show、kill、sync、set/unset 和运维语句 | 已开始 fixture |
| 函数 | 官方 scalar、aggregate、analytic、table、bitmap、array、JSON 函数 | 已开始 fixture |
| 本地解析 | 未执行 DDL、CTE、别名、表、列、函数解析 | 已开始 fixture |
| 格式化 | PSI 稳定后实现幂等 formatter 规则 | 延后 |
| 补全 | PSI 稳定后实现关键字、函数、片段、类型、属性补全 | 延后 |

## Fixture 目录

Fixture 放在 `src/testData/sql` 下，并按语法族分组：

- `query/`
- `ddl/`
- `dml/`
- `types/`
- `functions/`
- `admin/`

`src/testData/sql/scenarios.properties` 是 fixture manifest，用于把 fixture 文件映射到
里程碑和必备 feature 标记。

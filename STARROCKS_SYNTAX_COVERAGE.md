# StarRocks Syntax Coverage Matrix

This matrix tracks the parser coverage target for the StarRocks Grammar-Kit
grammar. The goal is broad StarRocks SQL support, not isolated fixes for a few
known red-underlined cases.

Primary syntax references:

- StarRocks official FE grammar and parser source.
- StarRocks SQL reference documentation.
- StarRocks source tests for syntax cases that are not fully documented.
- JetBrains dialect implementations as platform integration references.

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

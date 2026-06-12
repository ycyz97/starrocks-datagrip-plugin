# StarRocks Syntax and Function Sources

This plugin should add StarRocks-specific syntax, keywords, and functions only when they are backed by StarRocks official documentation, StarRocks source code, or StarRocks official tests.

## Confirmed v1 References

- StarRocks SQL reference, array function `UNNEST`: `https://docs.starrocks.io/docs/sql-reference/sql-functions/array-functions/unnest/`
- StarRocks query guide, Lateral Join: `https://docs.starrocks.io/docs/using_starrocks/Lateral_join/`

## Current Decisions

- `UNNEST` is treated as a StarRocks table function.
- `LATERAL` is highlighted only in confirmed `JOIN LATERAL UNNEST(...)` context.
- `LATERAL VIEW` and `explode` are not added because they are not confirmed StarRocks syntax in the references above.

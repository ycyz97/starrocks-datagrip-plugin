# StarRocks DataGrip 方言生成式语法架构

## 目标

将方言语法层从手写 `PsiBuilder` 解析逻辑改为以 JFlex 和 Grammar-Kit 为核心的生成式架构。

长期维护原则：

- JFlex 负责 parser lexer。
- Grammar-Kit `.bnf` 负责 parser grammar。
- DataGrip SQL adapter 保留，用于承接 DataGrip 平台的 SQL parser 钩子。
- 手写代码只做平台 glue、PSI 映射、formatter、resolve、completion、database services。
- 不保留旧手写 parser fallback。
- 新增语法必须优先修改 `.flex` / `.bnf`，不能继续堆手写 `PsiBuilder` parser。

## 总体架构

```text
官方 StarRocks 语法事实源
  -> JFlex lexer grammar
  -> Grammar-Kit parser grammar
  -> DataGrip SQL parser adapter
  -> PSI / formatter / resolve / completion / database services
```

核心分层：

```text
grammar/
  starrocks.flex
  starrocks.bnf

generated/
  lexer/
  parser/
  psi-types/

dialect/
  dialect identity
  keyword registry
  function registry

parser/
  DataGrip SQL parser adapter
  generated parser facade

psi/
  element types
  element factory
  named elements
  references
  stubs

formatter/
  formatting model
  block factory
  spacing and wrapping rules

completion/
  keyword completion
  function completion
  database object completion

resolve/
  table alias resolve
  column resolve
  CTE resolve
  window resolve

database/
  DBMS registration
  type system
  introspection
  definition provider
  script generator

tests/
  lexer tests
  parser tree tests
  scenario tests
  formatter tests
  completion and resolve tests
```

## Lexer 层

`starrocks.flex` 生成主解析 lexer。

要求：

- parser lexer 必须由 JFlex 生成。
- token 应尽量复用 IntelliJ SQL 平台 token。
- 标识符、字符串、数字、注释、括号、操作符、分号等基础 token 不应重新造一套不兼容 token。
- StarRocks 特有关键字可以映射到 dialect keyword token。
- 高亮可以有单独 highlighting lexer 或 wrapper，但主解析 lexer 不应继续依赖手写 lexer。

## Parser 层

`starrocks.bnf` 是语法主资产。

必须提供这些入口规则：

```text
script
statement
query_expression
value_expression
type_element
cast_type
table_column_list
analytic_clause
```

要求：

- 标准 SQL 结构优先映射到 IntelliJ SQL 平台 element type。
- StarRocks 特有结构使用自定义 element type。
- `pin` 和 `recoverWhile` 必须在 grammar 中显式建模。
- 禁止用“吃到分号”作为主要解析策略。
- 禁止 broad scanner 作为语法兜底。
- 旧手写 DML / DDL / Expression / Other parser 只能作为迁移参考，不能作为运行 fallback。

## DataGrip Parser Adapter

ParserDefinition 不应直接绕开 DataGrip SQL adapter。

正确入口形态：

```text
ParserDefinition
  -> createLexer(): JFlex generated lexer
  -> createParser(): DataGrip SQL parser adapter

DataGrip SQL parser adapter
  -> parse script: generated script rule
  -> parse SQL statement: generated statement rule
  -> parse query expression: generated query_expression rule
  -> parse data type: generated type_element rule
  -> parse cast data type: generated cast_type rule
  -> parse value expression: generated value_expression rule
  -> parse table column list: generated table_column_list rule
```

原因：

- DataGrip 的 SQL parser adapter 不只是普通 `PsiParser`。
- 它承接 query、type、value-expression、cast type、function tail、injection 等 SQL 平台钩子。
- 直接让 generated parser 裸跑会增加 formatter、resolve、completion、SQL injection 的回归风险。

## PSI 策略

PSI 不应全量自定义。

规则：

- 标准 SQL 节点优先复用平台 SQL PSI。
- StarRocks 独有节点才使用自定义 PSI。
- ElementFactory 负责把 generated element type 映射到平台 PSI 或自定义 PSI。
- formatter、resolve、completion 应依赖稳定 PSI，不直接依赖 token 流。
- generated element type 命名和 PSI 映射必须稳定，避免每次生成导致下游功能重写。

## Formatter / Resolve / Completion

这些模块不由 Grammar-Kit 自动解决，需要继续手写平台逻辑。

要求：

- formatter 基于 generated AST 和 SQL PSI block 体系适配。
- resolve 基于表别名、CTE、窗口名、列引用等 PSI 节点实现。
- completion 基于 dialect keywords、functions、database objects 和当前位置 PSI 实现。
- 迁移后如果 PSI 形状变化，优先修 PSI 映射，而不是回头添加手写 parser fallback。

## 构建链路

构建流程：

```text
generateLexer
  -> JFlex

generateParser
  -> Grammar-Kit

compile
  -> include generated sources

check
  -> compile + lexer tests + parser scenarios + formatter/resolve/completion tests
```

生成代码管理原则：

- 生成代码不可手改。
- 如果生成代码纳入版本库，必须能通过生成任务复现。
- 如果生成代码不纳入版本库，CI 必须在编译前生成。

## 直接替换路线

1. 建立 JFlex + Grammar-Kit 生成链路。
2. 编写完整 parser lexer token 映射。
3. 编写 grammar root、statement、query、expression、type、DDL、DML、Other 规则。
4. 接入 DataGrip SQL parser adapter。
5. 让主 parser 只委托 generated rules。
6. 删除旧手写 parser 运行路径。
7. 修正 PSI 映射。
8. 修正 formatter、resolve、completion。
9. 用测试补齐回归保护。

## 验收标准

- 主 parser 只走 generated grammar。
- 主 parser lexer 只走 JFlex generated lexer。
- ParserDefinition 仍通过 DataGrip SQL parser adapter 暴露 parser。
- Adapter 内部不再调用旧手写 parsing objects。
- 不存在旧 parser fallback。
- 不存在 broad scanner 作为语法兜底。
- 新增语法必须改 `.bnf` / `.flex`。
- lexer、parser scenario、formatter、resolve、completion 核心测试通过。

## 主要风险

最高风险区域：

- query expression
- expression precedence
- function call
- CAST and complex type
- subquery
- join
- CREATE TABLE
- PSI element type compatibility
- formatter block compatibility
- resolve and completion behavior

风险处理原则：

- 用测试锁定现有行为。
- 生成 parser 有问题时优先修 grammar、pin、recover、PSI 映射。
- 不通过恢复旧手写 parser 来绕过问题。

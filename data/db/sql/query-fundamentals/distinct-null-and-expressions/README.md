## DB / SQL / Query Fundamentals / DISTINCT, NULL & Expressions

### 1. DISTINCT & Set Semantics

`DISTINCT` removes duplicate rows from the full selected column list, not
per-column -- `SELECT DISTINCT a, b` dedupes on the `(a, b)` pair, not `a`
and `b` separately. It also forces a sort or hash step, so it's not free;
`EXISTS`/`GROUP BY` are often cheaper ways to express the same intent.

~~~sql
SELECT DISTINCT city, country FROM customers
-- dedupes on the (city, country) pair, not city alone
~~~

### 2. NULL Comparison Semantics

`NULL` means "unknown," so `NULL = NULL` evaluates to `NULL` (not `TRUE`)
and gets filtered out by `WHERE` -- you need `IS NULL`/`IS NOT NULL`
instead. This trips people up in `NOT IN` subqueries too: one `NULL` in the
subquery's results silently makes the whole `NOT IN` match nothing.

~~~sql
WHERE column IS NULL          -- correct
WHERE column = NULL           -- always unknown, matches nothing
~~~

### 3. CASE Expressions & COALESCE/NULLIF

`CASE` is SQL's inline if/else, usable anywhere an expression is allowed
(`SELECT`, `WHERE`, `ORDER BY`). `COALESCE(a, b, ...)` returns the first
non-null argument -- a shorthand default-value `CASE`. `NULLIF(a, b)`
returns `NULL` if `a = b`, handy for turning sentinel values into `NULL`.

~~~sql
SELECT COALESCE(nickname, first_name) AS display_name FROM users
~~~

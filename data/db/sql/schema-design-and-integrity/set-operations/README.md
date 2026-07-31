## DB / SQL / Schema Design & Integrity / Set Operations

### 1. UNION vs UNION ALL

`UNION` merges two result sets and removes duplicate rows, which means an
implicit sort/hash-dedupe pass; `UNION ALL` keeps every row including
duplicates and skips that pass entirely. Default to `UNION ALL` unless
duplicates are actually a problem -- `UNION`'s dedupe cost is easy to pay
without noticing.

~~~sql
SELECT id FROM active_users
UNION ALL
SELECT id FROM archived_users
-- no dedupe needed: the two tables can't share ids
~~~

### 2. INTERSECT/EXCEPT

`INTERSECT` returns rows present in both result sets; `EXCEPT` (aka
`MINUS` in Oracle) returns rows in the first set but not the second. Both
require matching column count/types across the two queries, same as
`UNION`, and both dedupe by default.

~~~sql
SELECT customer_id FROM orders_2023
EXCEPT
SELECT customer_id FROM orders_2024
-- customers who ordered in 2023 but not 2024
~~~

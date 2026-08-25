# DB / SQL / Query Fundamentals / SELECT Execution Order & Filtering

## 1. Logical Query Processing Order

SQL reads like `SELECT ... FROM ... WHERE ... GROUP BY ... HAVING ...`, but
the engine evaluates it almost backwards: `FROM` → `WHERE` → `GROUP BY` →
`HAVING` → `SELECT` → `ORDER BY`. This is why a column alias defined in
`SELECT` can't be used in `WHERE` -- `WHERE` runs before `SELECT` exists.

```sql
SELECT price * qty AS total
FROM orders
WHERE price * qty > 100  -- can't say "WHERE total > 100" here
```

## 2. WHERE vs HAVING

`WHERE` filters rows before grouping, `HAVING` filters groups after
aggregation -- so `HAVING` can reference `COUNT()`/`SUM()` and `WHERE`
can't. Putting a non-aggregate filter in `HAVING` still works but wastes
work grouping rows that `WHERE` could have discarded first.

```sql
SELECT customer_id, COUNT(*) AS orders
FROM orders
WHERE status = 'PAID'        -- filters rows first (cheap)
GROUP BY customer_id
HAVING COUNT(*) > 5          -- filters groups after (needs aggregate)
```

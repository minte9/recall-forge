# DB / Oracle / SQL Dialect & Syntax / DUAL & ROWNUM

## 1. SELECT FROM DUAL

Oracle requires every `SELECT` to reference a table -- there's no bare
`SELECT 1` like MySQL or Postgres allow. `DUAL` is a built-in one-row,
one-column table that exists purely so an expression can be evaluated
without needing a real table.

```sql
SELECT SYSDATE FROM DUAL;
SELECT 1 + 1 FROM DUAL;
```

## 2. ROWNUM & FETCH FIRST

`ROWNUM` is assigned to a row *before* `ORDER BY` runs, so `WHERE ROWNUM
<= 10` after an `ORDER BY` doesn't reliably give the top 10 -- it needs
wrapping in a subquery to order first, then apply `ROWNUM` to the already-
sorted result. `FETCH FIRST n ROWS ONLY` (standard SQL, since Oracle 12c)
avoids the trap entirely.

```sql
-- correct top-10: order first, then limit
SELECT * FROM (SELECT * FROM orders ORDER BY total DESC)
WHERE ROWNUM <= 10;
-- or, since 12c:
SELECT * FROM orders ORDER BY total DESC FETCH FIRST 10 ROWS ONLY;
```

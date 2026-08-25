# DB / PostgreSQL / SQL Dialect & Syntax / DISTINCT ON & generate_series

## 1. DISTINCT ON (expr)

`DISTINCT ON (col)` keeps only the first row per distinct value of `col`,
according to `ORDER BY` -- a "top 1 per group" query in one line, without
a window function or a self-join. `ORDER BY` must start with the same
column(s) as `DISTINCT ON`, since that's what determines which row counts
as "first".

```sql
SELECT DISTINCT ON (customer_id) customer_id, id, total
FROM orders
ORDER BY customer_id, created_at DESC;  -- latest order per customer
```

## 2. generate_series()

`generate_series(start, stop, step)` produces a set of rows without
querying any table -- generating a sequence of numbers or dates on the
fly. Common uses: filling gaps in a report (dates with zero orders still
need a row), or generating test data.

```sql
SELECT d::date FROM generate_series(
  '2024-01-01'::date, '2024-01-07'::date, '1 day'
) AS d;
```

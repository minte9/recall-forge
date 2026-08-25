# DB / SQL / Aggregation & Window Functions / Grouping & Aggregates

## 1. GROUP BY & Aggregate Functions

`GROUP BY` collapses rows sharing the same value(s) into one row per
group, and every non-aggregated column in `SELECT` must appear in
`GROUP BY` (or be functionally dependent on it). `COUNT`, `SUM`, `AVG`,
`MIN`, `MAX` all ignore `NULL`s except `COUNT(*)`, which counts rows
regardless of `NULL`s.

```sql
SELECT department, AVG(salary) AS avg_salary, COUNT(*) AS n
FROM employees
GROUP BY department
```

## 2. ROLLUP & CUBE

`ROLLUP` adds subtotal rows for each prefix of the grouping columns plus a
grand total -- useful for reports that need per-group and overall sums in
one query instead of separate `UNION` queries. `CUBE` goes further,
producing subtotals for every combination of the grouping columns.

```sql
SELECT department, role, SUM(salary)
FROM employees
GROUP BY ROLLUP (department, role)
-- adds per-department subtotal rows + one grand-total row
```

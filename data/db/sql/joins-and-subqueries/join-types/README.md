# DB / SQL / Joins & Subqueries / Join Types

## 1. INNER vs OUTER Joins

`INNER JOIN` keeps only rows with a match on both sides. `LEFT JOIN` keeps
every row from the left table, filling unmatched right-side columns with
`NULL`; `RIGHT JOIN` mirrors that for the right table. `FULL JOIN` keeps
unmatched rows from both sides -- rarely used, but the natural fallback
when neither side is guaranteed to have a match.

```sql
SELECT c.name, o.id
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
-- customers with zero orders still appear, o.id is NULL
```

## 2. CROSS JOIN & Self-Join

`CROSS JOIN` produces the full Cartesian product (every left row paired
with every right row) -- usually accidental when a `JOIN` condition is
missing, but occasionally intentional (generating combinations). A
self-join joins a table to itself via an alias, typically to compare rows
or walk a hierarchy (e.g. employee → manager).

```sql
SELECT e.name AS employee, m.name AS manager
FROM employees e
JOIN employees m ON e.manager_id = m.id
```

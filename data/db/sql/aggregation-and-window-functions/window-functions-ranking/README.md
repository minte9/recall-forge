# DB / SQL / Aggregation & Window Functions / Window Functions: Ranking

## 1. ROW_NUMBER / RANK / DENSE_RANK

All three number rows within a partition, but differ on ties: `ROW_NUMBER`
always assigns distinct sequential numbers (arbitrary tiebreak),
`RANK` gives tied rows the same number and skips the next ones (1,1,3),
`DENSE_RANK` gives tied rows the same number without skipping (1,1,2).
Picking the wrong one is a common off-by-one bug in "top N per group"
queries.

```sql
SELECT name, score,
  RANK() OVER (ORDER BY score DESC) AS rnk
FROM contestants
```

## 2. PARTITION BY & Frame Clauses

`PARTITION BY` resets the window function per group, the way `GROUP BY`
groups rows -- except window functions keep every row, not one per group.
The frame clause (`ROWS BETWEEN ...`) controls exactly which rows within
the partition each row's calculation sees, defaulting to "start of
partition through current row" when `ORDER BY` is present.

```sql
SELECT name, department, salary,
  ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC) AS rnk
FROM employees
-- rnk resets to 1 at the start of each department
```

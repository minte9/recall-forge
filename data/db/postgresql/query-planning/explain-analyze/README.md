# DB / PostgreSQL / Query Planning / EXPLAIN ANALYZE

## 1. Reading EXPLAIN ANALYZE

Plain `EXPLAIN` shows the planner's estimated plan without running the
query; `EXPLAIN ANALYZE` actually executes it and adds real timing and
row counts alongside the estimates -- indented lines are nested
operations, and the innermost (most indented) ones run first, feeding
their output up to their parent.

```sql
EXPLAIN ANALYZE SELECT * FROM orders WHERE customer_id = 5;
```

## 2. Estimated vs Actual Rows

A large gap between a node's estimated and actual row count is the
single most useful signal in an `EXPLAIN ANALYZE` output -- it means the
planner's statistics were wrong for that predicate, which can cascade
into a bad plan choice upstream (e.g. picking a nested loop join
expecting 10 rows when 100,000 actually matched).

```sql
-- Seq Scan on orders (rows=10 ... actual rows=98214)
-- big gap: stats are stale or the predicate is hard to estimate
```

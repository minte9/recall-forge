# DB / PostgreSQL / Indexing / Index Types Overview

## 1. B-Tree Default

`CREATE INDEX` without an explicit method creates a B-tree, the right
default for equality and range queries on scalar values (`=`, `<`, `>`,
`BETWEEN`, sorting) -- the same structure most other relational engines
default to as well, so B-tree intuition transfers directly.

```sql
CREATE INDEX idx_orders_customer ON orders (customer_id);
```

## 2. When to Reach for a Non-B-Tree Index

Postgres ships several specialized index types for data B-tree isn't
built for: GIN for containment queries on JSONB/arrays/full-text, GiST
for geometric and range-overlap queries, BRIN for very large tables
where rows are naturally ordered on disk (like a timestamp column on an
append-only log table).

```sql
CREATE INDEX idx_logs_time ON logs USING BRIN (created_at);
```

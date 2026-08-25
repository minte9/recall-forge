# DB / MySQL / Indexing / Composite Indexes & EXPLAIN

## 1. The Leftmost Prefix Rule

A composite index on `(a, b, c)` can be used for lookups on `a`, on
`(a, b)`, or on `(a, b, c)` -- but not on `b` alone or `(b, c)` alone,
since the index is physically sorted by `a` first. Column order in the
index definition should match the most common query patterns, not just
list every filtered column.

```sql
CREATE INDEX idx_status_date ON orders (status, created_at);
-- usable for: WHERE status = ?
-- usable for: WHERE status = ? AND created_at > ?
-- NOT usable for: WHERE created_at > ? (alone)
```

## 2. Reading EXPLAIN Output

`EXPLAIN` shows, per table, `type` (access method -- `ALL` is a full
scan, `ref`/`range` use an index), `key` (which index MySQL actually
picked), `rows` (estimated rows examined), and `Extra` (flags like
`Using filesort` or `Using temporary`, both signs of expensive work an
index could often eliminate).

```sql
EXPLAIN SELECT * FROM orders WHERE status = 'PAID';
```

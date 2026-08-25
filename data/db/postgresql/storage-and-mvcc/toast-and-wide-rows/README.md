# DB / PostgreSQL / Storage & MVCC / TOAST & Wide Rows

## 1. TOAST for Large Values

Postgres pages are a fixed size (8KB by default), so a value too large to
fit is compressed and/or moved into a separate TOAST table, leaving a
small pointer in the original row -- The Oversized-Attribute Storage
Technique. This happens automatically for `TEXT`, `JSONB`, `BYTEA`, and
similar variable-length types once a value crosses the threshold.

```sql
SELECT relname, reltoastrelid FROM pg_class WHERE relname = 'orders';
```

## 2. When Wide Rows Hurt

A table with many large columns -- even ones rarely queried -- means every
sequential scan reads more data per row, and every TOASTed value accessed
costs an extra lookup into the TOAST table. Splitting rarely-accessed
large columns into a separate table (accessed only when needed) keeps the
hot table's rows narrow and cache-friendly.

```sql
-- keep large/rare columns out of the hot table
CREATE TABLE order_notes (order_id INT PRIMARY KEY, notes TEXT);
```

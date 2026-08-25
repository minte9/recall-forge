# DB / Oracle / Concurrency & High Availability / Read Consistency via Undo

## 1. MVCC via Undo (No Dirty Reads by Default)

Oracle never lets a reader see another transaction's uncommitted changes
-- there's no `READ UNCOMMITTED` isolation level at all. A reader whose
query started before a concurrent update commits sees the pre-change
version, reconstructed from undo, for the entire duration of that query
(statement-level read consistency).

```sql
-- default READ COMMITTED: every statement gets its own consistent snapshot
SELECT balance FROM accounts WHERE id = 1;
```

## 2. ROWID & Consistent Gets

A `ROWID` is the physical address of a row (file, block, row-in-block) --
the fastest possible way to revisit a specific row, which is exactly what
Oracle uses internally to apply undo and reconstruct an older version of
a block for a consistent read. A "consistent get" (visible in execution
statistics) is a block read that had to be rolled back to an earlier
version to satisfy this.

```sql
SELECT ROWID, name FROM customers WHERE id = 1;
```

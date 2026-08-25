# DB / MySQL / Storage Engines / MVCC & Buffer Pool

## 1. MVCC & Read Views

InnoDB gives each transaction a consistent snapshot (a "read view") of the
database at a point in time, built from undo log versions of rows changed
since then -- readers never block writers and writers never block readers,
because a reader can always fall back to an older row version instead of
waiting on a lock.

```sql
-- REPEATABLE READ: read view fixed at first read, unaffected by
-- other transactions' commits for the rest of this transaction
START TRANSACTION;
SELECT balance FROM accounts WHERE id = 1;
```

## 2. The Buffer Pool

The buffer pool caches data and index pages in memory, so most reads and
writes touch RAM, not disk -- disk I/O only happens on a cache miss or
when dirty pages are flushed. The doublewrite buffer writes pages to a
safe area before the real write, protecting against a torn page write if
the process crashes mid-write.

```sql
SHOW VARIABLES LIKE 'innodb_buffer_pool_size';
```

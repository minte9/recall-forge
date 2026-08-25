# DB / MySQL / Locking & Concurrency / Gap & Next-Key Locks

## 1. Gap Locks

A gap lock locks the space *between* index records, not a row itself --
it stops other transactions from inserting a new row into that gap. This
is InnoDB-specific machinery, not something most other engines implement
the same way, and it exists specifically to close a hole that plain row
locking leaves open.

```sql
-- locks the gap between id=5 and id=10, blocking inserts of id=7
SELECT * FROM orders WHERE id BETWEEN 5 AND 10 FOR UPDATE;
```

## 2. Next-Key Locking

A next-key lock is a row lock plus the gap immediately before it combined
-- InnoDB's default locking strategy for `REPEATABLE READ`. It's what
lets `REPEATABLE READ` in InnoDB prevent phantom reads (new rows matching
a prior query's condition appearing on re-run), unlike the ANSI standard's
weaker guarantee for that isolation level.

```sql
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
-- next-key locks here also block phantom inserts, not just row changes
```

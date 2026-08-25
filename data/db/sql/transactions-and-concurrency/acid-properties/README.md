# DB / SQL / Transactions & Concurrency / ACID Properties

## 1. ACID Properties

Atomicity: a transaction's statements all commit or all roll back, no
partial application. Consistency: a transaction moves the database from
one valid state to another, respecting constraints. Isolation: concurrent
transactions don't see each other's uncommitted changes (degree depends on
isolation level). Durability: once committed, changes survive a crash.

```sql
BEGIN;
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT;  -- both updates apply, or neither does
```

## 2. Atomicity & Durability in Practice

Atomicity is what makes a multi-table transfer safe to retry after a crash
mid-way -- either it never happened or it fully happened, never half.
Durability is usually implemented via a write-ahead log: the engine
persists the *intent* to disk before acknowledging commit, so it can
replay committed-but-not-yet-flushed changes after a crash.

```sql
ROLLBACK;  -- undoes every statement since BEGIN, atomically
```

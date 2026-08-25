# DB / SQL / Transactions & Concurrency / Locking Concepts

## 1. Pessimistic vs Optimistic Locking

Pessimistic locking acquires a lock before touching a row (`SELECT ... FOR
UPDATE`), blocking other transactions until release -- safe but reduces
concurrency and risks contention/deadlocks under load. Optimistic locking
skips the lock and instead checks a version/timestamp column at commit
time, retrying if it changed -- better throughput when conflicts are rare.

```sql
-- pessimistic: blocks other writers until this tx commits
SELECT * FROM accounts WHERE id = 1 FOR UPDATE;
```

## 2. Deadlocks (Generic Concept)

A deadlock happens when two transactions each hold a lock the other
needs, so neither can proceed -- the engine detects the cycle and kills
one transaction (the "victim") to let the other finish. The standard
mitigation is acquiring locks in a consistent order across all code paths,
so a cycle can't form in the first place.

```sql
-- tx A: locks account 1, then wants account 2
-- tx B: locks account 2, then wants account 1  -> deadlock
-- fix: both transactions lock in the same id order
```

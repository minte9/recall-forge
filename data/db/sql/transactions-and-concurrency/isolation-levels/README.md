# DB / SQL / Transactions & Concurrency / Isolation Levels

## 1. Isolation Levels

The SQL standard defines four levels, each preventing more anomalies at
the cost of concurrency: `READ UNCOMMITTED` (prevents nothing),
`READ COMMITTED` (no dirty reads), `REPEATABLE READ` (no dirty or
non-repeatable reads), `SERIALIZABLE` (no anomalies at all, transactions
behave as if run one at a time). Most engines default to `READ COMMITTED`.

```sql
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
```

## 2. Read Anomalies (Dirty/Non-Repeatable/Phantom)

A dirty read sees another transaction's uncommitted change. A
non-repeatable read sees a row's value change between two reads in the
same transaction because another transaction committed an update to it. A
phantom read sees new rows appear on a re-run query because another
transaction inserted rows matching the same filter -- `REPEATABLE READ`
stops the first two but not always phantoms, depending on the engine.

```sql
-- non-repeatable read: same SELECT, different result, same tx
SELECT balance FROM accounts WHERE id = 1;  -- reads 100
-- (another tx commits balance = 50 here)
SELECT balance FROM accounts WHERE id = 1;  -- reads 50
```

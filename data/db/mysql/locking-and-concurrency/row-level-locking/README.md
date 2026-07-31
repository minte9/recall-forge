## DB / MySQL / Locking & Concurrency / Row-Level Locking

### 1. Row Locks vs Table Locks

InnoDB locks individual index records, not whole tables, so unrelated
rows stay concurrently writable even inside overlapping transactions --
this is the main practical reason InnoDB replaced MyISAM as the default.
A statement with no usable index still falls back to locking every row it
scans, which can look like a table lock without being one.

~~~sql
-- with an index on customer_id: locks only matching rows
UPDATE orders SET status = 'CANCELLED' WHERE customer_id = 5;
~~~

### 2. Locking Reads

`SELECT ... FOR UPDATE` takes an exclusive lock on the rows read, blocking
other transactions from modifying (or also locking) them until commit --
used to read a value and safely update it later in the same transaction.
`SELECT ... LOCK IN SHARE MODE` (or `FOR SHARE`) takes a shared lock:
other readers can still share-lock the same rows, but no one can write.

~~~sql
SELECT balance FROM accounts WHERE id = 1 FOR UPDATE;
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
~~~

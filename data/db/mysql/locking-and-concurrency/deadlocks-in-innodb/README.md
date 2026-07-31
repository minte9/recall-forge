## DB / MySQL / Locking & Concurrency / Deadlocks in InnoDB

### 1. Deadlock Detection

InnoDB maintains a wait-for graph of which transactions are blocked on
which locks, and actively checks it for cycles -- unlike waiting for a
timeout, it detects a deadlock almost immediately and rolls back the
transaction with the smaller estimated impact (usually the one that has
changed fewer rows), returning an error to that transaction's client.

~~~sql
-- one of these two gets: ERROR 1213 (40001): Deadlock found
-- tx A: locks id=1, then wants id=2
-- tx B: locks id=2, then wants id=1
~~~

### 2. Diagnosing with SHOW ENGINE INNODB STATUS

This command's `LATEST DETECTED DEADLOCK` section shows both
transactions involved, the exact statements they were running, and which
locks each was holding versus waiting for -- the standard first step when
an application reports intermittent deadlock errors under load.

~~~sql
SHOW ENGINE INNODB STATUS\G
~~~

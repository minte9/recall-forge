## DB / MySQL / Storage Engines / InnoDB Fundamentals

### 1. Clustered Index & Table Organization

InnoDB stores every table as a B+tree ordered by primary key -- the PK
*is* the table's physical layout, not a separate structure pointing at
rows. This makes PK lookups and range scans fast, but it also means the
PK's data type and insert order directly affect page splits and write
performance.

~~~sql
CREATE TABLE orders (
  id INT PRIMARY KEY,  -- rows are physically stored in id order
  customer_id INT
)
~~~

### 2. ACID via Redo & Undo Logs

The redo log records physical changes before they're applied to data
pages, so a crash can replay committed-but-not-yet-flushed changes
(durability). The undo log stores the previous version of a row before a
change, used both to roll back an aborted transaction (atomicity) and to
serve older row versions to concurrent readers (MVCC).

~~~sql
-- on COMMIT: redo log ensures durability
-- on ROLLBACK: undo log restores the prior row version
~~~

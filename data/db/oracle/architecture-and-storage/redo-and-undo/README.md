## DB / Oracle / Architecture & Storage / Redo & Undo

### 1. Redo Logs (Durability)

Every change is written to the redo log buffer before the corresponding
data block is modified, and LGWR flushes that buffer to disk on commit --
this is what makes a commit durable: after a crash, Oracle replays the
redo logs to reconstruct changes that were committed but not yet written
to the datafiles.

~~~sql
SELECT group#, status, bytes FROM v$log;
~~~

### 2. Undo Tablespace (Rollback & Read Consistency)

Before a row is changed, its prior version is written to the undo
tablespace -- used to roll back an uncommitted transaction, and also to
give other sessions a consistent, pre-change view of the data while the
transaction is still in flight. This undo-based approach is how Oracle
implements MVCC without ever blocking a reader on a writer.

~~~sql
SELECT tablespace_name, status FROM dba_tablespaces
WHERE contents = 'UNDO';
~~~

## DB / MySQL / Replication & Scaling / Binlog & Replication Modes

### 1. Binlog Formats

The binary log records changes for replication and point-in-time
recovery, in one of three formats: statement-based (logs the SQL
statement itself, compact but can replay non-deterministically for
functions like `NOW()`), row-based (logs the actual changed row data,
larger but always deterministic), or mixed (statement-based by default,
falling back to row-based when a statement isn't safely deterministic).

~~~sql
SET GLOBAL binlog_format = 'ROW';
~~~

### 2. Async vs Semi-Sync Replication

Asynchronous replication (the default) lets the primary commit and
acknowledge the client without waiting for any replica to receive the
change -- fast, but a primary crash can lose the most recent commits.
Semi-synchronous replication waits for at least one replica to
acknowledge receipt (not necessarily apply) before the primary
acknowledges the commit, trading some latency for a stronger durability
guarantee.

~~~sql
SET GLOBAL rpl_semi_sync_master_enabled = 1;
~~~

## DB / PostgreSQL / Concurrency & Replication / Snapshot Isolation

### 1. Snapshot Isolation & Row Visibility

At `READ COMMITTED` (the default), each statement gets a fresh snapshot;
at `REPEATABLE READ`, the whole transaction shares one snapshot taken at
its first statement. A snapshot is really just a list of "transaction IDs
in progress at this moment" -- a tuple is visible if its `xmin` committed
before the snapshot and its `xmax` is either unset or didn't commit
before the snapshot.

~~~sql
BEGIN ISOLATION LEVEL REPEATABLE READ;
SELECT * FROM accounts WHERE id = 1;  -- snapshot fixed here
~~~

### 2. Why Postgres Has No Dirty Reads

Because visibility is decided per-tuple by comparing transaction IDs
against a snapshot, an uncommitted change is simply invisible to every
other transaction's snapshot -- there's no `READ UNCOMMITTED` isolation
level in Postgres at all; requesting it silently behaves as `READ
COMMITTED`.

~~~sql
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
-- behaves exactly like READ COMMITTED in Postgres
~~~

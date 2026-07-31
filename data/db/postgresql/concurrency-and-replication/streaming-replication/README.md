## DB / PostgreSQL / Concurrency & Replication / Streaming Replication

### 1. Physical Replication & WAL Shipping

Every change first gets written to the Write-Ahead Log before being
applied to data files -- streaming replication ships that same WAL
stream to one or more replicas, which replay it to stay byte-for-byte
identical to the primary. This is the same log used for crash recovery,
just shipped over the network instead of only read back locally.

~~~sql
SELECT client_addr, state, sent_lsn, replay_lsn FROM pg_stat_replication;
~~~

### 2. Replica Promotion

A streaming replica is read-only by default -- useful for scaling reads,
but it can't accept writes. Promoting it (`pg_ctl promote` or
`SELECT pg_promote()`) stops WAL replay and switches it to a normal,
writable primary -- the standard failover step when the original primary
goes down.

~~~sql
SELECT pg_promote();
~~~

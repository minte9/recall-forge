## DB / MySQL / Replication & Scaling / Read Replicas & Lag

### 1. Read/Write Splitting

Routing writes to the primary and reads to one or more replicas scales
read throughput horizontally without sharding -- typically done in the
application layer or a proxy (ProxySQL, a connection router), not by
MySQL itself. The tradeoff is that a replica's data always lags the
primary by some amount, however small.

~~~sql
-- application-level: writes -> primary, reads -> a replica connection
~~~

### 2. Causes of Replica Lag

A replica applies changes from the binlog on a single thread by default
(or a limited number with parallel replication), so a burst of writes on
the primary, a long-running query on the replica, or the replica simply
running on weaker hardware can all cause it to fall behind. `SHOW REPLICA
STATUS` reports `Seconds_Behind_Source`, the standard way to check.

~~~sql
SHOW REPLICA STATUS\G
-- Seconds_Behind_Source: how far behind the primary this replica is
~~~

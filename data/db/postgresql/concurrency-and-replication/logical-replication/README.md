## DB / PostgreSQL / Concurrency & Replication / Logical Replication

### 1. Publications & Subscriptions

Unlike streaming replication (byte-for-byte, whole-cluster), logical
replication ships decoded row-level changes for specific tables --
a `PUBLICATION` on the source marks what to replicate, a `SUBSCRIPTION`
on the target consumes it. The subscriber can be a different Postgres
major version, or even just a subset of tables, since it isn't full
binary replication.

~~~sql
-- on the source
CREATE PUBLICATION orders_pub FOR TABLE orders;
-- on the target
CREATE SUBSCRIPTION orders_sub CONNECTION '...' PUBLICATION orders_pub;
~~~

### 2. Use Cases (Selective Replication, Near-Zero-Downtime Upgrades)

Because it's table-level and cross-version, logical replication enables
things streaming replication can't: replicating only a few tables to a
reporting database, or running old and new major-version clusters side by
side during a major-version upgrade, cutting over once the subscriber has
caught up -- far less downtime than a dump/restore upgrade.

~~~sql
SELECT * FROM pg_stat_subscription;  -- check replication lag/status
~~~

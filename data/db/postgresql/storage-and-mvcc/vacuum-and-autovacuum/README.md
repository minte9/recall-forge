## DB / PostgreSQL / Storage & MVCC / VACUUM & Autovacuum

### 1. Why Dead Tuples Accumulate

Because updates and deletes leave the old tuple version in place, a
heavily written table steadily fills with dead tuples that still occupy
disk space and get scanned (and skipped) by queries -- `VACUUM` is what
reclaims that space for reuse, and without it, table and index bloat
grows unbounded.

~~~sql
VACUUM (VERBOSE, ANALYZE) accounts;
~~~

### 2. Tuning Autovacuum

Autovacuum runs `VACUUM`/`ANALYZE` automatically based on thresholds
(`autovacuum_vacuum_scale_factor`, a percentage of the table's row count
that must be dead before it triggers). Default thresholds are tuned for
small-to-medium tables; large, high-churn tables often need
per-table overrides so autovacuum runs more aggressively and doesn't fall
behind write volume.

~~~sql
ALTER TABLE orders SET (autovacuum_vacuum_scale_factor = 0.01);
~~~

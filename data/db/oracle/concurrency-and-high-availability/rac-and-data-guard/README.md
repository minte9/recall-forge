# DB / Oracle / Concurrency & High Availability / RAC & Data Guard

## 1. RAC for Clustering & Scale

Real Application Clusters runs multiple instances (on different servers)
against one shared database on shared storage -- unlike a typical
primary/replica setup, every RAC node can accept both reads and writes.
Cache Fusion synchronizes each node's buffer cache over a private
interconnect so nodes stay consistent without constantly hitting disk.

```sql
SELECT inst_id, instance_name, status FROM gv$instance;
```

## 2. Data Guard for Disaster Recovery

Data Guard maintains one or more standby copies of the database on
separate storage (often a separate site), kept current by shipping and
applying redo -- unlike RAC's shared storage, Data Guard's standbys are
independent copies, so a storage failure at the primary site doesn't
take the standby down with it. A standby can run in physical mode (exact
block-for-block copy) or logical mode (SQL-level apply).

```sql
SELECT database_role, protection_mode FROM v$database;
```

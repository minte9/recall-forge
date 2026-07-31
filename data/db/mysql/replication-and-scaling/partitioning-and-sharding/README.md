## DB / MySQL / Replication & Scaling / Partitioning & Sharding

### 1. Table Partitioning

MySQL can split one logical table into physical partitions by `RANGE`
(e.g. by date), `LIST` (by discrete values), `HASH`, or `KEY` -- queries
that filter on the partition key can skip scanning partitions entirely
("partition pruning"), but this is still a single MySQL instance, not a
way to scale beyond one server's capacity.

~~~sql
CREATE TABLE orders (id INT, order_date DATE)
PARTITION BY RANGE (YEAR(order_date)) (
  PARTITION p2023 VALUES LESS THAN (2024),
  PARTITION p2024 VALUES LESS THAN (2025)
);
~~~

### 2. Sharding Strategies

Sharding splits data across multiple independent MySQL instances (by
customer ID, region, hash of a key), scaling writes and total storage
beyond one machine -- but at real cost: cross-shard joins and
transactions become application-level problems, and resharding when a
shard grows unevenly is a significant migration.

~~~sql
-- application picks the shard, e.g.: shard = customer_id % num_shards
~~~

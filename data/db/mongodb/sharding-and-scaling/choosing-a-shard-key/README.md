## DB / MongoDB / Sharding & Scaling / Choosing a Shard Key

### 1. Cardinality & Monotonic Keys

A good shard key needs high cardinality (many distinct values, so chunks
can split finely) and even write distribution. A monotonically
increasing key (an auto-incrementing counter, or a plain timestamp) sends
every new write to whichever shard owns the current high end of the
range -- a "hot shard" that defeats the purpose of sharding for writes.

~~~javascript
// risky: all new writes target the same shard
sh.shardCollection('app.events', { createdAt: 1 })
~~~

### 2. Zone Sharding

Zone sharding pins specific shard key ranges to specific shards -- e.g.
keeping European customers' data on shards physically located in Europe
for latency or data-residency reasons, rather than letting the balancer
place chunks anywhere.

~~~javascript
sh.addShardToZone('shard-eu', 'EU');
sh.updateZoneKeyRange('app.customers',
  { region: 'EU', _id: MinKey }, { region: 'EU', _id: MaxKey }, 'EU');
~~~

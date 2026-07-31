## DB / MongoDB / Sharding & Scaling / Chunk Distribution & Balancing

### 1. Chunks & the Balancer

A shard key's range of values is split into chunks, each assigned to one
shard -- as chunks grow too large or a shard accumulates too many
relative to others, the balancer automatically migrates chunks between
shards to keep the distribution even, running as a background process
with minimal manual intervention.

~~~javascript
sh.getBalancerState()
db.chunks.find({ ns: 'app.orders' }).count()  // (from config db)
~~~

### 2. Hashed vs Ranged Sharding

Ranged sharding keeps documents with nearby shard key values on the same
shard -- great for efficient range queries, but risky if the key is
monotonically increasing (all new writes hit one shard). Hashed sharding
distributes writes evenly by hashing the key first, at the cost of range
queries now having to fan out across every shard.

~~~javascript
sh.shardCollection('app.events', { _id: 'hashed' })
~~~

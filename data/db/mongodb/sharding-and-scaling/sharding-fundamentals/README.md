# DB / MongoDB / Sharding & Scaling / Sharding Fundamentals

## 1. Shard Keys

Sharding partitions a collection's data across multiple replica sets
(shards), each holding a portion of the data -- chosen by a shard key, a
field (or fields) present in every document. This single choice is the
most consequential decision in a sharded MongoDB deployment: it can't be
changed later without significant migration work, and a poor choice
creates uneven load no amount of hardware fixes.

```javascript
sh.shardCollection('app.orders', { customerId: 1 })
```

## 2. mongos & Config Servers

`mongos` is the query router applications connect to -- it doesn't store
data itself, just routes each query to the shard(s) that hold the
relevant data. Config servers store the cluster's metadata (which shard
owns which data range), consulted by every `mongos` instance to route
correctly.

```javascript
sh.status()  // cluster topology: shards, chunks, config servers
```

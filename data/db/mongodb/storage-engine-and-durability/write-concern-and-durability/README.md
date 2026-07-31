## DB / MongoDB / Storage Engine & Durability / Write Concern & Durability

### 1. Write Concern

Write concern controls how many nodes must acknowledge a write before
the driver considers it successful: `w: 1` (just the primary), `w:
'majority'` (a majority of the replica set, safe against most failover
scenarios), `w: 0` (fire-and-forget, no acknowledgment at all). It's a
per-operation tradeoff between latency and durability guarantees, not a
fixed server setting.

~~~javascript
db.orders.insertOne(
  { total: 99.90 },
  { writeConcern: { w: 'majority' } }
)
~~~

### 2. Journaling

WiredTiger writes changes to an on-disk journal before applying them to
the data files -- the same role a redo log or WAL plays in relational
engines, replayed after an unclean shutdown to recover changes that were
acknowledged but not yet fully persisted to the data files themselves.

~~~javascript
db.serverStatus().dur  // journaling stats, if enabled
~~~

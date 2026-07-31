## DB / MongoDB / Replication / Replica Set Fundamentals

### 1. Replica Set Architecture

A replica set is one primary (accepts writes) plus one or more secondaries
(replicate the primary's changes) -- unlike a manually configured
primary/replica pair, the set manages failover itself: if the primary
becomes unreachable, the remaining members automatically elect a new one.

~~~javascript
rs.status()
~~~

### 2. The Oplog

The oplog (operations log) is a capped collection recording every write
in order -- secondaries continuously tail it and apply the same
operations to stay in sync, the same role a binlog plays in MySQL or WAL
shipping plays in Postgres. Its fixed size limits how far a secondary can
fall behind before it needs a full resync.

~~~javascript
db.getSiblingDB('local').oplog.rs.find().limit(5)
~~~

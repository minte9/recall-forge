## DB / MongoDB / Replication / Elections & Failover

### 1. Primary Election

When the current primary becomes unreachable, the remaining members hold
an election and vote for a new primary among themselves -- a member with
higher `priority`, or more up-to-date oplog data, is favored. This
typically completes in a few seconds, during which the replica set can't
accept writes.

~~~javascript
rs.conf().members  // shows each member's priority and votes
~~~

### 2. Read Preference

Read preference controls which member type a read is routed to:
`primary` (default, always current), `primaryPreferred`, `secondary`
(offload reads from the primary, at the cost of possibly stale data),
`nearest` (lowest latency, regardless of role) -- a per-query or
per-connection choice between consistency and read scalability.

~~~javascript
db.orders.find({ status: 'PAID' }).readPref('secondary')
~~~

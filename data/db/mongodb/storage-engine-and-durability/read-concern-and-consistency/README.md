# DB / MongoDB / Storage Engine & Durability / Read Concern & Consistency

## 1. Read Concern

Read concern controls what guarantee a read makes about the data it
returns: `local` (whatever the node currently has, may be rolled back
later), `majority` (only data acknowledged by a majority of the replica
set, won't be rolled back), `linearizable` (the strongest guarantee,
reflects every majority-committed write before the read started).
Stronger guarantees cost more latency.

```javascript
db.orders.find({ status: 'PAID' }).readConcern('majority')
```

## 2. Causal Consistency Within a Session

A causally consistent session guarantees that a read reflects every prior
write made in that same session, even if the read is routed to a
different replica set member -- without it, reading immediately after
writing could hit a secondary that hasn't yet replicated the change.

```javascript
const session = db.getMongo().startSession({ causalConsistency: true });
```

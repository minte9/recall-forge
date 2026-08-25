# DB / MongoDB / Replication / Change Streams

## 1. Change Streams

A change stream is a subscription to real-time changes on a collection,
database, or cluster, built on top of the oplog -- an application can
react to inserts/updates/deletes as they happen without polling the
collection repeatedly.

```javascript
const stream = db.orders.watch();
stream.forEach(change => print(change.operationType, change.documentKey));
```

## 2. Resuming with Resume Tokens

Every change event includes a resume token; passing it back via
`resumeAfter` when reopening a stream picks up exactly where it left off
after a disconnect, instead of missing events or replaying ones already
processed -- the mechanism that makes change streams safe to build a
reliable consumer on top of.

```javascript
const resumed = db.orders.watch([], { resumeAfter: lastToken });
```

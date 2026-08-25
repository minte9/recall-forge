# DB / MongoDB / Storage Engine & Durability / WiredTiger Storage Engine

## 1. WiredTiger Fundamentals

WiredTiger has been MongoDB's default storage engine since 3.2, replacing
the older MMAPv1 -- it provides document-level concurrency control
(rather than collection- or database-level locking) and compresses data
on disk by default, both of which were the main reasons for the switch.

```javascript
db.serverStatus().storageEngine
```

## 2. The 16MB Document Size Limit

A single BSON document can't exceed 16MB -- a deliberate cap to keep a
document transferable and processable without excessive memory use. It
directly shapes schema design: an array field that could grow unbounded
(e.g. appending every event to one document forever) needs to be split
into separate documents, referenced rather than embedded, once growth
isn't bounded.

```javascript
Object.bsonsize(db.orders.findOne())  // check a document's BSON size
```

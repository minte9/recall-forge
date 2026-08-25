# DB / MongoDB / mongosh CLI / Querying from the Shell

## 1. db.collection.find() & Cursor Methods

`find()` returns a cursor, not the result set directly -- `.limit(10)`,
`.sort({ field: 1 })`, and `.pretty()` are chained method calls on that
cursor object, evaluated lazily, rather than SQL clauses baked into one
statement. The shell auto-prints the first batch of results when a
cursor is the last expression evaluated.

```javascript
db.orders.find({ status: 'PAID' }).sort({ total: -1 }).limit(5).pretty()
```

## 2. explain()

Appending `.explain('executionStats')` to a query (or aggregation) shows
which index MongoDB used, how many documents were scanned versus
returned, and the execution time -- mongosh's answer to `EXPLAIN
ANALYZE`. A big gap between documents examined and documents returned is
the same "missing index" signal it is in any other engine.

```javascript
db.orders.find({ status: 'PAID' }).explain('executionStats')
```

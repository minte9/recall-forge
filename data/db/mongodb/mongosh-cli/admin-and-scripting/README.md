# DB / MongoDB / mongosh CLI / Admin & Scripting

## 1. Running a Script File with mongosh

`mongosh` is a full JavaScript REPL connected to the database, not just a
query prompt -- `mongosh script.js` runs a `.js` file's statements
non-interactively, and ordinary JS (variables, loops, functions) works
alongside `db.*` calls, unlike SQL clients where scripting means chaining
SQL statements.

```javascript
// script.js
for (let i = 0; i < 5; i++) {
  db.orders.insertOne({ seq: i });
}
```

## 2. db.stats() & Collection Stats

`db.stats()` reports database-wide size and object counts; calling
`.stats()` on a specific collection reports the same for just that
collection -- storage size, document count, index sizes -- the quick
first check when diagnosing unexpected disk usage or deciding whether a
collection needs sharding.

```javascript
db.stats()
db.orders.stats()
```

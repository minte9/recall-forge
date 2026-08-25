# DB / SQL / SQL Antipatterns / Indexes

## 1. Indexing Every Column "Just in Case"

Every index speeds up reads that use it, but it also has to be updated on
every `INSERT`/`UPDATE`/`DELETE` that touches its columns, and takes
storage space -- indexing columns that are never filtered or joined on
just adds write overhead with no matching read benefit.

```sql
-- an index on a rarely-queried column pays write cost for no read benefit
CREATE INDEX idx_notes ON products (internal_notes)
```

## 2. Index What EXPLAIN Shows

`EXPLAIN` shows exactly which queries are scanning instead of seeking --
that's the signal for where an index earns its write cost, rather than
guessing. Foreign key columns and whatever actually drives `WHERE`,
`JOIN`, and `ORDER BY` in real queries are the ones worth indexing;
everything else is a cost with no matching benefit.

```sql
-- customer_id is joined/filtered on constantly but has no index
CREATE INDEX idx_orders_customer ON orders (customer_id)
```

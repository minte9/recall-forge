## DB / Antipatterns / Physical Design / Indexes

### 1. Indexing Every Column "Just in Case"

Every index speeds up reads that use it, but it also has to be updated on
every `INSERT`/`UPDATE`/`DELETE` that touches its columns, and takes
storage space -- indexing columns that are never filtered or joined on
just adds write overhead with no matching read benefit.

~~~sql
-- an index on a rarely-queried column pays write cost for no read benefit
CREATE INDEX idx_notes ON products (internal_notes)
~~~

### 2. Missing the Indexes That Matter

The opposite mistake is just as common: no index on foreign key columns
or the columns actually used in `WHERE`/`JOIN`/`ORDER BY`, forcing a full
table scan on every such query. `EXPLAIN` shows exactly which queries are
scanning instead of seeking -- that's where an index earns its write cost.

~~~sql
-- customer_id is joined/filtered on constantly but has no index
CREATE INDEX idx_orders_customer ON orders (customer_id)
~~~

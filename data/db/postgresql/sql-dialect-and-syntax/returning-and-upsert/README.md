## DB / PostgreSQL / SQL Dialect & Syntax / RETURNING & Upsert

### 1. RETURNING Clause

`INSERT`, `UPDATE`, and `DELETE` can all end with `RETURNING`, giving back
the affected rows' values in the same round trip -- no separate `SELECT`
needed to fetch a newly generated ID or confirm what was actually
changed. Several other engines only offer this for `INSERT`, or not at
all.

~~~sql
INSERT INTO orders (customer_id, total) VALUES (1, 99.90)
RETURNING id, created_at;
~~~

### 2. INSERT ... ON CONFLICT

Postgres's upsert idiom: `ON CONFLICT (columns) DO UPDATE` (or `DO
NOTHING`) specifies what happens when the insert would violate a unique
constraint -- the equivalent of MySQL's `ON DUPLICATE KEY UPDATE`, but
requires naming the conflicting constraint/columns explicitly rather than
matching any unique key.

~~~sql
INSERT INTO inventory (sku, qty) VALUES ('ABC', 5)
ON CONFLICT (sku) DO UPDATE SET qty = inventory.qty + EXCLUDED.qty;
~~~

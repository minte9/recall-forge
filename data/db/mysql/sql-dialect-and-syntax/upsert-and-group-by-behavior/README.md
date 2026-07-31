## DB / MySQL / SQL Dialect & Syntax / Upsert & GROUP BY Behavior

### 1. INSERT ... ON DUPLICATE KEY UPDATE

MySQL's upsert idiom triggers the `UPDATE` clause when the insert would
violate a `UNIQUE`/`PRIMARY KEY` constraint, instead of raising an error
-- functionally similar to Postgres's `ON CONFLICT DO UPDATE` or the
standard `MERGE` statement, but with MySQL-specific syntax that doesn't
transfer directly.

~~~sql
INSERT INTO inventory (sku, qty) VALUES ('ABC', 5)
ON DUPLICATE KEY UPDATE qty = qty + VALUES(qty);
~~~

### 2. ONLY_FULL_GROUP_BY

Older MySQL versions permissively allowed `SELECT` columns not listed in
`GROUP BY` and not wrapped in an aggregate, silently picking an arbitrary
row's value for them -- a behavior standard SQL never allowed.
`ONLY_FULL_GROUP_BY` (the default since 5.7.5) rejects such queries,
matching the stricter behavior other engines always enforced.

~~~sql
-- rejected under ONLY_FULL_GROUP_BY: name isn't grouped or aggregated
SELECT customer_id, name, COUNT(*) FROM orders GROUP BY customer_id;
~~~

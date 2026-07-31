## DB / SQL / Query Fundamentals / Sorting & Pagination

### 1. ORDER BY Fundamentals

`ORDER BY` runs last in logical order, after `SELECT`, so it can reference
column aliases that `WHERE` can't. Sort direction defaults to `ASC`; `NULL`
placement (`NULLS FIRST`/`NULLS LAST`) is engine-dependent unless stated
explicitly, which matters when comparing behavior across databases.

~~~sql
SELECT customer_id, total AS order_total
FROM orders
ORDER BY order_total DESC NULLS LAST
~~~

### 2. LIMIT/OFFSET vs FETCH & Pagination Pitfalls

`LIMIT`/`OFFSET` (or standard `OFFSET ... FETCH`) caps result size, but
`OFFSET` re-scans and discards skipped rows every call -- deep pagination
(`OFFSET 100000`) gets slow. Keyset pagination (`WHERE id > :last_id LIMIT
20`) avoids the scan by seeking directly, at the cost of no random-access
page jumps.

~~~sql
-- offset pagination: slow at high offsets
SELECT * FROM orders ORDER BY id LIMIT 20 OFFSET 100000;

-- keyset pagination: seeks directly, no scan-and-discard
SELECT * FROM orders WHERE id > 100000 ORDER BY id LIMIT 20;
~~~

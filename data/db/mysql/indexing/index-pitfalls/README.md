## DB / MySQL / Indexing / Index Pitfalls

### 1. Implicit Type Conversion

Comparing an indexed integer column to a string literal (or vice versa)
forces MySQL to convert values before comparing them, which can silently
disable the index -- the comparison MySQL actually runs is no longer a
direct match against the indexed column's stored values.

~~~sql
-- customer_id is INT and indexed, but '123' is a string
WHERE customer_id = '123'   -- may not use the index
WHERE customer_id = 123     -- uses the index
~~~

### 2. Functions on Indexed Columns

Wrapping an indexed column in a function (`WHERE YEAR(created_at) =
2024`) forces MySQL to compute that function for every row, since the
index only knows the raw column values, not the function's output --
the engine can't seek to "rows where YEAR(x) = 2024" the way it can seek
to a value range.

~~~sql
-- can't use an index on created_at
WHERE YEAR(created_at) = 2024
-- can: rewrite as a sargable range instead
WHERE created_at >= '2024-01-01' AND created_at < '2025-01-01'
~~~

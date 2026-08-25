# DB / SQL / SQL Antipatterns / Random

## 1. ORDER BY RANDOM() for "Pick One Row"

`ORDER BY RANDOM() LIMIT 1` reads like the obvious way to get a random
row, but the engine has to assign a random value to and sort *every* row
in the table before it can take the first one -- an O(n log n) operation
to return a single row, and it gets slower as the table grows.

```sql
-- sorts the entire table just to return 1 row
SELECT * FROM products ORDER BY RANDOM() LIMIT 1;
```

## 2. Cheaper Ways to Get a Random Row

Generating a random id/offset within the known key range and seeking
directly to it avoids the full-table sort -- O(1) instead of O(n). For
sampling many rows, some engines offer `TABLESAMPLE`, which reads a
random subset of pages instead of scanning and sorting every row.

```sql
-- pick a random id in range, then seek directly
SELECT * FROM products
WHERE id >= (SELECT floor(random() * max(id)) FROM products)
ORDER BY id LIMIT 1;
```

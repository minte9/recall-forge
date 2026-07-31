## DB / Antipatterns / Logical Design / Clone Tables

### 1. Splitting by Year/Region into Separate Tables

Creating `orders_2023`, `orders_2024`, `orders_2025` (or per-region
variants) instead of one `orders` table with a `year`/`region` column
looks like a way to keep individual tables small, but it duplicates the
schema itself -- every column, index, and constraint now exists N times
instead of once.

~~~sql
-- clone tables: same schema repeated per year
CREATE TABLE orders_2023 (id INT, customer_id INT, total NUMERIC);
CREATE TABLE orders_2024 (id INT, customer_id INT, total NUMERIC);
~~~

### 2. Why This Breaks Aggregation & Schema Changes

"Total sales across all years" now needs a `UNION ALL` across every clone
table instead of a `GROUP BY`, and adding a column means repeating the
`ALTER TABLE` once per clone, easy to miss one. Partitioning (one logical
table, physically split by the database) gives the same storage benefit
without splitting the schema.

~~~sql
SELECT SUM(total) FROM (
  SELECT total FROM orders_2023
  UNION ALL
  SELECT total FROM orders_2024
) all_orders
~~~

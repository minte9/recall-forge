## DB / SQL / SQL Antipatterns / Clone Tables

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

### 2. Partition Instead

A partitioned table is one logical table -- one schema, one `ALTER TABLE`,
one `GROUP BY` -- physically split into per-year segments by the database
itself. It keeps the storage/maintenance benefit clone tables were reaching
for, without forcing every cross-period query into a manual `UNION ALL`.

~~~sql
CREATE TABLE orders (id INT, order_date DATE, total NUMERIC)
  PARTITION BY RANGE (order_date);
CREATE TABLE orders_2023 PARTITION OF orders
  FOR VALUES FROM ('2023-01-01') TO ('2024-01-01');
~~~

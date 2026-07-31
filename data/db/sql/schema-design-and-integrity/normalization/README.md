## DB / SQL / Schema Design & Integrity / Normalization

### 1. Normal Forms (1NF-3NF)

1NF requires atomic columns (no repeating groups/arrays packed into one
field). 2NF requires every non-key column depend on the *whole* primary
key, not part of it (only matters with composite keys). 3NF requires
non-key columns depend on nothing but the key -- no column depending on
another non-key column. Each form removes a specific class of update
anomaly.

~~~sql
-- violates 3NF: city_population depends on city, not on order id
CREATE TABLE orders (id INT, city TEXT, city_population INT)
~~~

### 2. Denormalization Tradeoffs

Denormalizing (duplicating data to avoid a join) trades write complexity
and risk of inconsistent copies for faster reads -- a deliberate choice
for read-heavy paths (reporting tables, caches), not a shortcut for
schema design done under time pressure. The key question is always "who
keeps the duplicated copies in sync, and what happens when they drift?"

~~~sql
-- denormalized: customer_name duplicated to skip a join on read
CREATE TABLE orders (id INT, customer_id INT, customer_name TEXT)
~~~

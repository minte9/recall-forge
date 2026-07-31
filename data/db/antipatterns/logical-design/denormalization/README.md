## DB / Antipatterns / Logical Design / Denormalization

### 1. Premature Denormalization

Duplicating data across tables to avoid a join looks like a performance
win, but it's usually applied before profiling shows a join is actually
the bottleneck. Most joins on indexed keys are cheap -- the real cost of
denormalizing is paid later, on every write, not on the read you
optimized for.

~~~sql
-- denormalized: customer_name copied onto every order row
CREATE TABLE orders (id INT, customer_id INT, customer_name TEXT)
~~~

### 2. Denormalize Deliberately

Keep the schema normalized by default -- a join on an indexed key. If a
specific read path is proven too slow, denormalize as an explicit,
documented decision (a materialized view or a cache table with a known
refresh strategy), not by scattering duplicate columns wherever a join
felt inconvenient.

~~~sql
-- explicit, refreshable, not an ad hoc duplicate column
CREATE MATERIALIZED VIEW order_summary AS
SELECT o.id, c.name AS customer_name FROM orders o
JOIN customers c ON c.id = o.customer_id;
~~~

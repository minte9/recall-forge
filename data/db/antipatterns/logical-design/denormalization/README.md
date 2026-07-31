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

### 2. Keeping Duplicates in Sync

Once a value is copied into a second table, every update path must touch
both places or the copies drift -- a customer rename that only updates
`customers` leaves stale `customer_name` values sitting in `orders`
forever. Triggers or application-level sync logic can paper over this, but
they're extra failure surface a normalized schema doesn't need.

~~~sql
-- must be kept in sync with customers.name, or it silently goes stale
UPDATE orders SET customer_name = 'New Name' WHERE customer_id = 1;
~~~

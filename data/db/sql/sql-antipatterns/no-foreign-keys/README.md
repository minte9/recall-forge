## DB / SQL / SQL Antipatterns / No Foreign Keys

### 1. Skipping FKs "for Performance"

Dropping foreign key constraints to avoid the small overhead of the
database checking referenced rows is a common justification, but that
overhead is usually negligible next to what it buys: the database is the
one thing that can guarantee referential integrity across every code path
that writes to it, forever, not just the ones you remembered to test.

~~~sql
-- no FK: nothing stops customer_id from pointing at a row that's gone
CREATE TABLE orders (id INT, customer_id INT)
~~~

### 2. Add the Foreign Key

Declaring the FK lets the database reject the orphaning write itself,
across every code path, instead of relying on application checks that
only cover the ones someone remembered to write. `ON DELETE
CASCADE`/`RESTRICT` then makes the intended behavior on parent deletion
explicit, instead of leaving it to whatever the deleting code happens to
do.

~~~sql
ALTER TABLE orders
  ADD CONSTRAINT fk_customer
  FOREIGN KEY (customer_id) REFERENCES customers(id)
~~~

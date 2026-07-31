## DB / Antipatterns / Logical Design / No Foreign Keys

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

### 2. What You Lose Without Referential Integrity

Without FKs, a deleted customer silently leaves orphaned orders behind --
no error, just rows referencing an id that no longer exists, discovered
later as a `NULL` join result or a bug report. Application-level checks
can enforce the same rule, but only for the write paths that remember to
call them.

~~~sql
ALTER TABLE orders
  ADD CONSTRAINT fk_customer
  FOREIGN KEY (customer_id) REFERENCES customers(id)
~~~

## DB / SQL / Schema Design & Integrity / Keys & Constraints

### 1. Primary & Foreign Keys

A primary key uniquely identifies a row and implicitly creates a unique
index; a foreign key constrains a column to values that exist in another
table's key, enforcing referential integrity at the database level instead
of trusting application code to keep references valid.

~~~sql
CREATE TABLE orders (
  id INT PRIMARY KEY,
  customer_id INT REFERENCES customers(id)  -- FK: must exist in customers
)
~~~

### 2. UNIQUE/CHECK Constraints & Referential Integrity

`UNIQUE` forbids duplicate values (allowing multiple `NULL`s in most
engines, since `NULL <> NULL`). `CHECK` enforces an arbitrary boolean
condition per row. `ON DELETE CASCADE`/`ON DELETE RESTRICT` on a foreign
key decide what happens to child rows when the referenced parent is
deleted -- picking the wrong default here is a classic source of orphaned
rows or accidental mass deletes.

~~~sql
ALTER TABLE orders
  ADD CONSTRAINT chk_qty CHECK (quantity > 0)
~~~

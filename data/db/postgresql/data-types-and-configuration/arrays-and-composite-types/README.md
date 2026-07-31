## DB / PostgreSQL / Data Types & Configuration / Arrays & Composite Types

### 1. Native ARRAY Columns & Operators

Any Postgres type can be used as an array column (`TEXT[]`, `INT[]`) --
no separate join table needed for a simple list of values. `@>` tests
containment, `ANY(array)` checks membership, and `unnest()` expands an
array back into rows when you need to join or aggregate over its
elements.

~~~sql
CREATE TABLE products (id INT, tags TEXT[]);
SELECT * FROM products WHERE tags @> ARRAY['sale'];
SELECT id, unnest(tags) AS tag FROM products;
~~~

### 2. Composite/Row Types

A composite type groups fields into a single named type, usable as a
column type, a function's parameter/return type, or the row type
implicitly created by every table. It's Postgres's closest equivalent to
a struct, letting a function return multiple named values as one type
instead of `OUT` parameters.

~~~sql
CREATE TYPE address AS (street TEXT, city TEXT, zip TEXT);
CREATE TABLE customers (id INT, home address);
~~~

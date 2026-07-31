## DB / Antipatterns / Physical Design / Enum

### 1. Baking a Fixed Value List into the Column Type

`ENUM('small', 'medium', 'large')` validates the value set right in the
schema, which looks like free data integrity -- but it hard-codes the
list of allowed values into the table definition itself, not into data
that can be queried, joined against, or extended without touching DDL.

~~~sql
CREATE TABLE products (
  size ENUM('small', 'medium', 'large')
)
~~~

### 2. Why It's Painful to Change

Adding `'extra-large'` later means an `ALTER TABLE` that some engines
run as a full table rewrite, and the valid-values list isn't queryable
(no `SELECT DISTINCT valid_size FROM ...`). A lookup table with a foreign
key gets the same validation, is extensible with an `INSERT`, and can
carry extra metadata (display order, description) the enum can't.

~~~sql
CREATE TABLE sizes (code TEXT PRIMARY KEY, display_order INT);
CREATE TABLE products (
  size TEXT REFERENCES sizes(code)  -- extend by inserting a row
)
~~~

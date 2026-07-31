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

### 2. A Lookup Table Instead

A lookup table referenced by foreign key gives the same validation as
`ENUM`, but the valid-values list becomes ordinary queryable data --
extending it is an `INSERT`, not an `ALTER TABLE`, and it can carry extra
metadata (display order, description) an enum type has no room for.

~~~sql
CREATE TABLE sizes (code TEXT PRIMARY KEY, display_order INT);
CREATE TABLE products (
  size TEXT REFERENCES sizes(code)  -- extend by inserting a row
)
~~~

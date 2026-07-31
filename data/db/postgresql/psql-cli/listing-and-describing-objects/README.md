## DB / PostgreSQL / psql CLI / Listing & Describing Objects

### 1. \dt & \d: Listing and Describing Tables

`\dt` lists tables in the current schema's search path -- the first
command to run against an unfamiliar database. `\d tablename` shows a
table's full structure: columns, types, defaults, indexes, and foreign
keys, the psql equivalent of MySQL's `DESCRIBE` plus `SHOW CREATE TABLE`
combined into one.

~~~
\dt
\d orders
~~~

### 2. \di, \dv & \df: Indexes, Views & Functions

The `\d` family extends to every object type with a letter suffix: `\di`
lists indexes, `\dv` lists views, `\df` lists functions. Appending `+` to
any of these (`\dt+`, `\d+ orders`) adds extra detail -- size on disk,
storage parameters, comments -- that the plain form omits.

~~~
\di
\dv
\d+ orders
~~~

## DB / Oracle / SQLPlus CLI / Listing & Describing Objects

### 1. DESC/DESCRIBE

`DESC tablename` (or `DESCRIBE`) lists a table's columns, types, and
nullability -- the same role as MySQL's `DESCRIBE` or Postgres's `\d`. It
also works on views, procedures, and packages, showing a procedure's
parameter list instead of columns.

~~~sql
DESC orders;
DESC order_pkg;
~~~

### 2. Querying Data Dictionary Views

Oracle's metadata views come in three tiers with a consistent prefix
convention: `USER_TABLES` (objects the current user owns), `ALL_TABLES`
(everything the current user can access, including other schemas),
`DBA_TABLES` (every object in the database, needs elevated privileges).
The same suffix (`_TABLES`, `_INDEXES`, `_CONSTRAINTS`) exists across all
three prefixes.

~~~sql
SELECT table_name FROM user_tables;
SELECT owner, table_name FROM all_tables WHERE table_name = 'ORDERS';
~~~

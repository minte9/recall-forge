## DB / MySQL / SQL Dialect & Syntax / String & NULL Functions

### 1. Case-Insensitive String Comparison by Default

MySQL's default collations (e.g. `utf8mb4_general_ci`/`_unicode_ci`) make
`=` comparisons case-insensitive out of the box -- `'Smith' = 'smith'` is
true, unlike engines that default to case-sensitive comparison. A
case-sensitive column needs an explicit `_bin` or `_cs` collation, not
just the right data type.

~~~sql
SELECT 'Smith' = 'smith';  -- 1 (true) under a _ci collation
~~~

### 2. IFNULL & String Functions

`IFNULL(expr, default)` is MySQL's two-argument shorthand for the
standard `COALESCE(expr, default)` -- both work in MySQL, but `IFNULL`
only takes two arguments where `COALESCE` takes any number.
`GROUP_CONCAT` folds a group's values into one delimited string, MySQL's
answer to what `STRING_AGG` does in other engines.

~~~sql
SELECT GROUP_CONCAT(name ORDER BY name SEPARATOR ', ') FROM customers;
~~~

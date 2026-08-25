# DB / MySQL / Data Types & Configuration / MySQL-Specific Data Types

## 1. Integer & Text Size Tiers

MySQL offers a tiered set of integer types (`TINYINT`, `SMALLINT`,
`MEDIUMINT`, `INT`, `BIGINT`) and text types (`TINYTEXT`, `TEXT`,
`MEDIUMTEXT`, `LONGTEXT`), each with a different storage size and max
value/length -- unlike engines that only offer a couple of broad
integer/text types, picking the right tier here directly controls row
size.

```sql
CREATE TABLE products (
  quantity TINYINT UNSIGNED,   -- 0-255, 1 byte
  description MEDIUMTEXT       -- up to ~16MB
)
```

## 2. ENUM & SET Types

`ENUM` stores one value from a fixed list; `SET` stores any combination
of values from a fixed list, packed into a single integer internally.
Both sort by the *definition order* of the list, not alphabetically --
`ENUM('low','medium','high')` sorts low < medium < high even though
that's not alphabetical order.

```sql
CREATE TABLE tasks (priority ENUM('low', 'medium', 'high'));
SELECT * FROM tasks ORDER BY priority;  -- low, medium, high (not A-Z)
```

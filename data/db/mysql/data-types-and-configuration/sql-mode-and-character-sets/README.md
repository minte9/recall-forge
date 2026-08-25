# DB / MySQL / Data Types & Configuration / sql_mode & Character Sets

## 1. sql_mode & STRICT_TRANS_TABLES

`sql_mode` is a server setting controlling how strictly MySQL enforces
data validity -- in permissive (legacy) mode, an out-of-range or
wrong-type value gets silently truncated or coerced instead of raising an
error. `STRICT_TRANS_TABLES`, part of the modern default, rejects such
values with an error instead, matching the behavior most other engines
always had.

```sql
SELECT @@sql_mode;
```

## 2. utf8 vs utf8mb4

MySQL's `utf8` character set is a historical mistake: it only supports up
to 3 bytes per character, which excludes many emoji and some CJK
characters -- full UTF-8 needs `utf8mb4` (up to 4 bytes). Any new
database should default to `utf8mb4`; `utf8` alone is a trap for anyone
assuming it means what "UTF-8" means everywhere else.

```sql
CREATE DATABASE app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

# DB / MySQL / MySQL CLI / Listing & Describing Objects

## 1. SHOW DATABASES & SHOW TABLES

`SHOW DATABASES` lists every database the current user can see; `SHOW
TABLES` lists tables in whichever database is currently selected via
`USE`. These are the first two commands to run when exploring an
unfamiliar MySQL server from the CLI, the same role `\l` and `\dt` play
in `psql`.

```sql
SHOW DATABASES;
USE app;
SHOW TABLES;
```

## 2. DESCRIBE & SHOW CREATE TABLE

`DESCRIBE tablename` (or `DESC`) lists columns, types, nullability, and
keys -- a quick structural summary. `SHOW CREATE TABLE tablename` prints
the full `CREATE TABLE` statement as MySQL would generate it, including
every constraint and index definition, useful for copying a table's exact
structure elsewhere.

```sql
DESCRIBE orders;
SHOW CREATE TABLE orders;
```

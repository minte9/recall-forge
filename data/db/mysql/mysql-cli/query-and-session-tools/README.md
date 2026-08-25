# DB / MySQL / MySQL CLI / Query & Session Tools

## 1. \G: Vertical Output

Ending a query with `\G` instead of `;` prints each row as one column per
line rather than a wide table -- the MySQL CLI's answer to `psql`'s `\x`,
essential when a row has more columns than fit in a terminal's width.

```sql
SELECT * FROM orders WHERE id = 1\G
```

## 2. SHOW PROCESSLIST & source

`SHOW PROCESSLIST` lists every active connection and what it's currently
running -- the first stop when a query seems stuck, to see if something
else is holding a lock or a connection is hung. `source file.sql` (or
`\.`) runs a script file's statements in the current session, the CLI
equivalent of piping a file into `mysql`.

```sql
SHOW PROCESSLIST;
source /path/to/migration.sql;
```

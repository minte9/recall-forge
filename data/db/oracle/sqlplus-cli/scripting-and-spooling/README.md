# DB / Oracle / SQLPlus CLI / Scripting & Spooling

## 1. @script.sql & START

`@script.sql` (or the equivalent `START script.sql`) runs a `.sql` file's
statements in the current SQL*Plus session -- the standard way to apply a
migration or run a saved query without retyping it. `@@script.sql` (double
`@`) resolves the path relative to the currently running script, useful
for scripts that call other scripts in the same directory.

```sql
@migration_001.sql
START migration_002.sql
```

## 2. SPOOL for Output Capture

`SPOOL filename` redirects everything printed to the session into a file
until `SPOOL OFF` -- the SQL*Plus equivalent of piping a session's output
to a file, commonly used to capture a report or a diagnostic query's
output for later review.

```sql
SPOOL orders_report.txt
SELECT * FROM orders WHERE status = 'PENDING';
SPOOL OFF
```

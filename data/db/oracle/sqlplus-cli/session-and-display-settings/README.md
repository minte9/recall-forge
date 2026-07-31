## DB / Oracle / SQLPlus CLI / Session & Display Settings

### 1. SET Commands

Unlike `psql` or the MySQL client, SQL*Plus doesn't auto-detect terminal
width -- `SET LINESIZE n` controls how many characters fit per line
before wrapping, and `SET PAGESIZE n` controls how many rows print before
repeating the column headers. Wide query results are unreadable without
tuning these first.

~~~sql
SET LINESIZE 200
SET PAGESIZE 50
~~~

### 2. SHOW Commands

`SHOW USER` prints the currently connected username -- a quick sanity
check before running anything destructive. `SHOW PARAMETER name`
queries an initialization parameter's current value (e.g.
`SHOW PARAMETER memory_target`), the CLI shortcut for what would
otherwise need a `v$parameter` query.

~~~sql
SHOW USER
SHOW PARAMETER open_cursors
~~~

# DB / PostgreSQL / psql CLI / Query & Session Tools

## 1. \x: Expanded Display

`\x` toggles between the normal wide-table layout and one-column-per-line
output for each row -- essential once a row has more columns than fit in
a terminal, the psql equivalent of ending a MySQL query with `\G`. `\x
auto` switches automatically only when a row would otherwise wrap.

```
\x auto
SELECT * FROM orders WHERE id = 1;
```

## 2. \timing & \e

`\timing` toggles printing how long each statement took to execute --
the quickest way to eyeball whether a query is slow without wrapping it
in `EXPLAIN ANALYZE`. `\e` opens the previous query in `$EDITOR`, handy
for editing a long query without retyping it at the prompt.

```
\timing on
SELECT count(*) FROM orders;
```

## DB / Antipatterns / Query / Null

### 1. Treating NULL Like a Regular Value

`NULL` means "unknown," so `= NULL`, `!= NULL`, and `<> NULL` all
evaluate to unknown rather than true/false and silently match nothing.
The sharpest version of this bug is `NOT IN (subquery)`: if the subquery
returns even one `NULL`, the entire `NOT IN` matches nothing, with no
error to flag it.

~~~sql
-- if any customer_id in the subquery is NULL, this matches 0 rows
WHERE id NOT IN (SELECT customer_id FROM archived)
~~~

### 2. Consistent NULL Handling

Use `IS NULL`/`IS NOT NULL` for comparisons, `COALESCE` for defaults, and
prefer `NOT EXISTS` over `NOT IN` when the subquery's column can be
`NULL`. At the schema level, deciding a column is `NOT NULL` with a
sensible default is often simpler than threading `NULL`-safe logic through
every query that touches it.

~~~sql
WHERE NOT EXISTS (
  SELECT 1 FROM archived a WHERE a.customer_id = c.id
)  -- unaffected by NULLs in archived.customer_id
~~~

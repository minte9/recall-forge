# DB / Oracle / SQL Dialect & Syntax / NULLs & Implicit Conversion

## 1. NVL & Empty String as NULL

`NVL(expr, default)` is Oracle's two-argument default-value function,
the same role as `IFNULL` in MySQL, though `COALESCE` also works in
Oracle for more than two arguments. The sharper gotcha: Oracle treats an
empty string `''` as `NULL` for `VARCHAR2` columns -- every other major
engine treats them as distinct values.

```sql
SELECT NVL(nickname, 'Anonymous') FROM users;
-- Oracle-specific: '' IS NULL evaluates to TRUE
INSERT INTO users (name) VALUES ('');
SELECT * FROM users WHERE name IS NULL;  -- matches the row above
```

## 2. Implicit Type Conversion

Oracle will silently convert a string to a number or date when comparing
it to a numeric/date column, following its own conversion rules --
convenient until the conversion fails at runtime (`ORA-01722: invalid
number`) or succeeds in a way that wasn't intended. Explicit `TO_NUMBER`/
`TO_DATE` avoids depending on the implicit rules.

```sql
-- implicit conversion: works, but silently depends on NLS settings
WHERE order_date = '01-JAN-24'
-- explicit: unambiguous regardless of session settings
WHERE order_date = TO_DATE('2024-01-01', 'YYYY-MM-DD')
```

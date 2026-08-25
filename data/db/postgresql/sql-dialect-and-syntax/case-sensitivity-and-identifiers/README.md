# DB / PostgreSQL / SQL Dialect & Syntax / Case Sensitivity & Identifiers

## 1. Unquoted vs Quoted Identifiers

An unquoted identifier is folded to lowercase before being stored, so
`CREATE TABLE Orders` actually creates `orders` -- `SELECT * FROM Orders`
and `SELECT * FROM ORDERS` both still work, because both get folded the
same way. A quoted identifier (`"Orders"`) preserves its exact case and
becomes case-sensitive, so it must be quoted identically on every later
reference.

```sql
CREATE TABLE "Orders" (id INT);
SELECT * FROM "Orders";   -- works
SELECT * FROM Orders;     -- error: relation "orders" does not exist
```

## 2. ILIKE for Case-Insensitive Matching

Unlike MySQL's default collation, Postgres string comparisons (`=`,
`LIKE`) are case-sensitive by default. `ILIKE` is Postgres's
case-insensitive `LIKE` variant -- the explicit opt-in that MySQL gives
you implicitly through its default collation.

```sql
SELECT * FROM customers WHERE name ILIKE 'smith';  -- matches 'Smith' too
```

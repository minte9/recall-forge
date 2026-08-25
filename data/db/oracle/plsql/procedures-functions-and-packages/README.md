# DB / Oracle / PLSQL / Procedures, Functions & Packages

## 1. Procedures & Functions

A procedure performs an action and may return values via `OUT`/`IN OUT`
parameters; a function must return exactly one value via `RETURN` and
can be called directly inside a SQL expression. Both are stored compiled
in the database, so calling one avoids the network round trip a client
would pay sending the same logic as ad hoc SQL.

```sql
CREATE FUNCTION order_total(p_customer_id NUMBER) RETURN NUMBER IS
  v_total NUMBER;
BEGIN
  SELECT SUM(amount) INTO v_total FROM orders
  WHERE customer_id = p_customer_id;
  RETURN NVL(v_total, 0);
END;
```

## 2. Packages for Encapsulation

A package groups related procedures/functions/types into one named unit
with a public specification (`CREATE PACKAGE`) and a hidden body
(`CREATE PACKAGE BODY`) -- the PL/SQL equivalent of an interface plus
implementation. Package-level variables also persist for the life of a
session, giving cheap session-scoped state without a table.

```sql
CREATE PACKAGE order_pkg IS
  FUNCTION total_for(p_customer_id NUMBER) RETURN NUMBER;
END order_pkg;
```

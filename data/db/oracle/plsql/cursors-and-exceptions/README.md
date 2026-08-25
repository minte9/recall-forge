# DB / Oracle / PLSQL / Cursors & Exceptions

## 1. Explicit vs Implicit Cursors

Every SQL statement in PL/SQL uses an implicit cursor behind the scenes
(`SQL%ROWCOUNT`, `SQL%FOUND` report on it after `INSERT`/`UPDATE`/
`DELETE`). An explicit cursor is declared and controlled manually
(`OPEN`/`FETCH`/`CLOSE`) for row-by-row processing -- or more idiomatically,
a cursor `FOR` loop handles the open/fetch/close automatically.

```sql
BEGIN
  FOR r IN (SELECT id, name FROM customers WHERE active = 1) LOOP
    DBMS_OUTPUT.PUT_LINE(r.id || ': ' || r.name);
  END LOOP;
END;
```

## 2. Exception Handling

The `EXCEPTION` section catches named conditions (`NO_DATA_FOUND`,
`TOO_MANY_ROWS`, `DUP_VAL_ON_INDEX`) or a custom-declared exception,
similar to typed catch blocks. `OTHERS` is the catch-all -- using it
without re-raising or logging the actual error is how real failures get
silently swallowed in production PL/SQL.

```sql
BEGIN
  SELECT name INTO v_name FROM customers WHERE id = 999;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    DBMS_OUTPUT.PUT_LINE('No such customer');
END;
```

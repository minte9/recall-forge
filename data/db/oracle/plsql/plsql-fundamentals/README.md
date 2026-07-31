## DB / Oracle / PLSQL / PLSQL Fundamentals

### 1. Anonymous Blocks & Variables

A PL/SQL block is `DECLARE` (optional) / `BEGIN` / `EXCEPTION` (optional)
/ `END` -- an anonymous block runs once and isn't stored, unlike a
procedure or function. Variables are declared with an explicit type in
the `DECLARE` section, and assignment uses `:=`, not `=` (which is
reserved for comparison, same as standard SQL).

~~~sql
DECLARE
  v_total NUMBER := 0;
BEGIN
  SELECT SUM(amount) INTO v_total FROM orders WHERE customer_id = 1;
  DBMS_OUTPUT.PUT_LINE('Total: ' || v_total);
END;
~~~

### 2. %TYPE and %ROWTYPE

`column%TYPE` declares a variable with the exact same data type as a
table column, so it stays correct automatically if the column's type
changes later. `table%ROWTYPE` declares a record matching an entire row's
structure -- both avoid hardcoding types that could silently drift from
the schema.

~~~sql
DECLARE
  v_name customers.name%TYPE;
  v_row  customers%ROWTYPE;
BEGIN
  SELECT name INTO v_name FROM customers WHERE id = 1;
END;
~~~

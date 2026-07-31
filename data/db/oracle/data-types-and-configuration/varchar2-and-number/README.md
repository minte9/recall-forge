## DB / Oracle / Data Types & Configuration / VARCHAR2 & NUMBER

### 1. VARCHAR2 vs VARCHAR

Oracle documentation explicitly reserves `VARCHAR` for a possible future
type with different (standard-SQL) semantics -- `VARCHAR2` is the type
that's actually stable and should always be used. Writing `VARCHAR` in
new Oracle DDL is a common habit carried over from other engines that
doesn't match Oracle's own guidance.

~~~sql
CREATE TABLE customers (name VARCHAR2(100));  -- correct
~~~

### 2. The NUMBER Type

`NUMBER(precision, scale)` is Oracle's single flexible numeric type,
covering what other engines split across `INT`, `BIGINT`, `DECIMAL`, and
`FLOAT` -- `NUMBER` with no precision/scale stores any number up to 38
significant digits, exactly, with no separate integer type needed.

~~~sql
CREATE TABLE orders (
  id NUMBER(10),          -- integer-like, up to 10 digits
  total NUMBER(10, 2)     -- exact, 2 decimal places
)
~~~

## DB / PostgreSQL / Data Types & Configuration / SERIAL, IDENTITY & TIMESTAMPTZ

### 1. SERIAL vs GENERATED ... AS IDENTITY

`SERIAL` (the older idiom) is shorthand that creates a backing sequence
and an implicit default -- but the column itself is just a plain integer,
so nothing stops a direct `INSERT` from breaking the sequence.
`GENERATED ... AS IDENTITY` (the SQL-standard form, preferred since
Postgres 10) can enforce that only the sequence generates the value,
closer to what `AUTO_INCREMENT` guarantees in MySQL.

~~~sql
CREATE TABLE orders (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  total NUMERIC
);
~~~

### 2. TIMESTAMPTZ vs TIMESTAMP

`TIMESTAMPTZ` stores every value converted to UTC internally, then
converts back to the session's timezone on display -- despite the name,
it doesn't store a timezone, just a point in time. Plain `TIMESTAMP`
stores exactly what was written, with no conversion either way.
`TIMESTAMPTZ` is the right default for almost anything representing a
real-world moment.

~~~sql
SET timezone = 'America/New_York';
SELECT now();  -- TIMESTAMPTZ, displayed in the session's timezone
~~~

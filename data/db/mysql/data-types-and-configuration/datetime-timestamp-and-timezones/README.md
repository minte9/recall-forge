## DB / MySQL / Data Types & Configuration / DATETIME, TIMESTAMP & Timezones

### 1. DATETIME vs TIMESTAMP

`DATETIME` stores the literal value with no timezone conversion, valid
across a wide range (1000 to 9999). `TIMESTAMP` stores as UTC internally
and converts to the session's timezone on read, but its range is much
narrower (1970 to 2038) -- a `TIMESTAMP` column can also auto-populate on
insert/update (`DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`).

~~~sql
CREATE TABLE events (
  happened_at DATETIME,                 -- stored as-is, no conversion
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)
~~~

### 2. Zero Dates & Strict Mode

Historically MySQL allowed `'0000-00-00'` as a placeholder "no date"
value -- valid to insert but invalid to actually use in date arithmetic,
a landmine for anyone expecting `NULL` semantics. `STRICT_TRANS_TABLES`
mode (part of the modern default `sql_mode`) rejects zero dates outright,
forcing an explicit `NULL` instead.

~~~sql
-- rejected under strict mode
INSERT INTO events (happened_at) VALUES ('0000-00-00');
~~~

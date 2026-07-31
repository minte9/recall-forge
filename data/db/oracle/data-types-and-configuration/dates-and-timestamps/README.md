## DB / Oracle / Data Types & Configuration / DATE & TIMESTAMP

### 1. DATE Always Includes Time

Unlike the ANSI standard `DATE` type (date only, no time), Oracle's
`DATE` always stores a time component down to the second -- a "date-only"
value is really just a `DATE` where the time portion happens to be
midnight. Comparing a `DATE` column to a literal without accounting for
this is a common source of "row not found" bugs.

~~~sql
-- may miss rows with a non-midnight time component that day
WHERE order_date = TO_DATE('2024-01-01', 'YYYY-MM-DD')
-- safer: compare against a range instead
WHERE order_date >= TO_DATE('2024-01-01', 'YYYY-MM-DD')
  AND order_date < TO_DATE('2024-01-02', 'YYYY-MM-DD')
~~~

### 2. TIMESTAMP & Time Zones

Oracle has three explicit timestamp variants: plain `TIMESTAMP` (no time
zone, like `DATE` but with fractional seconds), `TIMESTAMP WITH TIME
ZONE` (stores the offset explicitly), and `TIMESTAMP WITH LOCAL TIME
ZONE` (stored as UTC, converted to the session's time zone on read) --
three distinct types, rather than Postgres's single `TIMESTAMPTZ` toggle.

~~~sql
CREATE TABLE events (happened_at TIMESTAMP WITH LOCAL TIME ZONE);
~~~

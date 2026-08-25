# DB / SQL / SQL Antipatterns / Using Float

## 1. Floating-Point Rounding Errors

`FLOAT`/`DOUBLE` store an approximation of most decimal values, not the
exact value -- `0.1 + 0.2` doesn't equal `0.3` in binary floating point.
For money or any value requiring exact arithmetic, that rounding error
accumulates across operations and can produce off-by-a-cent totals that
don't reconcile.

```sql
SELECT 0.1::float + 0.2::float = 0.3::float  -- false
```

## 2. DECIMAL/NUMERIC Instead

`DECIMAL(p, s)` stores an exact fixed-point value with a defined precision
and scale, so arithmetic on it doesn't drift the way binary floating point
does. The tradeoff is slightly more storage and computation cost -- worth
it for currency, quantities, or anything compared for exact equality.

```sql
CREATE TABLE payments (
  amount NUMERIC(10, 2)  -- exact to the cent, no rounding drift
)
```

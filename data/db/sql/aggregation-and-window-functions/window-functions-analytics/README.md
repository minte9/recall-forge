# DB / SQL / Aggregation & Window Functions / Window Functions: Analytics

## 1. Running Totals & Moving Averages

Unlike `GROUP BY`, a window `SUM`/`AVG` keeps one output row per input row
while still computing over a range of rows -- so a running total is just
`SUM(...) OVER (ORDER BY date)` with the default frame ("start through
current row"), no self-join or app-side loop needed.

```sql
SELECT order_date, amount,
  SUM(amount) OVER (ORDER BY order_date) AS running_total
FROM orders
```

## 2. LAG / LEAD

`LAG(col, n)` reads a column's value from `n` rows before the current row
within the window; `LEAD` reads `n` rows ahead. Both remove the need for a
self-join to compare a row against its neighbor -- common for "difference
from previous day" or "time since last event" calculations.

```sql
SELECT order_date, amount,
  amount - LAG(amount) OVER (ORDER BY order_date) AS delta
FROM daily_sales
```

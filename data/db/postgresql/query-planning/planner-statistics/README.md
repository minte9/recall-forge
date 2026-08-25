# DB / PostgreSQL / Query Planning / Planner Statistics

## 1. ANALYZE & pg_stats

`ANALYZE` samples a table and records column statistics (distinct value
counts, most common values, a histogram) in `pg_statistic`, queryable via
the `pg_stats` view -- this is the data the planner's cost estimates are
built from, same role as Oracle's optimizer stats or MySQL's index
cardinality estimates.

```sql
ANALYZE orders;
SELECT attname, n_distinct FROM pg_stats WHERE tablename = 'orders';
```

## 2. Extended Statistics for Correlated Columns

By default Postgres assumes columns are independent, so a predicate on
two correlated columns (`city = 'NYC' AND state = 'NY'`) can be
under-estimated -- the combination looks rarer to the planner than it
actually is. `CREATE STATISTICS` records cross-column dependencies
explicitly so the planner's row estimate accounts for the correlation.

```sql
CREATE STATISTICS city_state_stats (dependencies)
  ON city, state FROM customers;
```

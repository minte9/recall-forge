## DB / Oracle / Indexing & Optimizer / Cost-Based Optimizer

### 1. The CBO & Optimizer Statistics

The Cost-Based Optimizer picks an execution plan by estimating the cost
(I/O, CPU) of alternative plans, using statistics collected about each
table and index -- row counts, column value distributions (histograms),
and index selectivity. It doesn't just find *a* working plan; it's
choosing among many equivalent ones for the cheapest.

~~~sql
EXEC DBMS_STATS.GATHER_TABLE_STATS('APP', 'ORDERS');
~~~

### 2. Why Stale Stats Mislead the Optimizer

If a table's statistics were gathered when it had 100 rows and it now has
10 million, the CBO's cost estimates are based on the old numbers -- it
may pick a full table scan expecting it to be cheap, or an index it
thinks is highly selective when it no longer is. Regularly refreshed
stats (usually via an automated job) are what keep the CBO's decisions
matching reality.

~~~sql
SELECT table_name, last_analyzed FROM user_tables
WHERE table_name = 'ORDERS';
~~~

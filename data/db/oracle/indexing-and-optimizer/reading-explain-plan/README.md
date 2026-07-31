## DB / Oracle / Indexing & Optimizer / Reading EXPLAIN PLAN

### 1. EXPLAIN PLAN Output

`EXPLAIN PLAN` populates `PLAN_TABLE` with the CBO's chosen execution
steps without running the query; `DBMS_XPLAN.DISPLAY` renders it
readably as a tree, indented by nesting -- child operations feed their
parent. Watch for `TABLE ACCESS FULL` on a large table where an index
scan was expected; that's the same "not using the index" signal as other
engines' `EXPLAIN`.

~~~sql
EXPLAIN PLAN FOR SELECT * FROM orders WHERE customer_id = 5;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);
~~~

### 2. Optimizer Hints

A hint (`/*+ INDEX(...) */`, `/*+ FULL(...) */`) overrides the CBO's
choice for that specific statement -- an escape hatch for the rare case
where the optimizer's cost estimate is wrong and stats can't be fixed in
time. Overusing hints turns into its own maintenance problem: a hint that
was correct when written can quietly become wrong as data and stats
change.

~~~sql
SELECT /*+ INDEX(o idx_orders_customer) */ * FROM orders o
WHERE customer_id = 5;
~~~

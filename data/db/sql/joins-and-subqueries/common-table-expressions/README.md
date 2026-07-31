## DB / SQL / Joins & Subqueries / Common Table Expressions

### 1. Common Table Expressions

A `WITH` clause names a subquery so it can be referenced later in the
statement, sometimes more than once -- mainly a readability tool for
breaking a complex query into named steps, similar to extracting a
variable in application code. Whether it materializes or gets inlined is
engine-dependent, so it isn't automatically a performance win.

~~~sql
WITH big_spenders AS (
  SELECT customer_id, SUM(total) AS spent
  FROM orders GROUP BY customer_id HAVING SUM(total) > 1000
)
SELECT c.name, b.spent FROM customers c
JOIN big_spenders b ON b.customer_id = c.id
~~~

### 2. Recursive CTEs

A recursive CTE has an anchor query (base case) `UNION ALL`'d with a
recursive query that references the CTE itself, terminating when the
recursive part returns no rows -- the standard SQL tool for walking
hierarchies (org charts, category trees) without app-side recursion.

~~~sql
WITH RECURSIVE org AS (
  SELECT id, manager_id, 1 AS depth FROM employees WHERE manager_id IS NULL
  UNION ALL
  SELECT e.id, e.manager_id, org.depth + 1
  FROM employees e JOIN org ON e.manager_id = org.id
)
SELECT * FROM org
~~~

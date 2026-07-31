## DB / SQL / Joins & Subqueries / Subqueries

### 1. Scalar & Correlated Subqueries

A scalar subquery returns a single value and can sit anywhere an
expression can. A correlated subquery references a column from the outer
query, so it conceptually re-runs once per outer row -- powerful, but the
main reason a subquery-based query can turn out much slower than an
equivalent join.

~~~sql
SELECT name,
  (SELECT COUNT(*) FROM orders o WHERE o.customer_id = c.id) AS n
FROM customers c
-- correlated: re-evaluated per row of customers
~~~

### 2. Subquery vs Join Tradeoffs

Modern optimizers often rewrite `IN`/`EXISTS` subqueries into joins
internally, so the two aren't always as different as they look. `EXISTS`
short-circuits on the first match and ignores `NULL`s the way `IN` doesn't
-- prefer `EXISTS` over `NOT IN` when the subquery's column can contain
`NULL`, to avoid the "NOT IN silently matches nothing" trap.

~~~sql
WHERE EXISTS (
  SELECT 1 FROM orders o WHERE o.customer_id = c.id
)
~~~

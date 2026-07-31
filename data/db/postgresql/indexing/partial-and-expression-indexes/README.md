## DB / PostgreSQL / Indexing / Partial & Expression Indexes

### 1. Partial Indexes

A partial index only covers rows matching a `WHERE` clause on the index
definition itself -- indexing just the 1% of orders that are still
`'PENDING'` is far smaller and faster to maintain than indexing every
order ever placed, when queries only ever filter on that subset.

~~~sql
CREATE INDEX idx_orders_pending ON orders (created_at)
WHERE status = 'PENDING';
~~~

### 2. Expression Indexes

An index can be built on the result of an expression, not just a raw
column -- so a query filtering on `lower(email)` can still use an index,
even though no plain column stores the lowercased value. The expression
in the query must match the expression in the index definition for
Postgres to use it.

~~~sql
CREATE INDEX idx_users_email_lower ON users (lower(email));
SELECT * FROM users WHERE lower(email) = 'a@example.com';
~~~

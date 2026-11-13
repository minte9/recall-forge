## 1. Introduction

In relational databases, information is typically distributed across multiple tables to reduce duplication and maintain consistency.

For example, an e-commerce application might store:

- `users` — customer information
- `orders` — orders placed by customers
- `products` — products available for purchase
- `order_items` — products belonging to each order
    

A SQL `JOIN` combines related rows from two or more tables into a single result.

The most important JOIN types are:

|JOIN type|Purpose|
|---|---|
|`INNER JOIN`|Return rows with matching records in both tables|
|`LEFT JOIN`|Return all left-table rows, even without matches|
|`CROSS JOIN`|Return every possible combination|
|`SELF JOIN`|Join a table to itself|

We'll use SQLite, with examples that also apply to PostgreSQL.

### Preparing the database

Create three tables representing users, orders, and products.

```sql
PRAGMA foreign_keys = ON;

CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE
);

CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    total REAL NOT NULL,
    status TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE products (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    price REAL NOT NULL
);
```

Insert sample data:

```sql
INSERT INTO users (id, name, email) VALUES
(1, 'Alice', 'alice@example.com'),
(2, 'Bob', 'bob@example.com'),
(3, 'Charlie', 'charlie@example.com'),
(4, 'David', 'david@example.com');

INSERT INTO orders (id, user_id, total, status) VALUES
(101, 1, 120, 'PAID'),
(102, 1, 80, 'PENDING'),
(103, 2, 200, 'PAID'),
(104, 3, 150, 'CANCELLED');

INSERT INTO products (id, name, price) VALUES
(1, 'Laptop', 1200),
(2, 'Mouse', 25),
(3, 'Keyboard', 75);
```

Notice that David has no orders. This will help demonstrate the difference between `INNER JOIN` and `LEFT JOIN`.

## 2 Inner Join — Matching records

`INNER JOIN` returns only rows where the join condition matches records in both tables.

```sql
SELECT
    users.name,
    orders.id AS order_id,
    orders.total
FROM users
INNER JOIN orders
    ON users.id = orders.user_id;
```

Result:

|name|order_id|total|
|---|---|---|
|Alice|101|120|
|Alice|102|80|
|Bob|103|200|
|Charlie|104|150|

David is excluded because he has no matching order.

![[Pasted image 20261113183423.png|537]]

### Using aliases

Aliases make queries shorter and easier to read.

```sql
SELECT
    u.name,
    o.id AS order_id,
    o.total
FROM users u
JOIN orders o
    ON u.id = o.user_id;
```

`INNER JOIN` and `JOIN` are equivalent in this context.

### Filtering joined records

Find users with paid orders:

```sql
SELECT
    u.name,
    o.id,
    o.total
FROM users u
JOIN orders o
    ON u.id = o.user_id
WHERE o.status = 'PAID';
```

Result:

|name|id|total|
|---|---|---|
|Alice|101|120|
|Bob|103|200|

The `JOIN` establishes the relationship, while `WHERE` filters the resulting rows.

## 3. Left Join — Include unmatched records

`LEFT JOIN` returns every row from the left table, even when no matching row exists in the right table.

```sql
SELECT
    u.name,
    o.id AS order_id,
    o.total
FROM users u
LEFT JOIN orders o
    ON u.id = o.user_id;
```

Result:

|name|order_id|total|
|---|---|---|
|Alice|101|120|
|Alice|102|80|
|Bob|103|200|
|Charlie|104|150|
|David|NULL|NULL|

David appears because `users` is the left table.

![[Pasted image 20261113183455.png|546]]

### Find users without orders

A common backend requirement is to find customers who have never placed an order.

```sql
SELECT u.id, u.name
FROM users u
LEFT JOIN orders o
    ON u.id = o.user_id
WHERE o.id IS NULL;
```

Result:

|id|name|
|---|---|
|4|David|

This is an example of an **anti-join pattern**.

An equivalent approach uses `NOT EXISTS`:

```sql
SELECT u.id, u.name
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.user_id = u.id
);
```

`NOT EXISTS` is often useful because it directly expresses that no matching record should exist.

### Important: ON vs WHERE

Consider finding all users, including those without paid orders.

Correct:

```sql
SELECT u.name, o.id
FROM users u
LEFT JOIN orders o
    ON u.id = o.user_id
   AND o.status = 'PAID';
```

This preserves all users.

Compare:

```sql
SELECT u.name, o.id
FROM users u
LEFT JOIN orders o
    ON u.id = o.user_id
WHERE o.status = 'PAID';
```

The second query excludes users without paid orders because their `o.status` is `NULL`, which does not satisfy the `WHERE` condition.

**Key rule:** Conditions placed in `ON` affect matching. Conditions placed in `WHERE` filter rows after the join.

## 4. CROSS JOIN — Every combination

A `CROSS JOIN` produces the Cartesian product of two tables.

```sql
SELECT
    u.name,
    p.name AS product
FROM users u
CROSS JOIN products p;
```

We have four users and three products.

Total result rows

**4 × 3 = 12**

Every user is paired with every product.

Example result:

|user|product|
|---|---|
|Alice|Laptop|
|Alice|Mouse|
|Alice|Keyboard|
|Bob|Laptop|
|Bob|Mouse|
|Bob|Keyboard|
|...|...|

### **Practical use cases**

`CROSS JOIN` is useful for generating combinations such as:

- Products and available sizes
- Employees and calendar dates
- Test cases and configuration variants
    

Be careful with large tables.

If one table contains 10,000 rows and another contains 5,000, a CROSS JOIN produces 50 million combinations before any later filtering.

## 5. SELF JOIN — Joining a table to itself

A self-join connects records within the same table.

A common example is an employee-manager hierarchy.

```sql
CREATE TABLE employees (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    manager_id INTEGER,
    FOREIGN KEY (manager_id)
        REFERENCES employees(id)
);

INSERT INTO employees VALUES
(1, 'Alice', NULL),
(2, 'Bob', 1),
(3, 'Charlie', 1),
(4, 'David', 2);
```

Hierarchy:

![[Pasted image 20261113184056.png|459]]

Find each employee's manager:

```sql
SELECT
    e.name AS employee,
    m.name AS manager
FROM employees e
LEFT JOIN employees m
    ON e.manager_id = m.id;
```

Result:

|employee|manager|
|---|---|
|Alice|NULL|
|Bob|Alice|
|Charlie|Alice|
|David|Bob|

We use two aliases, `e` and `m`, to represent different roles of the same table.

A self-join does not require a special SQL keyword. It is an ordinary join involving the same table twice.

## 6. Many-to-many relationships

So far, users and orders have a one-to-many relationship.

But an order can contain multiple products, and a product can appear in multiple orders.  
This is a many-to-many relationship.

We model it using a junction table called `order_items`.

```sql
CREATE TABLE order_items (
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL
        CHECK (quantity > 0),
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

Insert sample data:

```sql
INSERT INTO order_items VALUES
(101, 2, 2),
(101, 3, 1),
(102, 2, 1),
(103, 1, 1);
```

The relationships are:

![[Pasted image 20261113184237.png|489]]

### Join three tables

Find the products belonging to each order:

```sql
SELECT
    o.id AS order_id,
    p.name AS product,
    oi.quantity,
    p.price,
    oi.quantity * p.price AS subtotal
FROM orders o
JOIN order_items oi
    ON o.id = oi.order_id
JOIN products p
    ON oi.product_id = p.id
ORDER BY o.id, p.name;
```

Result:

|order_id|product|quantity|price|subtotal|
|---|---|---|---|---|
|101|Keyboard|1|75|75|
|101|Mouse|2|25|50|
|102|Mouse|1|25|25|
|103|Laptop|1|1200|1200|

Note that these calculated subtotals do not match all of the sample values stored in `orders.total`. The tables use illustrative data. In a real system, you would define how order totals are calculated and keep them consistent, often storing the price at the time of purchase in `order_items`.

### Join four tables

Find users and the products they purchased:

```sql
SELECT
    u.name AS customer,
    o.id AS order_id,
    p.name AS product,
    oi.quantity
FROM users u
JOIN orders o
    ON u.id = o.user_id
JOIN order_items oi
    ON o.id = oi.order_id
JOIN products p
    ON oi.product_id = p.id
ORDER BY u.name, o.id;
```

This is a common pattern in backend applications when constructing detailed order responses.

## 7. JOIN with aggregation

JOINs are frequently combined with `GROUP BY` and aggregate functions.

### **8.1 Count orders per user**

```sql
SELECT
    u.id,
    u.name,
    COUNT(o.id) AS order_count
FROM users u
LEFT JOIN orders o
    ON u.id = o.user_id
GROUP BY u.id, u.name
ORDER BY u.id;
```

Result:

|user|order_count|
|---|---|
|Alice|2|
|Bob|1|
|Charlie|1|
|David|0|

Why `COUNT(o.id)` instead of `COUNT(*)`?

Because a `LEFT JOIN` still produces one row for David, with `NULL` values for the order columns.

- `COUNT(*)` would count that row as 1.
- `COUNT(o.id)` ignores `NULL` and correctly returns 0.
    

### Calculate total spending per user

```sql
SELECT
    u.name,
    COALESCE(SUM(o.total), 0) AS total_spent
FROM users u
LEFT JOIN orders o
    ON u.id = o.user_id
   AND o.status = 'PAID'
GROUP BY u.id, u.name;
```

`COALESCE` replaces a `NULL` result with zero.

We also filter to paid orders within the `ON` clause so users without paid orders remain in the result.

## 8. JOIN performance and indexes

JOINs can become expensive when tables contain millions of records.

Suppose we frequently run:

```sql
SELECT *
FROM orders o
JOIN users u
    ON o.user_id = u.id
WHERE u.id = 100;
```

The database must locate the user and corresponding orders.

Create an index on the foreign key:

```sql
CREATE INDEX idx_orders_user_id
ON orders(user_id);
```

SQLite automatically indexes primary keys and UNIQUE constraints where necessary, but does not automatically create an index for every foreign key.

An index on `orders.user_id` can make lookups significantly faster.

### Inspect the query plan

SQLite provides `EXPLAIN QUERY PLAN`.

```sql
EXPLAIN QUERY PLAN
SELECT *
FROM orders o
JOIN users u
    ON o.user_id = u.id
WHERE u.id = 100;
```

You may see operations such as:

```
SEARCH u USING INTEGER PRIMARY KEY
SEARCH o USING INDEX idx_orders_user_id
```

This indicates that SQLite can use indexed lookups rather than scanning the entire orders table.

The exact output depends on the database schema, statistics, and SQLite version.

### JOIN complexity

There is no single time complexity for a JOIN. It depends on the algorithm and available indexes.

|Strategy|Approximate complexity|
|---|---|
|Nested-loop join without indexes|O(N × M)|
|Nested-loop join with indexed lookup|O(N × log M), for typical B-tree lookups|
|Hash join|O(N + M) average|
|Sort-merge join|O(N log N + M log M), including sorting|

SQLite primarily uses nested-loop joins, while PostgreSQL can also choose hash joins and merge joins.

The query planner decides which access strategy to use based on the query, indexes, and estimated costs.

## 9. Common JOIN mistakes

### **Mistake 1: Missing join condition**

```sql
SELECT *
FROM users u
JOIN orders o;
```

In SQLite, this produces combinations of users and orders rather than matching them by relationship.

Correct:

```sql
SELECT *
FROM users u
JOIN orders o
    ON u.id = o.user_id;
```

### Mistake 2: Unexpected duplicate rows

```sql
SELECT u.name
FROM users u
JOIN orders o
    ON u.id = o.user_id;
```

Alice appears twice because she has two orders.

This is not a database error. A JOIN can produce multiple rows for one entity.

If you need unique users:

```sql
SELECT DISTINCT u.id, u.name
FROM users u
JOIN orders o
    ON u.id = o.user_id;
```

If you only need to know whether an order exists, `EXISTS` may be more appropriate:

```sql
SELECT u.id, u.name
FROM users u
WHERE EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.user_id = u.id
);
```

### Mistake 3: Ambiguous column names

Both `users` and `orders` contain an `id` column.

This is ambiguous:

```sql
SELECT id
FROM users u
JOIN orders o
    ON u.id = o.user_id;
```

Specify the table alias:

```sql
SELECT u.id, o.id
FROM users u
JOIN orders o
    ON u.id = o.user_id;
```

## 10. Key takeaways

- `INNER JOIN` returns only matching records.
- `LEFT JOIN` preserves all rows from the left table.
- `CROSS JOIN` generates every possible combination.
- `SELF JOIN` allows relationships within the same table.
- Junction tables represent many-to-many relationships.
- `ON` and `WHERE` behave differently with outer joins.
- `COUNT(column)` and `COUNT(*)` can produce different results after a LEFT JOIN.
- Indexes on foreign keys often improve JOIN performance.
- `EXISTS` is useful when checking whether related records exist.
- JOINs can help prevent N+1 queries in backend applications.
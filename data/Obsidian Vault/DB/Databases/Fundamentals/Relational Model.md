
In this lesson, we will cover:

1. Tables, rows, columns, and schemas.
2. Primary keys, foreign keys, and candidate keys.
3. One-to-one, one-to-many, and many-to-many relationships.
4. Referential integrity and constraints.
5. Relational operations: selection, projection, and joins.
6. A complete database implementation using SQLite and PostgreSQL.
7. Practical database design considerations for backend applications.

## 1. What is the relational model?

It represents data using relations, which are mathematically defined sets of tuples.

In practical database terminology:

|Relational concept|SQL terminology|Example|
|---|---|---|
|Relation|Table|`users`|
|Tuple|Row|`(1, 'Alice')`|
|Attribute|Column|`email`|
|Domain|Allowed values|`VARCHAR(255)` plus constraints|
|Relation schema|Table definition|`CREATE TABLE users ...`|
|Key|Identifier|`user_id`|

SQL tables are based on the relational model but differ from mathematical relations in certain ways. For example, SQL permits duplicate rows unless constraints or `DISTINCT` prevent them, and supports `NULL`.

Consider a `users` table:

|id|name|email|
|---|---|---|
|1|Alice|alice@example.com|
|2|Bob|bob@example.com|
|3|Carol|carol@example.com|

Here:

- The relation is `users`.
- Each row represents a user.
- Each column represents an attribute.
- `id` uniquely identifies each user.
    
## 2. Keys in relational databases

Keys are essential for identifying records and establishing relationships.

### 2.1 Primary key

A primary key uniquely identifies each row in a table.

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL
);
```

A primary key must be unique and cannot contain `NULL` in a properly defined relational schema.

### 2.2 Foreign key

A foreign key references a primary key or another eligible unique key.

```sql
CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

The foreign key `orders.user_id` references `users.id`.

One user can have multiple orders.

This relationship ensures that an order cannot reference a nonexistent user when foreign key enforcement is enabled.

### 2.3 Candidate key and unique key

A candidate key is a minimal set of columns that uniquely identifies a row.

For example, if both `id` and `email` are unique and non-null, each can be a candidate key. We select `id` as the primary key and enforce uniqueness on `email`.

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);
```

A SQL `UNIQUE` constraint is not always equivalent to a candidate key because many DBMSs permit multiple `NULL` values in unique columns.

### 2.4 Composite key

A composite key contains multiple columns.

```sql
CREATE TABLE enrollments (
    student_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    enrolled_at TIMESTAMP NOT NULL,

    PRIMARY KEY (student_id, course_id)
);
```

The combination `(student_id, course_id)` must be unique.

This is useful for junction tables that represent many-to-many relationships.

## 3. Types of relationships

**One-to-one (1:1)**  
One user has at most one profile, and each profile belongs to exactly one user.

![[Pasted image 20261106161956.png]]

**One-to-many (1:N)**   
One customer can place multiple orders, but each order belongs to one customer.

![[Pasted image 20261106162041.png]]

**Many-to-many (N:M)**   
A student can enroll in multiple courses, and a course can contain multiple students. A junction table represents the relationship.

![[Pasted image 20261106162109.png]]

### One-to-one SQL

```sql
CREATE TABLE profiles (
    user_id INTEGER PRIMARY KEY,
    bio VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

Making `user_id` the primary key prevents multiple profiles from referencing the same user.

### Many-to-many SQL

```sql
CREATE TABLE students (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE courses (
    id INTEGER PRIMARY KEY,
    title VARCHAR(100) NOT NULL
);

CREATE TABLE enrollments (
    student_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,

    PRIMARY KEY (student_id, course_id),

    FOREIGN KEY (student_id)
        REFERENCES students(id),

    FOREIGN KEY (course_id)
        REFERENCES courses(id)
);
```

The junction table `enrollments` contains two foreign keys and a composite primary key.

## 4. Referential integrity and constraints

Constraints protect data integrity at the database level, even if application validation is missing.

|Constraint|Purpose|
|---|---|
|`PRIMARY KEY`|Unique row identifier|
|`FOREIGN KEY`|Valid relationship between tables|
|`UNIQUE`|Prevent duplicate values|
|`NOT NULL`|Require a value|
|`CHECK`|Enforce a condition|
|`DEFAULT`|Supply a default value|

Example:

```sql
CREATE TABLE products (
    id INTEGER PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

This ensures that every product has a SKU, the SKU is unique, and the price cannot be negative.

### Foreign key deletion rules

When deleting a referenced row, databases can apply different policies.

```sql
CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
```

Common options:

- `CASCADE`: delete dependent rows automatically.
- `RESTRICT`: reject deletion while dependent rows exist.
- `SET NULL`: clear the foreign key, provided the column permits `NULL`.
- `NO ACTION`: enforce the constraint, potentially at a deferred check point where supported.


Be especially careful with `CASCADE` in business-critical applications. Deleting a customer should not automatically erase financial transaction history unless that behavior is explicitly required.

## 5. Relational operations

Three fundamental operations are selection, projection, and join.

### Selection — filter rows

```sql
SELECT *
FROM products
WHERE price > 100;
```

### Projection — choose columns

```sql
SELECT name, price
FROM products;
```

Unlike mathematical projection, SQL projection can preserve duplicate results. Use `DISTINCT` when duplicate elimination is required.

### Join — combine related data

```sql
SELECT
    u.name,
    o.id AS order_id
FROM users u
JOIN orders o
    ON o.user_id = u.id;
```

Joins allow us to reconstruct related information without storing redundant user details in every order.

## 6. Complete practical example — Order management

Let's implement a small order management database with customers, products, orders, and order items.

Entity relationship diagram — order management:

![[Pasted image 20261106162727.png]]

Install JetBrains DataGrip on Ubuntu:

```sh
sudo snap install datagrip --classic
```

### Step 1 — Create the schema

This example uses explicit integer identifiers so it runs on both SQLite and PostgreSQL.

```sql
CREATE TABLE customers (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE products (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0)
);

CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE order_items (
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

Notice that `order_items` stores `unit_price`, even though the `products` table already has `price`.

This is intentional: the product price can change, but the historical price paid for an order must remain unchanged.

### Step 2 — Insert sample data

```sql
INSERT INTO customers (id, name, email)
VALUES
    (1, 'Alice', 'alice@example.com'),
    (2, 'Bob', 'bob@example.com');

INSERT INTO products (id, name, price)
VALUES
    (1, 'Laptop', 1200.00),
    (2, 'Monitor', 300.00),
    (3, 'Keyboard', 80.00);

INSERT INTO orders (id, customer_id)
VALUES
    (101, 1),
    (102, 2);

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
    (101, 1, 1, 1200.00),
    (101, 3, 2, 80.00),
    (102, 2, 2, 300.00);
```

### Step 3 — Calculate order totals

```sql
SELECT
    o.id AS order_id,
    c.name AS customer,
    SUM(oi.quantity * oi.unit_price) AS total
FROM orders o
JOIN customers c
    ON c.id = o.customer_id
JOIN order_items oi
    ON oi.order_id = o.id
GROUP BY o.id, c.name
ORDER BY o.id;
```

Expected output:

|order_id|customer|total|
|---|---|---|
|101|Alice|1360.00|
|102|Bob|600.00|

The query combines three tables, calculates each line item's cost, and aggregates the total for each order.
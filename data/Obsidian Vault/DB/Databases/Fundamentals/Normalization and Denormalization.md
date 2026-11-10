## 1. Introduction

Database normalization is the process of organizing data into tables to reduce redundancy, prevent inconsistencies, and improve data integrity.  

Denormalization is the deliberate introduction of redundancy to improve read performance or simplify certain queries.  

Both techniques are important for backend engineers designing relational databases.   
    
## 2. Why normalization matters

Consider an e-commerce application storing customers and orders.

An initial design might use a single table:

**orders**

|order_id|customer_name|email|product|price|
|---|---|---|---|---|
|101|Alice|alice@mail.com|Laptop|1200|
|102|Alice|alice@mail.com|Mouse|30|
|103|Bob|bob@mail.com|Keyboard|80|

This design has several problems.

The customer's name and email are repeated for every order. If Alice changes her email, multiple rows must be updated.

Furthermore, customer information cannot be stored independently of an order.

Normalization addresses these problems by separating different entities.

![[Pasted image 20261109125929.png|686]]

Now customer information is stored once and referenced through a foreign key.

This improves consistency and reduces unnecessary duplication.

## 3. Data anomalies

Poorly normalized databases can suffer from three major anomalies.

### 3.1 Update anomaly

Suppose Alice changes her email.

```sql
UPDATE orders
SET email = 'alice@newmail.com'
WHERE customer_name = 'Alice';
```

If one row is missed, the database contains conflicting customer information.

### 3.2 Insert anomaly

Suppose a new customer registers but has not placed an order.

If customer information exists only in the orders table, inserting the customer may require creating a fake order or leaving order-related columns empty.

### 3.3 Delete anomaly

Suppose Bob has only one order.

```sql
DELETE FROM orders
WHERE order_id = 103;
```

Deleting that order also removes Bob's customer information if it exists nowhere else.

|Anomaly|Problem|
|---|---|
|Update|Same information must be modified in multiple places|
|Insert|Cannot store one fact without another unrelated fact|
|Delete|Removing one fact unintentionally removes another|

These anomalies are a primary motivation for normalization.

## 4. Functional dependencies

A functional dependency describes a relationship between attributes.

If attribute A determines attribute B, we write:

```
A -> B
```

This means that whenever two rows have the same value of A, they must have the same value of B.

Consider:

|customer_id|name|email|
|---|---|---|
|1|Alice|alice@mail.com|
|2|Bob|bob@mail.com|

We can define:

```
customer_id -> name
customer_id -> email
```

The customer ID uniquely determines the name and email.

However, the reverse isn't always true, name do not determine the customer_id.

Two customers may have the same name.

### Composite dependencies

Consider an order items table:

|order_id|product_id|quantity|
|---|---|---|
|101|10|2|
|101|20|1|
|102|10|3|

Assuming one row per product in an order:

```
(order_id, product_id) -> quantity
```

Neither `order_id` nor `product_id` alone determines the quantity.

The combination does.

Functional dependencies are based on business rules, not merely patterns observed in sample data.

## 5. First Normal Form (1NF)

A table satisfies 1NF when each column contains a single value from its defined domain, and rows can be uniquely identified.

In practical relational design, avoid repeating groups and storing lists of independent values in one column.

### Incorrect design

|customer_id|name|phones|
|---|---|---|
|1|Alice|111, 222|
|2|Bob|333|

The `phones` column contains multiple phone numbers.

This makes querying and updating individual phone numbers difficult.

For example:

```sql
SELECT *
FROM customers
WHERE phones LIKE '%222%';
```

This is unreliable because substring matches can produce false positives.

### Normalized design

customers

|customer_id|name|
|---|---|
|1|Alice|
|2|Bob|

customer_phones

|customer_id|phone|
|---|---|
|1|111|
|1|222|
|2|333|

SQL implementation:

```sqlite
CREATE TABLE customers (
    customer_id INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE customer_phones (
    customer_id INTEGER NOT NULL,
    phone TEXT NOT NULL,

    PRIMARY KEY (customer_id, phone),

    FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
);
```

SQLite requires foreign-key enforcement to be enabled for each connection:

```sqlite
PRAGMA foreign_keys = ON;
```

Now each phone number is independently searchable.

```sqlite
SELECT c.name, p.phone
FROM customers c
JOIN customer_phones p
    ON c.customer_id = p.customer_id;
```

Note: A JSON or array column is not automatically a violation of 1NF under every theoretical interpretation. The important design question is whether its contents need independent relational querying, constraints, and updates.

## 6. Second Normal Form (2NF)

A table satisfies 2NF when:

1. It satisfies 1NF.
2. Every non-prime attribute depends on the entire candidate key, not just part of a composite candidate key.
    

A non-prime attribute is an attribute that does not belong to any candidate key.

### Example: order items

Consider:

|order_id|product_id|product_name|quantity|
|---|---|---|---|
|101|10|Laptop|2|
|101|20|Mouse|1|
|102|10|Laptop|3|

The primary key is:

```sqlite
PRIMARY KEY (order_id, product_id)
```

Functional dependencies:

```
(order_id, product_id) -> quantity
```

But product_id does not determine product_name.    
Product name depends only on part of composite key.  

This is called a partial dependency.

### Solution

Separate product information into its own table.

```sqlite
CREATE TABLE products (
    product_id INTEGER PRIMARY KEY,
    product_name TEXT NOT NULL
);

CREATE TABLE order_items (
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,

    PRIMARY KEY (order_id, product_id),

    FOREIGN KEY (product_id)
        REFERENCES products(product_id)
);
```

Now:

- Product information is stored in `products`.
- Order-specific quantities are stored in `order_items`.
- Product names are no longer duplicated across orders.

## 7. Complete normalization example

Let's design a small e-commerce database in 3NF.

We need customers, products, orders, and order items.

![[Pasted image 20261109163217.png|546]]

### PostgreSQL implementation

```postgresql
CREATE TABLE customers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE products (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    current_price NUMERIC(12,2) NOT NULL
        CHECK (current_price >= 0)
);

CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id BIGINT NOT NULL
        REFERENCES customers(id),
    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    order_id BIGINT NOT NULL
        REFERENCES orders(id),
    product_id BIGINT NOT NULL
        REFERENCES products(id),
    quantity INTEGER NOT NULL
        CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL
        CHECK (unit_price >= 0),

    PRIMARY KEY (order_id, product_id)
);
```

Notice that `order_items` contains `unit_price`, even though `products` already has `current_price`.

This is intentional.

The product's current price may change, but the price charged for an existing order must remain unchanged.

These are two different business facts:

- `products.current_price`: the price currently offered.
- `order_items.unit_price`: the price agreed upon at purchase time.
    
Therefore, this is not necessarily a normalization violation.

### SQLite differences

SQLite does not support PostgreSQL's `GENERATED ALWAYS AS IDENTITY` syntax.

Use:

```sqlite
CREATE TABLE products (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    current_price INTEGER NOT NULL
        CHECK (current_price >= 0)
);
```

Here `current_price` is stored as integer cents to avoid floating-point rounding problems.

For example, 1299 represents 12.99 in the chosen currency.

PostgreSQL's `NUMERIC(12,2)` is suitable for exact decimal monetary values. SQLite's `NUMERIC` affinity does not provide the same fixed-precision decimal guarantees.

## 8. Denormalization

Normalization improves consistency, but it can require additional joins or calculations.

Denormalization deliberately stores redundant or derived information to optimize certain operations.

Consider an e-commerce application displaying the total amount of each order.

With the normalized design:

```postgresql
SELECT
    order_id,
    SUM(quantity * unit_price) AS total
FROM order_items
GROUP BY order_id;
```

This is correct, but repeatedly calculating totals across large numbers of order items can become expensive.

One option is to store the total directly in `orders`.

```postgresql
ALTER TABLE orders
ADD COLUMN total_amount NUMERIC(12,2);
```

Now:

```postgresql
SELECT id, total_amount
FROM orders;
```

This avoids recalculating the total for every read.

However, it introduces redundancy.

If an order item changes, the stored total must also be updated.

## 9. When should you denormalize?

Denormalization should usually be introduced after identifying a measurable performance problem.

Typical use cases include:

- Reporting dashboards that repeatedly aggregate millions of rows.
- E-commerce order summaries.
- Precomputed statistics and counters.
- Search-oriented read models.
- Analytics systems with expensive joins.
- Frequently accessed historical snapshots.
    
For example, a learning application might store review records in a normalized structure:

```java
reviews (
    id,
    user_id,
    topic_id,
    score,
    reviewed_at
)
```

A dashboard could calculate:

```sql
SELECT
    user_id,
    AVG(score) AS average_score,
    COUNT(*) AS review_count
FROM reviews
GROUP BY user_id;
```

For a small dataset, this is perfectly reasonable.

For millions of reviews and frequent dashboard requests, a precomputed summary might help:

```sql
CREATE TABLE user_review_stats (
    user_id BIGINT PRIMARY KEY,
    review_count BIGINT NOT NULL,
    total_score NUMERIC(18,4) NOT NULL
);
```

This avoids scanning all review records for every dashboard request.

The tradeoff is that the summary must remain synchronized with the source data.
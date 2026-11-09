## 1. Introduction

Data modeling is the process of designing how information is organized, stored, and related inside a database.

Before creating tables, a software engineer should understand:

- What entities exist in the application?
- What attributes describe each entity?
- How are entities related?
- Which constraints protect data integrity?
- How can the schema support future application requirements?
    

A well-designed database reduces data duplication, prevents inconsistent records, and makes queries easier to maintain.

We'll use an e-commerce application as our practical example, with customers, orders, products, and payments.

## 2. Entities and Attributes

An entity represents a real-world object or concept that the application needs to store.

Examples:

|Entity|Attributes|
|---|---|
|Customer|id, name, email|
|Product|id, name, price|
|Order|id, customer_id, created_at|
|Payment|id, order_id, amount|

In a relational database:

- An entity is usually represented by a table.
- An attribute is represented by a column.
- An entity instance is represented by a row.
    

Example:

```postgresql
CREATE TABLE customers (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE
);
```

Each row represents one customer.

```postgresql
INSERT INTO customers (id, name, email)
VALUES
    (1, 'Alice', 'alice@example.com'),
    (2, 'Bob', 'bob@example.com');
    
SELECT * FROM customers;
```

The result:

|id|name|email|
|---|---|---|
|1|Alice|alice@example.com|
|2|Bob|bob@example.com|

### Entity vs. attribute

Suppose a customer has multiple addresses.

One possible design:

```sql
CREATE TABLE customers (
    id INTEGER PRIMARY KEY,
    name TEXT,
    address1 TEXT,
    address2 TEXT,
    address3 TEXT
);
```

This is generally a poor design because it introduces an arbitrary limit of three addresses and makes address management difficult.

A better approach is to create a separate entity:

```sql
CREATE TABLE addresses (
    id INTEGER PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    street TEXT NOT NULL,
    city TEXT NOT NULL,
    FOREIGN KEY (customer_id)
        REFERENCES customers(id)
);
```

Now a customer can have any number of addresses.

Key principle: When a concept has its own attributes, lifecycle, or multiple occurrences, consider modeling it as a separate entity.

## 3. Primary Keys

A primary key (PK) uniquely identifies each row in a table.

```postgresql
CREATE TABLE products (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    price NUMERIC NOT NULL
);
```

Properties of a primary key:

- Must be unique.
- Cannot contain NULL.
- Each table can have only one primary key constraint.
- Can consist of one or multiple columns.
    

### Natural vs. surrogate keys

A natural key uses meaningful business data.

```sql
CREATE TABLE countries (
    country_code CHAR(2) PRIMARY KEY,
    name TEXT NOT NULL
);
```

Examples: `RO`, `DE`, `FR`.

A surrogate key is generated specifically for identification.

```sql
CREATE TABLE customers (
    id INTEGER PRIMARY KEY,
    email TEXT NOT NULL UNIQUE
);
```

Here `id` is a surrogate key, while `email` is a business attribute with a uniqueness constraint.

Surrogate keys are often preferred because business data can change.

For example, a customer might change their email address, but their database ID remains stable.

### Composite primary keys

A primary key can contain multiple columns.

```postgresql
CREATE TABLE order_items (
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,

    PRIMARY KEY (order_id, product_id)
);
```

The pair `(order_id, product_id)` must be unique.

This design means the same product can appear only once per order, with quantity representing multiple units.

## 4. Foreign Keys

A foreign key (FK) establishes a relationship between tables.

Consider customers and orders.

```sql
CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    created_at TEXT NOT NULL,

    FOREIGN KEY (customer_id)
        REFERENCES customers(id)
);
```

The `customer_id` column references `customers.id`.

![[Pasted image 20261107155824.png]]

One customer can have many orders; each order references one customer.

A foreign key prevents an order from referencing a nonexistent customer, provided foreign key enforcement is enabled.

```postgresql
INSERT INTO orders (id, customer_id, created_at)
VALUES (101, 999, '2026-11-07');
```

If customer `999` does not exist, this insertion fails.

### Referential actions

Foreign keys can define what happens when a referenced row is deleted.

|Action|Behavior|
|---|---|
|`RESTRICT`|Prevent deletion while dependent rows exist|
|`CASCADE`|Automatically delete dependent rows|
|`SET NULL`|Set referencing columns to NULL|
|`SET DEFAULT`|Set referencing columns to their defaults|
|`NO ACTION`|Raise a constraint error if the relationship remains invalid when checked|

Example:

```sql
FOREIGN KEY (customer_id)
    REFERENCES customers(id)
    ON DELETE CASCADE
```

Deleting a customer would also delete their orders.

For business applications, this can be dangerous. Financial and historical records often need to remain available even if a customer account is deactivated.

Practical recommendation: Choose referential actions based on business rules, not convenience.

## 5. One-to-One Relationships (1:1)

A one-to-one relationship means that one record in table A is associated with at most one record in table B, and vice versa.

Example: Customer and Customer Profile.

![[Pasted image 20261107155912.png|459]]

```postgresql
CREATE TABLE customer_profiles (
    customer_id INTEGER PRIMARY KEY,
    date_of_birth TEXT,
    phone TEXT,

    FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON DELETE CASCADE
);
```

Notice that `customer_id` is both a primary key and a foreign key.

This guarantees:

- A profile belongs to exactly one customer.
- A customer can have at most one profile.
- A customer may exist without a profile.
    

Common applications include user preferences, optional configuration, and separating sensitive or infrequently accessed data.

## 6. One-to-Many Relationships (1:N)

A one-to-many relationship means one record in table A can have multiple related records in table B.

Example: One customer can place many orders.

![[Pasted image 20261107155954.png]]

Customer 1 → Orders 101, 102, 103

```postgresql
CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    status TEXT NOT NULL,

    FOREIGN KEY (customer_id)
        REFERENCES customers(id)
);
```

Query all orders belonging to a customer:

```postgresql
SELECT
    c.name,
    o.id AS order_id,
    o.status
FROM customers c
JOIN orders o
    ON o.customer_id = c.id
WHERE c.id = 1;
```

### Where should the foreign key go?

In a one-to-many relationship, the foreign key normally belongs to the many side.

For example, `orders.customer_id`, not `customers.order_id`.

This is one of the most important rules in relational modeling.

## 7. Many-to-Many Relationships (N:M)

A many-to-many relationship means:

- One order can contain many products.
- One product can appear in many orders.
    
A relational database normally represents this using a junction table.

![[Pasted image 20261107160038.png|565]]


```postgresql
CREATE TABLE order_items (
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),

    PRIMARY KEY (order_id, product_id),

    FOREIGN KEY (order_id)
        REFERENCES orders(id),

    FOREIGN KEY (product_id)
        REFERENCES products(id)
);
```

Example records:

|order_id|product_id|quantity|
|---|---|---|
|101|10|2|
|101|20|1|
|102|10|3|

Order 101 contains two products, and product 10 appears in two different orders.


## 8. Complete Practical Schema — SQLite

Let's build a small e-commerce database.

The following script can be executed in DataGrip using a SQLite database connection.

```sqlite
PRAGMA foreign_keys = ON;

CREATE TABLE customers (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE
);

CREATE TABLE products (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    price NUMERIC NOT NULL CHECK (price >= 0)
);

CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    created_at TEXT NOT NULL
        DEFAULT CURRENT_TIMESTAMP,
    status TEXT NOT NULL DEFAULT 'NEW',

    FOREIGN KEY (customer_id)
        REFERENCES customers(id)
);

CREATE TABLE order_items (
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC NOT NULL CHECK (unit_price >= 0),

    PRIMARY KEY (order_id, product_id),

    FOREIGN KEY (order_id)
        REFERENCES orders(id),

    FOREIGN KEY (product_id)
        REFERENCES products(id)
);

CREATE INDEX idx_orders_customer_id
    ON orders(customer_id);

CREATE INDEX idx_order_items_product_id
    ON order_items(product_id);
```

Important SQLite details:

- `INTEGER PRIMARY KEY` acts as an alias for the rowid in ordinary rowid tables, allowing automatic ID assignment.
    
- `PRAGMA foreign_keys = ON` enables foreign key enforcement for the current connection. It should be set outside an active transaction.
    
- SQLite has flexible type affinity; `NUMERIC` does not guarantee fixed decimal precision.
    
- For monetary values requiring exact arithmetic, storing integer minor units (such as cents) is often safer.
    
### Insert sample data

```sqlite
INSERT INTO customers (name, email)
VALUES
    ('Alice', 'alice@example.com'),
    ('Bob', 'bob@example.com');

INSERT INTO products (name, price)
VALUES
    ('Laptop', 1200.00),
    ('Mouse', 25.00),
    ('Keyboard', 75.00);

INSERT INTO orders (customer_id, status)
VALUES
    (1, 'PAID'),
    (1, 'NEW'),
    (2, 'PAID');

INSERT INTO order_items
    (order_id, product_id, quantity, unit_price)
VALUES
    (1, 1, 1, 1200.00),
    (1, 2, 2, 25.00),
    (2, 3, 1, 75.00),
    (3, 2, 1, 25.00);
```

### Query: Customers and their orders

```sqlite
SELECT
    c.name,
    o.id AS order_id,
    o.status
FROM customers c
JOIN orders o
    ON c.id = o.customer_id
ORDER BY c.name, o.id;
```

### Query: Total value of each order

```sqlite
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

Expected results:

|order_id|customer|total|
|---|---|---|
|1|Alice|1250.00|
|2|Alice|75.00|
|3|Bob|25.00|

Notice that we store `unit_price` in `order_items`, even though the product already has a price.

This is intentional: product prices change over time, but historical order totals must remain accurate.

## 9. PostgreSQL Implementation

PostgreSQL follows the same relational modeling principles but provides stronger typing and additional schema capabilities.

A PostgreSQL version of our schema:

```postgresql
CREATE TABLE customers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE products (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0)
);

CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',

    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
);

CREATE TABLE order_items (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL
        CHECK (unit_price >= 0),

    PRIMARY KEY (order_id, product_id),

    FOREIGN KEY (order_id)
        REFERENCES orders(id),

    FOREIGN KEY (product_id)
        REFERENCES products(id)
);

CREATE INDEX idx_orders_customer_id
    ON orders(customer_id);

CREATE INDEX idx_order_items_product_id
    ON order_items(product_id);
```

### SQLite vs. PostgreSQL

|Feature|SQLite|PostgreSQL|
|---|---|---|
|Generated IDs|`INTEGER PRIMARY KEY`|`GENERATED AS IDENTITY`|
|Numeric precision|Flexible affinity|`NUMERIC(12,2)`|
|Timestamps|Often stored as TEXT|`TIMESTAMPTZ`|
|Foreign keys|Enable per connection|Enforced by default|
|Schema namespaces|No comparable named schemas|Supports multiple schemas|
|Altering tables|More limited|Extensive DDL support|

In PostgreSQL, `NUMERIC(12,2)` supports exact decimal storage suitable for many financial calculations.

`TIMESTAMPTZ` stores an absolute instant and converts its display according to the session time zone.

## 10. Relationship Cardinality and Optionality

Two important modeling concepts are cardinality and optionality.

Cardinality describes how many records can participate in a relationship.

Optionality describes whether participation is required.

|Notation|Meaning|
|---|---|
|0..1|Zero or one|
|1..1|Exactly one|
|0..N|Zero or many|
|1..N|One or many|

For example:

- A customer can have zero or many orders.
- Every order must belong to exactly one customer.
    

This can be represented as:

```
Customer (1) -------- (0..N) Order
```

In SQL, `orders.customer_id NOT NULL` ensures that an order must reference a customer.

However, SQL foreign keys alone do not guarantee that every customer has at least one order.

This distinction matters when translating business requirements into constraints.

## 11. Common Data Modeling Mistakes

### Mistake 1: Storing multiple values in one column

```sql
CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    product_ids TEXT
);
```

Example: `product_ids = '1,2,5'`

Problems include difficult joins, weak integrity guarantees, and complicated updates.

Use a junction table instead.

### Mistake 2: Missing foreign keys

```sql
CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    customer_id INTEGER
);
```

Without a foreign key, invalid customer IDs can be stored.

Application validation is useful, but database constraints provide protection across every application and client writing to the database.

### Mistake 3: Duplicating business information

```sql
CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    customer_id INTEGER,
    customer_email TEXT,
    customer_name TEXT
);
```

Duplicating customer information may lead to inconsistent data.

Prefer references unless duplication serves a specific purpose, such as storing an immutable billing snapshot.

### Mistake 4: Using inappropriate cascading deletes

```sql
FOREIGN KEY (customer_id)
    REFERENCES customers(id)
    ON DELETE CASCADE
```

For historical financial records, automatically deleting orders when a customer is deleted is often unacceptable.

A better strategy may be to deactivate or anonymize the customer while preserving records required by business and legal rules.

### Mistake 5: Missing indexes on foreign keys

PostgreSQL does not automatically create indexes on referencing foreign key columns.

For example:

```sql
CREATE INDEX idx_orders_customer_id
    ON orders(customer_id);
```

This can improve joins, filtering, and referential integrity operations.

Indexes also introduce storage and write overhead, so they should be selected based on actual queries.

## 12. Spring Boot and JPA

In Java applications, relational relationships are commonly represented through JPA entity mappings.

Example:

```java
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @OneToMany(mappedBy = "customer")
    private List<Order> orders = new ArrayList<>();
}
```

And the order entity:

```java
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_id",
        nullable = false
    )
    private Customer customer;
}
```

This models the same relationship as:

```sql
orders.customer_id
    REFERENCES customers(id)
```

Important distinctions:

- `@ManyToOne` defines the owning side of the relationship.
- `@OneToMany(mappedBy = "customer")` defines the inverse side.
- `FetchType.LAZY` defers loading the customer association until it is accessed, subject to the persistence context and provider behavior.
- JPA cascade operations and database `ON DELETE CASCADE` are different mechanisms.
    
For production applications, database schemas are often managed through Flyway or Liquibase rather than relying on automatic Hibernate schema generation.

## 13. Key Takeaways

- Entities represent business concepts; attributes describe them.
- Primary keys uniquely identify records.
- Foreign keys connect records and enforce referential integrity.
- One-to-one relationships commonly use a unique foreign key or a shared primary key.
- One-to-many relationships place the foreign key on the many side.
- Many-to-many relationships require a junction table in a conventional normalized relational schema.
- Cardinality and optionality express business rules that must be considered before writing SQL.
- Database constraints protect data independently of application code.
- SQLite and PostgreSQL use the same core relational modeling principles, with differences in typing, ID generation, and advanced capabilities.
    

The most important habit in data modeling is to identify entities, relationships, and business rules before creating tables.
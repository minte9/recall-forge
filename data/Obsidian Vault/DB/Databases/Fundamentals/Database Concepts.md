## 1. What is a Database?

A database is an organized collection of data that can be stored, retrieved, modified, and managed efficiently. Unlike ordinary files, databases typically provide structured querying, consistency mechanisms, concurrent access, and recovery capabilities.

A Database Management System (DBMS) is the software responsible for managing databases. PostgreSQL, SQLite, Oracle, and MongoDB are examples of DBMS products.

Simplified application-to-database architecture:

![[Pasted image 20261106152457.png]]

A DBMS typically provides several capabilities:

- Persistence: data survives application restarts.
- Querying: data can be retrieved using a query language.
- Concurrency: multiple operations can access data safely.
- Transactions: related operations can succeed or fail as a unit.
- Recovery: mechanisms help restore consistency after failures.

## 2. Relational Databases

A relational database organizes data into tables consisting of rows and columns. Tables can be connected through relationships, usually represented by primary and foreign keys.

Consider a simple application containing users and their orders.

|id (PK)|name|
|---|---|
|1|Alice|
|2|Bob|

One user can have many orders:

|id (PK)|user_id (FK)|total|
|---|---|---|
|101|1|150.00|
|102|1|75.00|
|103|2|200.00|

The `user_id` column in `orders` references `users.id`. This relationship allows the database to enforce referential integrity.

Here is the corresponding SQL, compatible with SQLite and PostgreSQL:

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO users (id, name)
VALUES (1, 'Alice'), (2, 'Bob');

INSERT INTO orders (id, user_id, total)
VALUES
    (101, 1, 150.00),
    (102, 1, 75.00),
    (103, 2, 200.00);
```

We can retrieve orders together with their owners using a `JOIN`:

```sql
SELECT
    u.name,
    o.id AS order_id,
    o.total
FROM users u
JOIN orders o ON o.user_id = u.id;
```

Expected result

|name|order_id|total|
|---|---|---|
|Alice|101|150.00|
|Alice|102|75.00|
|Bob|103|200.00|

## 3. Relational vs. NoSQL Databases

Relational databases use tables and SQL, while NoSQL databases support alternative data models such as documents, graphs, and key-value pairs.

NoSQL does not necessarily mean the absence of SQL-like querying, transactions, or schemas. The main distinction is the underlying data model and how applications access the data.

|Database|Model|Typical use|
|---|---|---|
|SQLite|Relational|Embedded applications|
|PostgreSQL|Relational|Backend applications|
|Oracle|Relational|Enterprise systems|
|MongoDB|Document|Flexible document data|
|Neo4j|Graph|Highly connected data|
|Redis|Key-value / data structures|Caching, fast access|

For example, the same user can be represented as a relational row or a MongoDB document.

Users table (relational):

|id|name|
|---|---|
|1|Alice|

Users collection (document):

```json
{
  "id": 1,
  "name": "Alice"
}
```

The choice of database depends on data relationships, query patterns, consistency requirements, scalability, and operational constraints.

## 4. Practical exercise — SQLite

SQLite is a good starting point because it runs without a separate database server.

On Ubuntu:

```sh
sudo apt install sqlite3

sqlite3 learning.db
```

Inside the SQLite shell:

```sql
PRAGMA foreign_keys = ON;

CREATE TABLE products (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    price REAL NOT NULL
);

INSERT INTO products (name, price)
VALUES
    ('Laptop', 1200),
    ('Monitor', 300),
    ('Keyboard', 80);

SELECT * FROM products;

SELECT name, price
FROM products
WHERE price > 100
ORDER BY price DESC;
```

The final query returns Laptop and Monitor, sorted by descending price.

```
Laptop|1200.0
Monitor|300.0
```

Use `.quit` to exit SQLite. 

```sh
.quit
```

Your data remains stored in `learning.db`.
## 1. What is SQL?

SQL (Structured Query Language) is a declarative language used to interact with relational databases.

Unlike Java, where you describe how to perform operations, SQL generally describes what data you want.

For example:

```sql
SELECT name, email
FROM customers
WHERE active = 1;
```

This query retrieves the names and emails of active customers.

SQLite determines how to execute the query internally.

### Create a learning database

We'll start with a `customers` table.

```sql
CREATE TABLE customers (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    city TEXT,
    active INTEGER DEFAULT 1
);
```

Insert sample data:

```sql
INSERT INTO customers (name, email, city, active)
VALUES
('Alice', 'alice@example.com', 'London', 1),
('Bob', 'bob@example.com', 'Paris', 1),
('Charlie', 'charlie@example.com', 'London', 0),
('Diana', 'diana@example.com', 'Berlin', 1),
('Eve', 'eve@example.com', 'Paris', 1);
```

In SQLite, Boolean values are commonly represented by integers: `1` for true and `0` for false.

## 2. SELECT — Retrieve data

The `SELECT` statement retrieves data from one or more tables.

```sql
SELECT * FROM customers;
```

Returns all columns.

```sql
SELECT name, city
FROM customers;
```

Returns only the requested columns.

|name|city|
|---|---|
|Alice|London|
|Bob|Paris|
|Charlie|London|
|Diana|Berlin|
|Eve|Paris|

Backend practice: Avoid `SELECT *` when an API only needs specific fields. Selecting fewer columns can reduce unnecessary data transfer and processing.

### WHERE — Filter rows

Use `WHERE` to select rows satisfying a condition.

```sql
SELECT name, city
FROM customers
WHERE city = 'London';
```

Result:

|name|city|
|---|---|
|Alice|London|
|Charlie|London|

Combine conditions:

```sql
SELECT name, city
FROM customers
WHERE city = 'London'
  AND active = 1;
```

Returns only Alice.

### ORDER BY — Sort results

```sql
SELECT name, city
FROM customers
ORDER BY name ASC;
```

`ASC` means ascending; `DESC` means descending.

You can sort by multiple columns:

```sql
SELECT name, city
FROM customers
ORDER BY city ASC, name DESC;
```

The database sorts by city first, then by name within each city.

## 3. LIMIT and OFFSET — Pagination

```sql
SELECT id, name
FROM customers
ORDER BY id
LIMIT 2 OFFSET 2;
```

This is commonly used in REST APIs:

`GET /api/customers?page=2&size=20`

For large datasets, keyset pagination is often more efficient than large offsets. We'll cover that later.

### DISTINCT — Remove duplicates

```sql
SELECT DISTINCT city
FROM customers
ORDER BY city;
```

Result:

```
Berlin
London
Paris
```

`DISTINCT` eliminates duplicate combinations of selected values.

## 4. SQL logical execution order

Although SQL is written starting with `SELECT`, its logical processing order is different.

1. FROM - Identify source tables
2. WHERE - Filter individual rows
3. GROUP BY - Form groups
4. HAVING - Filter groups
5. SELECT - Compute output expressions
6. DISTINCT - Remove duplicate rows
7. ORDER BY - Sort results
8. LIMIT / OFFSET - Select result window

This is the conceptual order, not necessarily the physical execution order chosen by SQLite's query optimizer.
## 1. Introduction

Data Manipulation Language (DML) is the part of SQL responsible for modifying the data stored in database tables.

The four main operations are commonly known as CRUD:

|Operation|SQL statement|Purpose|
|---|---|---|
|Create|`INSERT`|Add new records|
|Read|`SELECT`|Retrieve records|
|Update|`UPDATE`|Modify existing records|
|Delete|`DELETE`|Remove records|

In backend applications, these operations are fundamental to implementing REST APIs, persistence services, and business workflows.

For example:

```
POST   /api/users       → INSERT
GET    /api/users/10    → SELECT
PUT    /api/users/10    → UPDATE
DELETE /api/users/10    → DELETE
```

We will use SQLite for the practical examples. Most concepts also apply to PostgreSQL.

### Preparing the database

Create a simple database containing users and orders.

```sql
PRAGMA foreign_keys = ON;

CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    balance REAL NOT NULL DEFAULT 0
        CHECK (balance >= 0),
    created_at TEXT NOT NULL
        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    total REAL NOT NULL CHECK (total >= 0),
    status TEXT NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

Important constraints:

- `PRIMARY KEY` uniquely identifies each row.
- `UNIQUE` prevents duplicate email addresses.
- `NOT NULL` requires a value.
- `DEFAULT` supplies a value when one is omitted.
- `CHECK` enforces a condition.
- `FOREIGN KEY` maintains relationships between tables.
    

SQLite foreign-key enforcement must be enabled for each connection, outside an active transaction.

## 2. INSERT — Creating records

`INSERT` adds one or more rows to a table.

### Insert a single row

```sql
INSERT INTO users (email, name, balance)
VALUES ('alice@example.com', 'Alice', 100);
```

SQLite automatically generates the `id` and supplies `created_at`.

Result:

|id|email|name|balance|
|---|---|---|---|
|1|alice@example.com|Alice|100|

### Insert multiple rows

```sql
INSERT INTO users (email, name, balance)
VALUES
    ('bob@example.com', 'Bob', 200),
    ('charlie@example.com', 'Charlie', 150);
```

This is generally more efficient than issuing a separate statement for each row, particularly when database round trips are expensive.

### INSERT with SELECT

You can insert data obtained from another query.

```sql
CREATE TABLE premium_users (
    user_id INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);

INSERT INTO premium_users (user_id, name)
SELECT id, name
FROM users
WHERE balance >= 150;
```

This is useful for migrations, data transformations, and batch processing.

### INSERT with RETURNING

SQLite 3.35+ supports `RETURNING`, which retrieves information from inserted rows without a separate `SELECT`.

```
INSERT INTO users (email, name, balance)
VALUES ('david@example.com', 'David', 300)
RETURNING id, email, created_at;
```

Example result:

```
id | email             | created_at
4  | david@example.com | 2026-11-09 18:00:00
```

This is particularly useful in backend applications that need to return a newly created resource.

## 3. UPDATE — Modifying records

`UPDATE` changes existing records.

### Update a single row

```sql
UPDATE users
SET balance = 250
WHERE id = 1;
```

The `WHERE` clause identifies which records to modify.

Without it:

```sql
UPDATE users
SET balance = 250;
```

Every user would receive the same balance.

### Update multiple columns

```sql
UPDATE users
SET
    name = 'Alice Smith',
    balance = 350
WHERE id = 1;
```

### Update using an expression

Instead of replacing a value, modify it relative to its current value.

```sql
UPDATE users
SET balance = balance + 50
WHERE id = 1;
```

This is preferable to reading the balance into application code, calculating the result, and writing it back without concurrency protection.

### Conditional UPDATE

Consider a payment operation where a user can spend money only if sufficient funds exist.

```sql
UPDATE users
SET balance = balance - 80
WHERE id = 1
  AND balance >= 80
RETURNING id, balance;
```

If the balance is insufficient, no row is updated and `RETURNING` produces no rows.

This is a useful atomic conditional update pattern: the balance check and modification are performed in one statement.

## 4. DELETE — Removing records

`DELETE` removes existing rows.

### Delete a specific row

```sql
DELETE FROM users
WHERE id = 4;
```

### Delete using a condition

```sql
DELETE FROM orders
WHERE status = 'CANCELLED';
```

### DELETE with RETURNING

```sql
DELETE FROM orders
WHERE id = 10
RETURNING id, user_id, total;
```

The returned data describes the deleted row.

### The danger of DELETE without WHERE

```sql
DELETE FROM orders;
```

This removes all records from the table.

It does not remove the table itself.

Compare:

```sql
DELETE FROM orders; -- Removes all rows
DROP TABLE orders;   -- Removes the table definition
```

A safe pattern is to inspect the targeted records first:

```sql
SELECT *
FROM orders
WHERE status = 'CANCELLED';

DELETE FROM orders
WHERE status = 'CANCELLED';
```

For critical operations, perform the inspection and deletion within a controlled transaction and verify the affected rows.

## 5. UPSERT — INSERT ON CONFLICT

UPSERT combines inserting and updating.

It is useful when a record should be created if it does not exist, or updated if it already exists.

SQLite supports:

```sql
INSERT INTO ...
ON CONFLICT (...) DO UPDATE ...
```

### Example: Insert or update a user

Suppose `email` must be unique.

```sql
INSERT INTO users (email, name, balance)
VALUES ('alice@example.com', 'Alice Updated', 500)
ON CONFLICT(email)
DO UPDATE SET
    name = excluded.name,
    balance = excluded.balance;
```

Behavior:

- If the email does not exist, insert a new user.
- If the email exists, update the matching user.
    
The special `excluded` qualifier represents values from the row that was proposed for insertion.

UPSERT decision flow:

![[Pasted image 20261110163518.png|523]]

One statement handles both outcomes based on the UNIQUE constraint.

### DO NOTHING

Sometimes duplicates should simply be ignored.

```sql
INSERT INTO users (email, name)
VALUES ('alice@example.com', 'Alice')
ON CONFLICT(email) DO NOTHING;
```

If the email already exists, SQLite skips the insertion.

This is useful for importing records and handling duplicate events when the uniqueness key correctly represents the operation.

### UPSERT with RETURNING

```sql
INSERT INTO users (email, name)
VALUES ('bob@example.com', 'Robert')
ON CONFLICT(email)
DO UPDATE SET name = excluded.name
RETURNING id, email, name;
```

The backend receives the resulting row whether it was inserted or updated.

One important distinction: UPSERT only handles the specified uniqueness conflict. It does not automatically resolve foreign-key failures or arbitrary validation errors.

## 6. RETURNING — Retrieving modified rows

`RETURNING` works with `INSERT`, `UPDATE`, and `DELETE`.

|Statement|What RETURNING provides|
|---|---|
|`INSERT`|Newly inserted row values|
|`UPDATE`|Updated row values|
|`DELETE`|Deleted row values|

Example:

```sql
UPDATE users
SET balance = balance + 100
WHERE id = 2
RETURNING id, name, balance;
```

A backend service can obtain the new balance directly.

This avoids an extra query and reduces the risk of observing a later modification made by another transaction.

SQLite's `RETURNING` is supported from version 3.35.0 onward. PostgreSQL also supports this feature.

## 7. Safe data modification patterns

Production applications need more than correct SQL syntax.

### Use transactions

Suppose money must be transferred from Alice to Bob.

Two operations must succeed together:

```sql
BEGIN IMMEDIATE;

UPDATE users
SET balance = balance - 100
WHERE id = 1
  AND balance >= 100;

UPDATE users
SET balance = balance + 100
WHERE id = 2;

COMMIT;
```

However, this example is not sufficient by itself: the application must check that the first update affected exactly one row before executing the second update. It must also verify the recipient exists.

If either operation fails, execute `ROLLBACK` rather than `COMMIT`.

For example, the backend workflow should be:

```
BEGIN IMMEDIATE
    |
    v
Debit sender
    |
    +-- affected rows != 1 --> ROLLBACK
    |
    v
Credit recipient
    |
    +-- affected rows != 1 --> ROLLBACK
    |
    v
COMMIT
```

`BEGIN IMMEDIATE` acquires SQLite's write transaction early, helping prevent another writer from changing the relevant balances during the transfer.

For real financial applications, use integer minor currency units or an appropriate exact decimal representation instead of floating-point `REAL`.

### Use parameterized queries

Never construct SQL by concatenating untrusted user input.

Unsafe Java example:

```sql
String sql =
    "DELETE FROM users WHERE email = '" + email + "'";
```

Safe JDBC example:

```java
String sql = "DELETE FROM users WHERE email = ?";

try (PreparedStatement stmt =
         connection.prepareStatement(sql)) {

    stmt.setString(1, email);

    int deleted = stmt.executeUpdate();

    System.out.println("Deleted rows: " + deleted);
}
```

Parameters protect against SQL injection by separating SQL structure from supplied values.

### Check affected row counts

An `UPDATE` that executes successfully may modify zero rows.

```java
String sql = """
    UPDATE users
    SET balance = balance - ?
    WHERE id = ?
      AND balance >= ?
    """;

try (PreparedStatement stmt =
         connection.prepareStatement(sql)) {

    stmt.setInt(1, 100);
    stmt.setLong(2, 1);
    stmt.setInt(3, 100);

    int updated = stmt.executeUpdate();

    if (updated != 1) {
        throw new IllegalStateException(
            "User not found or insufficient balance"
        );
    }
}
```

This pattern prevents an application from assuming that a modification succeeded merely because no SQL exception occurred.

### Use optimistic locking

Optimistic locking prevents lost updates when multiple clients modify the same record.

Add a version column:

```sql
ALTER TABLE users
ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
```

Update with an expected version:

```sql
UPDATE users
SET
    name = 'Alice Updated',
    version = version + 1
WHERE id = 1
  AND version = 3;
```

If another transaction already changed the row and incremented the version, this statement updates zero rows.

In Spring Data JPA, the equivalent concept is commonly implemented with `@Version`.

### Consider soft deletion

Instead of physically deleting records, some applications mark them as deleted.

```sql
ALTER TABLE users
ADD COLUMN deleted_at TEXT;
```

Soft delete:

```sql
UPDATE users
SET deleted_at = CURRENT_TIMESTAMP
WHERE id = 1
  AND deleted_at IS NULL;
```

Retrieve active users:

```sql
SELECT *
FROM users
WHERE deleted_at IS NULL;
```

This can support recovery and audit requirements, but every relevant query must account for deleted records.

## 8. Practical backend example

### Idempotent API operations

Imagine an API receiving the same payment request twice because a client retries after a timeout.

Without protection, the backend might process the payment twice.

A common solution is an idempotency key stored in a table with a uniqueness constraint.

```sql
CREATE TABLE payment_requests (
    id INTEGER PRIMARY KEY,
    idempotency_key TEXT NOT NULL UNIQUE,
    amount INTEGER NOT NULL CHECK (amount > 0),
    status TEXT NOT NULL DEFAULT 'PENDING'
);
```

Insert the request:

```sql
INSERT INTO payment_requests (
    idempotency_key, amount
)
VALUES ('request-123', 5000)
ON CONFLICT(idempotency_key) DO NOTHING
RETURNING id, status;
```

If the request already exists, no new row is inserted.

The backend can then retrieve the existing request and decide how to respond.

For production use, the application must also verify that retries with the same key contain the same request parameters, and coordinate the actual payment execution with the stored state. A unique constraint alone does not guarantee exactly-once external payment processing.

## 10. SQLite vs PostgreSQL

|Feature|SQLite|PostgreSQL|
|---|---|---|
|INSERT / UPDATE / DELETE|Supported|Supported|
|Multi-row INSERT|Supported|Supported|
|UPSERT|`ON CONFLICT`|`ON CONFLICT`|
|RETURNING|Since 3.35|Supported|
|Transactions|Supported|Supported|
|Concurrent writers|One writer at a time|Multiple writers using MVCC|
|Row-level locking|No|Yes|
|`UPDATE ... FROM`|Supported|Supported|

Both systems provide powerful data modification features, but PostgreSQL is generally more suitable for backend workloads requiring many concurrent writers.
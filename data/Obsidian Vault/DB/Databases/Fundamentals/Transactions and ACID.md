## 1. What is a database transaction?

A transaction is a group of database operations treated as one logical unit of work.

Either all operations succeed and their changes are committed, or the transaction fails and its changes are rolled back.

Consider transferring €100 between two bank accounts:

1. Subtract €100 from Account A.
2. Add €100 to Account B.
    

Both operations must succeed together.

Without a transaction, the system could subtract the money from Account A and fail before adding it to Account B.

### Transaction lifecycle

![[Pasted image 20261107130424.png]]

Two possible outcomes for an explicitly managed transaction.

A transaction has two principal outcomes:

- `COMMIT` — make the transaction's changes permanent.
- `ROLLBACK` — discard the transaction's uncommitted changes.
    

## 2. ACID properties

ACID describes four fundamental properties of reliable database transactions.

![[Pasted image 20261107130510.png|619]]

### A — Atomicity

Atomicity guarantees that a transaction is applied entirely or not at all.

Example:

```sql
BEGIN;

UPDATE accounts
SET balance = balance - 100
WHERE id = 1;

UPDATE accounts
SET balance = balance + 100
WHERE id = 2;

COMMIT;
```

If the second operation fails, the application must roll back the transaction instead of committing a partial transfer.

Important: A SQL statement affecting zero rows is not necessarily an error. Applications must check affected-row counts and business conditions before committing.

### C — Consistency

Consistency means that a transaction moves the database from one valid state to another, preserving defined constraints and invariants.

For example:

```sql
CREATE TABLE accounts (
    id INTEGER PRIMARY KEY,
    balance NUMERIC NOT NULL
        CHECK (balance >= 0)
);
```

The constraint prevents negative account balances.

If an update violates this constraint, the database rejects that update.

However, consistency is not guaranteed by constraints alone. Business rules such as ensuring that a transfer does not create or destroy money must also be implemented correctly.

### I — Isolation

Isolation controls how concurrent transactions interact.

Suppose two transactions attempt to modify the same account.

![[Pasted image 20261107131136.png|649]]

Databases use mechanisms such as locks, MVCC (Multi-Version Concurrency Control), and isolation levels to control these interactions.

Isolation does not mean every transaction always runs completely independently. The guarantees depend on the database and isolation level.

### D — Durability

Durability means that once a transaction commits successfully, its changes survive a crash, subject to the database's durability configuration and storage guarantees.

Databases typically implement this using mechanisms such as:

- Write-ahead logging (WAL)
- Transaction journals
- Disk synchronization
- Crash recovery
    

In PostgreSQL, WAL records allow committed changes to be recovered after a crash.

In SQLite, durability depends partly on its journal mode and `synchronous` settings.

## 3. Practical example — PostgreSQL

Let's create a simple banking database.

### Step 1 — Create the table

```postgresql
DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY
        PRIMARY KEY,
    owner VARCHAR(100) NOT NULL,
    balance NUMERIC(12, 2) NOT NULL
        CHECK (balance >= 0)
);

INSERT INTO accounts (owner, balance)
VALUES
    ('Alice', 500.00),
    ('Bob', 200.00);
    
SELECT * FROM accounts;
```

Initial state:

|Account|Balance|
|---|---|
|Alice|€500|
|Bob|€200|
|Total|€700|

### Step 2 — Execute a successful transaction

```postgresql
BEGIN;

UPDATE accounts
SET balance = balance - 100
WHERE owner = 'Alice';

UPDATE accounts
SET balance = balance + 100
WHERE owner = 'Bob';

COMMIT;
```

Result:

|Account|Balance|
|---|---|
|Alice|€400|
|Bob|€300|
|Total|€700|

Both operations were committed together.

### Step 3 — Test rollback

```postgresql
BEGIN;

UPDATE accounts
SET balance = balance - 50
WHERE owner = 'Alice';

SELECT * FROM accounts;

ROLLBACK;

SELECT * FROM accounts;
```

Before rollback, the transaction sees Alice's balance as €350.

After rollback, Alice's balance returns to €400.

Key point: Changes made inside a transaction are visible to that transaction before `COMMIT`, but normally remain invisible to other transactions until committed.

### Step 4 — Test a constraint violation

```postgresql
BEGIN;

UPDATE accounts
SET balance = balance - 1000
WHERE owner = 'Alice';
```

PostgreSQL returns an error similar to:

```sql
ERROR: new row for relation "accounts"
violates check constraint "accounts_balance_check"
```

The transaction is now in an aborted state.

```postgresql
ROLLBACK;
```

In PostgreSQL, after a statement error inside a transaction, subsequent ordinary SQL commands are rejected until the transaction is rolled back, or an appropriate savepoint is used.

## 4. Practical example — SQLite

SQLite supports ACID transactions but has a different concurrency architecture from PostgreSQL.

### Step 1 — Create the database

From a terminal:

```sh
sqlite3 banking.db
```

Create the table:

```sqlite
CREATE TABLE accounts (
    id INTEGER PRIMARY KEY,
    owner TEXT NOT NULL,
    balance NUMERIC NOT NULL
        CHECK (balance >= 0)
);

INSERT INTO accounts VALUES
    (1, 'Alice', 500),
    (2, 'Bob', 200);
```

SQLite's `NUMERIC` affinity is not equivalent to PostgreSQL's exact fixed-precision `NUMERIC(12,2)`. For financial applications, storing integer minor units (such as cents) is generally safer in SQLite.

### Step 2 — Execute a transaction

```sqlite
BEGIN TRANSACTION;

UPDATE accounts
SET balance = balance - 100
WHERE id = 1;

UPDATE accounts
SET balance = balance + 100
WHERE id = 2;

COMMIT;
```

### Step 3 — Test rollback

```sqlite
BEGIN TRANSACTION;

UPDATE accounts
SET balance = balance - 50
WHERE id = 1;

ROLLBACK;
```

The account balances remain unchanged.

### SQLite transaction modes

SQLite provides three transaction-start modes:

|Mode|Behavior|
|---|---|
|`BEGIN DEFERRED`|Default; starts without immediately acquiring a write reservation|
|`BEGIN IMMEDIATE`|Starts a write transaction immediately, potentially waiting or failing if another writer is active|
|`BEGIN EXCLUSIVE`|Like immediate in WAL mode; in rollback-journal mode also prevents other connections from reading|

Example:

```sqlite
BEGIN IMMEDIATE;

UPDATE accounts
SET balance = balance - 100
WHERE id = 1;

COMMIT;
```

SQLite permits only one active writer at a time for a database file, although WAL mode allows readers and a writer to operate concurrently.

PostgreSQL supports more extensive concurrent write workloads through row-level locking and MVCC.

## 5. Autocommit and transaction boundaries

A transaction boundary defines where a transaction starts and ends.

### Autocommit

By default, many database clients operate in autocommit mode.

For example:

```sql
UPDATE accounts
SET balance = balance - 100
WHERE id = 1;
```

Without an explicit transaction block, this statement executes as its own transaction in PostgreSQL's normal autocommit mode.

If you execute another update afterward, it is a separate transaction.

This creates a risk for operations that must succeed together.

### Explicit transaction

```sql
BEGIN;

UPDATE accounts
SET balance = balance - 100
WHERE id = 1;

UPDATE accounts
SET balance = balance + 100
WHERE id = 2;

COMMIT;
```

Both updates belong to the same transaction.

### Transaction boundaries in Java and Spring Boot

In a Spring Boot application, transaction boundaries are often defined at the service layer.

```java
@Service
public class TransferService {

    private final AccountRepository accounts;

    public TransferService(
            AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Transactional
    public void transfer(
            Long fromId,
            Long toId,
            BigDecimal amount) {

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException(
                "Accounts must be different"
            );
        }

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                "Amount must be positive"
            );
        }

        Account from = accounts.findById(fromId)
            .orElseThrow();

        Account to = accounts.findById(toId)
            .orElseThrow();

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(
                "Insufficient funds"
            );
        }

        from.setBalance(
            from.getBalance().subtract(amount)
        );

        to.setBalance(
            to.getBalance().add(amount)
        );
    }
}
```

Here, Spring manages the transaction:

1. Start a transaction when entering the proxied `transfer()` method.
2. Execute the database operations.
3. Commit if the method completes successfully.
4. Roll back by default if an unchecked exception occurs.
    

This example illustrates transaction boundaries, not a complete concurrency-safe transfer implementation. Concurrent transfers require appropriate locking, optimistic version checks, or atomic conditional updates.

Also remember that Spring's default rollback behavior does not automatically include checked exceptions. You can configure that explicitly:

```java
@Transactional(rollbackFor = Exception.class)
```

## 6. Savepoints — partial rollback

Sometimes you want to undo only part of a transaction.

SQL provides savepoints for this.

```postgresql
BEGIN;

UPDATE accounts
SET balance = balance + 100
WHERE id = 1;

SAVEPOINT before_bonus;

UPDATE accounts
SET balance = balance + 50
WHERE id = 2;

ROLLBACK TO SAVEPOINT before_bonus;

COMMIT;
```

The first update is committed, but the second update is undone.

First update: +100 — committed

Savepoint created

Second update: +50 — rolled back

Savepoints are supported by both SQLite and PostgreSQL.

They are particularly useful in complex import processes where some operations can be retried or undone without discarding the entire transaction.

## 7. PostgreSQL vs SQLite transaction behavior

|Feature|PostgreSQL|SQLite|
|---|---|---|
|ACID transactions|Yes|Yes|
|Explicit transactions|`BEGIN`|`BEGIN`|
|Rollback|Yes|Yes|
|Savepoints|Yes|Yes|
|MVCC|Yes|Snapshot-based concurrency in WAL mode|
|Concurrent writers|Yes, subject to conflicts|One writer at a time|
|Default isolation|Read Committed|Serializable by default, with documented exceptions|
|WAL|Supported, normally enabled|Optional journal mode|

Both are reliable transactional databases, but they are designed for different concurrency and deployment requirements.

SQLite is particularly useful for embedded applications, local storage, and development.

PostgreSQL is generally better suited for backend services with many concurrent connections and complex transactional workloads.

## 8. Common transaction mistakes

### Mistake 1 — Forgetting the transaction

```sql
UPDATE accounts SET balance = balance - 100
WHERE id = 1;

UPDATE accounts SET balance = balance + 100
WHERE id = 2;
```

If these run as separate autocommitted statements, the first change may persist even when the second fails.

### Mistake 2 — Keeping transactions open too long

```sql
BEGIN;

UPDATE orders
SET status = 'PROCESSING'
WHERE id = 10;

-- Application waits 30 seconds
-- for an external HTTP service

COMMIT;
```

Long-running transactions may hold locks, delay cleanup, and reduce concurrency.

Prefer keeping database transactions short and avoid external network calls while holding them open when possible.

### Mistake 3 — Assuming transactions prevent every race condition

A transaction guarantees atomicity, but it does not automatically prevent every concurrency anomaly.

For example, two transactions can read the same initial value and make conflicting business decisions.

The correct solution may require:

- Row-level locks
- Optimistic locking
- Conditional updates
- A stronger isolation level
    

These topics belong to the later Transactions and Concurrency category.

### Mistake 4 — Assuming rollback reverses external effects

A database rollback cannot automatically undo an email already sent, an HTTP request to another service, or a payment processed by an external provider.

Distributed workflows may require patterns such as the transactional outbox, idempotency, or sagas.

## 9. Summary

|Concept|Key idea|
|---|---|
|Transaction|Logical unit of database work|
|Atomicity|All operations succeed or none are applied|
|Consistency|Defined constraints and invariants are preserved|
|Isolation|Concurrent transactions interact under controlled rules|
|Durability|Committed changes survive failures|
|`COMMIT`|Permanently completes a transaction|
|`ROLLBACK`|Undoes uncommitted changes|
|`SAVEPOINT`|Allows partial rollback|
|`@Transactional`|Defines a Spring-managed transaction boundary|

The most important lesson is that a transaction is a unit of correctness, not merely a group of SQL statements. It should represent a complete business operation whose database changes must succeed or fail together.
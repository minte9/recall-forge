## 1. Overview

A Database Management System (DBMS) is much more than a program that stores and retrieves data. Internally, it is a collection of components responsible for interpreting SQL, choosing efficient execution strategies, managing memory and disk storage, coordinating concurrent operations, and recovering from failures.

When an application executes:

```sql
SELECT name
FROM users
WHERE id = 10;
```

the DBMS must determine:

1. Whether the SQL is valid.
2. Which table and columns are referenced.
3. Whether an index can accelerate the lookup.
4. Which physical operations should execute.
5. Whether the required data is already in memory.
6. How to retrieve the data from disk if necessary.
7. How to return the result to the application.

Understanding this architecture helps software engineers write better queries, investigate performance problems, and understand database reliability.

## 2. High-level DBMS architecture

Conceptual DBMS architecture. Real systems also include transaction, concurrency, catalog, and background-process components.

![[Pasted image 20261106175522.png]]

The major components are:

|Component|Responsibility|
|---|---|
|SQL Parser|Converts SQL text into an internal representation|
|Query Planner|Selects an efficient execution plan|
|Execution Engine|Executes the selected plan|
|Buffer Manager|Caches database pages in memory|
|Storage Manager|Organizes tables and indexes on disk|
|Transaction Manager|Coordinates transactions and concurrency|
|WAL / Recovery|Maintains durability and restores consistency after failures|

## 3. SQL parsing

Before executing a query, the DBMS must understand its structure.

Consider:

```sql
SELECT name, email
FROM users
WHERE age > 25
ORDER BY name;
```

The parser processes the query into a structured representation, commonly called an Abstract Syntax Tree (AST) or parse tree.

![[Pasted image 20261106175625.png]]

Parsing involves several operations:

- Lexical analysis: Identify tokens such as `SELECT`, `FROM`, identifiers, and operators.
- Syntax analysis: Verify that tokens follow SQL grammar.
- Semantic analysis: Resolve tables and columns, check types, and validate references.
    
For example:

```sql
SELEC name FROM users;
```

fails because `SELEC` is not the SQL keyword `SELECT`.

However:

```sql
SELECT unknown_column FROM users;
```

can be syntactically valid but fails during name resolution if the column does not exist.

### SQLite vs. PostgreSQL

SQLite compiles SQL statements into instructions for its virtual machine, the VDBE (Virtual Database Engine).

PostgreSQL uses a parser, semantic analysis, query rewriting, planning, and execution stages.

The same SQL language can therefore be processed by significantly different internal implementations.

## 4. Query planning and optimization

After understanding a query, the DBMS decides how to execute it.

Consider a table containing one million users:

```sql
SELECT *
FROM users
WHERE email = 'alice@example.com';
```

Two possible strategies are:

**Strategy A — Full table scan**

Read the table and inspect rows until all matching records have been identified.

**Strategy B — Index lookup**

Use an index on `email` to locate matching rows efficiently.

![[Pasted image 20261106175848.png]]

These complexity descriptions are simplified. Real performance depends on selectivity, cached pages, physical I/O, index structure, and the number of returned rows.

### PostgreSQL example

Create a PostgreSQL database using Docker Compose.

```sh
cd /postgresql-learning
touch compose.yml
```

compose.yml

```yaml
services:
  postgres:
    image: postgres:16
    container_name: postgres-learning
    restart: unless-stopped

    environment:
      POSTGRES_DB: learning
      POSTGRES_USER: student
      POSTGRES_PASSWORD: learning123

    ports:
      - "127.0.0.1:5434:5432"

    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql:ro

    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U student -d learning"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

Start container:

```sh
docker compose up -d
```

Connect through the terminal or IDE:

```sh
docker exec -it postgres-learning \
    psql -U student -d learning
```

Your `compose.yml` contains:

```yaml
ports:
  - "127.0.0.1:5434:5432"
```

This means:

- 5434 — Port accessible from Ubuntu (DataGrip)
- 5432 — Port used inside the Docker container

Create a table:

```postgresql
CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL
);

CREATE INDEX idx_users_email
ON users(email);
```

Inspect the execution plan:

```postgresql
EXPLAIN
SELECT *
FROM users
WHERE email = 'alice@example.com';
```

A possible plan is:

```
Index Scan using idx_users_email on users
  Index Cond: (email = 'alice@example.com')
```

PostgreSQL uses a cost-based optimizer. It estimates the cost of different strategies using statistics such as row counts, data distribution, and selectivity.

A sequential scan may still be preferable for small tables or queries returning a large percentage of rows.

For actual execution measurements:

```postgresql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM users
WHERE email = 'alice@example.com';
```

`ANALYZE` executes the query and reports actual timing and row counts. `BUFFERS` reports buffer activity.

### SQLite example

SQLite provides:

```sqlite
EXPLAIN QUERY PLAN
SELECT *
FROM customers 
WHERE email = 'alice@example.com';
```

Possible output:

```
SEARCH customers USING INDEX sqlite_autoindex_customers_1 (email=?)
```

Without a suitable index, the plan may show:

```
SCAN users
```

Practical lesson: SQL describes what data is required, while the query planner determines how to retrieve it.

## 5. Query execution engine

Once a plan is selected, the execution engine performs the physical operations.

For example:

```sql
SELECT department, COUNT(*)
FROM employees
WHERE salary > 50000
GROUP BY department;
```

A conceptual execution pipeline is:

![[Pasted image 20261106182126.png]]

Actual execution is not always a simple materialized sequence. PostgreSQL commonly uses a demand-driven executor where plan nodes request tuples from child nodes.

SQLite executes bytecode instructions using its VDBE.

Typical physical operators include:

|Operator|Purpose|
|---|---|
|Sequential Scan|Read table rows|
|Index Scan|Retrieve rows using an index|
|Filter|Apply conditions|
|Nested Loop Join|Match rows through repeated inner lookups|
|Hash Join|Join using a hash table|
|Sort|Order results|
|Aggregate|Calculate counts, sums, averages|

PostgreSQL supports hash joins and merge joins. SQLite primarily uses nested-loop joins, with indexes and other optimizations making them efficient.

## 6. Buffer pools and memory management

Reading data from disk is generally more expensive than accessing it from RAM.

A DBMS therefore caches frequently accessed database pages.

A page is a fixed-size unit of storage, typically containing multiple rows or index entries.

PostgreSQL commonly uses 8 KB pages. SQLite commonly defaults to 4 KB pages, although its page size is configurable.

![[Pasted image 20261106194441.png]]

### How a page is read

1. The execution engine requests a page.
2. The buffer manager checks whether the page is cached.
3. If present, the DBMS reads it from memory — a cache hit.
4. Otherwise, the page is fetched from storage — a cache miss.
5. If necessary, a cached page is evicted to make space.
    
Modified pages are called dirty pages. They eventually need to be written to persistent storage.

### PostgreSQL

PostgreSQL uses a shared buffer pool configured through:

```postgresql
SHOW shared_buffers;
```

It also benefits from the operating system's filesystem cache.

PostgreSQL uses a clock-sweep-based buffer replacement strategy, with additional policies for some access patterns.

### SQLite

SQLite uses a page cache associated with database connections and pager structures.

Inspect the configured cache size:

```sqlite
PRAGMA cache_size;
```

Configure an approximate 64 MiB cache target:

```sqlite
PRAGMA cache_size = -65536;
```

Negative values specify a target in KiB rather than a page count.

Unlike PostgreSQL's shared buffer architecture, SQLite's page caching is generally connection-local, although SQLite supports shared-cache configurations.

## 7. Disk storage organization

A database does not typically store each row in a separate operating-system file.  

Instead, it organizes data into pages inside database files.  

A simplified page might look like:

![[Pasted image 20261106194959.png]]

### PostgreSQL storage

PostgreSQL stores tables in heap files, typically organized into 8 KB pages.

Each heap page contains:

- A page header
- Line pointers referencing tuples
- Free space
- Tuple data
    
Indexes, such as B-tree indexes, are stored separately from heap tables.

PostgreSQL also uses MVCC (Multi-Version Concurrency Control). Updates often create a new tuple version instead of overwriting the existing tuple in place.

Old tuple versions may later be reclaimed by `VACUUM`.

### SQLite storage

SQLite commonly stores an entire database in a single main database file.

Its tables and indexes are organized using B-trees.

For ordinary rowid tables, table B-tree leaf pages store records, while index B-trees store index entries.

SQLite also supports `WITHOUT ROWID` tables, which use a different table organization.

Inspect the database page size:

```sqlite
PRAGMA page_size;
```

And page count:

```sqlite
PRAGMA page_count;
```

Approximate main database file size:

```
Size = page_size * page_count
```

This does not include separate WAL or shared-memory files.

## 8. Write-Ahead Logging (WAL)

One of the most important reliability mechanisms in database systems is Write-Ahead Logging.

The principle is:

> Record sufficient recovery information in a durable log before writing the corresponding modified database pages to their persistent locations.

Why is this necessary?

Imagine a bank transfer:

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

If the database crashes between physical page writes, the stored database must not be left with only one side of the committed transfer.

### PostgreSQL WAL

PostgreSQL records changes in WAL before modified data pages are written to their permanent locations.

Simplified PostgreSQL commit sequence

1. Transaction modifies pages in memory.
2. PostgreSQL generates WAL records describing the changes.
3. At commit, the required WAL is flushed to durable storage under normal durability settings.
4. The transaction is acknowledged as committed.
5. Dirty data pages can be written to disk later.
    
The crucial distinction is that committing a transaction does not require every modified table page to be written immediately.

Inspect the current WAL location:

```postgresql
SELECT pg_current_wal_lsn();
```

Inspect the WAL level:

```postgresql
SHOW wal_level;
```

PostgreSQL stores WAL segment files under `pg_wal` within its data directory.

### SQLite WAL

SQLite also supports WAL mode, but its implementation differs significantly from PostgreSQL.

Enable WAL mode:

```
PRAGMA journal_mode = WAL;
```

In this mode, SQLite appends changed page images to a WAL file rather than immediately overwriting the main database file.

The WAL file is commonly named:

```
database.db-wal
```

SQLite may also create:

```
database.db-shm
```

The shared-memory file supports WAL indexing and coordination between connections.

A checkpoint copies eligible committed page changes from the WAL into the main database file.

```sqlite
PRAGMA wal_checkpoint(PASSIVE);
```

SQLite WAL supports concurrent readers and a writer, but still permits only one writer at a time.

### Important distinction

|Aspect|PostgreSQL|SQLite|
|---|---|---|
|WAL contents|Recovery records for data changes|Page images in WAL frames|
|Main purpose|Durability and crash recovery; also supports replication|Durability, crash recovery, and improved reader/writer concurrency|
|Checkpointing|Coordinates recovery position and dirty-page flushing|Transfers WAL pages into the main database|
|Concurrency|Multiple concurrent writers with transaction coordination|One writer at a time|

## 9. Crash recovery

What happens if the server loses power immediately after a transaction commits?

### PostgreSQL recovery

At startup after an unclean shutdown, PostgreSQL uses the latest checkpoint and available WAL records to recover database state.

The simplified process:

1. Locate the recovery starting point.
2. Read WAL records from the required position.
3. Replay necessary changes to restore database pages.
4. Restore a consistent state.
5. Resume normal operations.
    
PostgreSQL does not use a conventional undo log to roll back every uncommitted data change. Its MVCC visibility rules ensure that uncommitted tuple versions are not visible as committed data.

### SQLite recovery

SQLite recovery depends on its journal mode.

In WAL mode, committed transactions are identified through valid WAL frames and commit markers. SQLite can use the WAL to recover committed changes that have not yet been checkpointed.

In rollback-journal mode, SQLite instead uses a rollback journal to restore original page contents after interrupted writes.

WAL and recovery are closely related, but their exact mechanisms are database-specific.

## 10. Complete practical example

We will create a database, inspect query plans, and observe storage configuration.

### SQLite

```sqlite
CREATE TABLE products (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    price REAL NOT NULL
);

INSERT INTO products (name, category, price)
VALUES
    ('Laptop', 'Electronics', 1200),
    ('Monitor', 'Electronics', 350),
    ('Desk', 'Furniture', 500),
    ('Chair', 'Furniture', 200);

CREATE INDEX idx_products_category
ON products(category);
```

Inspect the query plan:

```sqlite
EXPLAIN QUERY PLAN
SELECT *
FROM products
WHERE category = 'Electronics';
```

Inspect storage:

```sqlite
PRAGMA page_size;
PRAGMA page_count;
PRAGMA cache_size;
PRAGMA journal_mode;
```

Enable WAL:

```sqlite
PRAGMA journal_mode = WAL;
```

Run a transaction:

```sqlite
BEGIN;

UPDATE products
SET price = price * 1.10
WHERE category = 'Electronics';

COMMIT;
```

SQLite records the transaction according to its active journal mode. With WAL enabled, committed page changes can remain in the WAL until checkpointed.

### PostgreSQL

```postgresql
CREATE TABLE products (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    price NUMERIC(10, 2) NOT NULL
);

INSERT INTO products (name, category, price)
VALUES
    ('Laptop', 'Electronics', 1200),
    ('Monitor', 'Electronics', 350),
    ('Desk', 'Furniture', 500),
    ('Chair', 'Furniture', 200);

CREATE INDEX idx_products_category
ON products(category);
```

Inspect the plan:

```postgresql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM products
WHERE category = 'Electronics';
```

With only four rows, PostgreSQL may prefer a sequential scan even though an index exists.

Inspect storage and WAL configuration:

```postgresql
SHOW block_size;
SHOW shared_buffers;
SHOW wal_level;
SHOW synchronous_commit;
```

Inspect table size:

```postgresql
SELECT
    pg_size_pretty(pg_relation_size('products')) AS table_size,
    pg_size_pretty(pg_total_relation_size('products')) AS total_size;
```

`pg_relation_size` measures the main table relation by default; `pg_total_relation_size` includes indexes and associated auxiliary storage.

## 11. SQLite vs. PostgreSQL architecture

|Feature|SQLite|PostgreSQL|
|---|---|---|
|Architecture|Embedded library|Client-server DBMS|
|SQL execution|Bytecode virtual machine|Plan-node executor|
|Query optimizer|Cost-based planning using available statistics and heuristics|Advanced cost-based optimizer|
|Page cache|Primarily per connection|Shared buffers across server processes|
|Storage|Main database file with B-tree structures|Heap tables and separate index relations|
|Concurrency|Multiple readers, one writer|Multiple concurrent readers and writers|
|Transactions|ACID transactions|ACID transactions with MVCC|
|WAL|Optional journal mode|Core durability and recovery mechanism|
|Recovery|WAL or rollback journal|WAL replay and MVCC visibility|

SQLite is excellent for embedded applications, local tools, mobile applications, and lightweight deployments.

PostgreSQL is designed for applications needing sophisticated concurrent transactions, advanced query optimization, replication, and server-side database management.

Neither architecture is universally superior; each is optimized for different deployment and workload requirements.

## 12. Practical software engineering considerations

Understanding DBMS internals directly influences application design.

Query performance: An index can improve a lookup, but it also introduces additional storage and write overhead. Always inspect execution plans before assuming a particular strategy is faster.

Memory: A slow query may be caused by excessive disk reads rather than inefficient SQL syntax. PostgreSQL's `EXPLAIN (ANALYZE, BUFFERS)` helps distinguish some of these cases.

Transactions: Use transactions to preserve consistency across related modifications. In Spring Boot, `@Transactional` can define a transaction boundary, but the database remains responsible for enforcing transactional guarantees.

Durability: A successful commit depends on database durability settings and the storage system honoring flush requests. WAL is not a replacement for backups.

Concurrency: PostgreSQL supports concurrent writers using MVCC and locking. SQLite can work well with concurrent readers but may experience writer contention under write-heavy workloads.
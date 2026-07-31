## DB / MySQL / Indexing / Clustered vs Secondary Indexes

### 1. The Clustered Index

Because InnoDB's table data *is* the primary key's B+tree, every table has
exactly one clustered index -- the PK. If a table has no explicit PK,
InnoDB picks the first `UNIQUE NOT NULL` column, or silently generates a
hidden internal row ID -- worth declaring a PK explicitly rather than
relying on that fallback.

~~~sql
CREATE TABLE orders (id INT PRIMARY KEY, total NUMERIC)
-- id is the clustered index; no PK means InnoDB invents one internally
~~~

### 2. Secondary Indexes Store the PK

A secondary index's leaf nodes don't point directly at a row's disk
location -- they store the primary key value instead. So a lookup via a
secondary index is really two B+tree traversals: one on the secondary
index to find the PK, one on the clustered index to fetch the row. This
is why a wide/unstable PK makes every secondary index bigger and slower.

~~~sql
CREATE INDEX idx_customer ON orders (customer_id);
-- lookup: idx_customer -> id (PK) -> clustered index -> row
~~~
